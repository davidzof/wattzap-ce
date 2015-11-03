/* This file is part of Wattzap Community Edition.
 *
 * Wattzap Community Edtion is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Wattzap Community Edition is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Wattzap.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wattzap.model.ant;

import org.cowboycoders.ant.messages.data.BroadcastDataMessage;

import com.wattzap.controller.MessageBus;
import com.wattzap.controller.MessageCallback;
import com.wattzap.controller.Messages;
import com.wattzap.model.RouteReader;
import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.Point;
import com.wattzap.model.dto.Telemetry;
import com.wattzap.model.power.Power;
import com.wattzap.utils.Rolling;

/**
 * Power Meter. Converts power to speed based on rider's mass.
 * 
 * @author David George
 * @date 14th November 2014
 * 
 *       (c) 2014 David George / Wattzap.com
 */
public class PowerListener extends AntListener implements MessageCallback {
	public static String name = "C:POW";
	private static final byte DEVICE_TYPE = (byte) 0x0B;
	private static final short MESSAGE_PERIOD = 8182; // 4hz 8182/32768

	private boolean simulSpeed;
	private long lastTime;
	private double distance = 0.0;
	private boolean cadenceSensor;

	private int count = -1;

	RouteReader routeData;
	private double mass;
	private Rolling averagePower;
	private final UserPreferences userPrefs = UserPreferences.INSTANCE;

	// initialize for pairing
	private double wheelSize = userPrefs.getWheelSizeCM();
	private int resistance = userPrefs.getResistance();
	private Power power = userPrefs.getPowerProfile();

	public PowerListener() {
		MessageBus.INSTANCE.register(Messages.START, this);
		MessageBus.INSTANCE.register(Messages.STARTPOS, this);
		MessageBus.INSTANCE.register(Messages.GPXLOAD, this);

		int id = userPrefs.getCadenceId();
		if (id > 0) {
			cadenceSensor = true; // cadence done by cadence sensor
		}
	}

	@Override
	public void receiveMessage(BroadcastDataMessage message) {
		if (message.getUnsignedData()[0] == 0x10) {
			// simple power message
			int powerWatts = 0;
			if (message.getUnsignedData()[1] != count) {
				powerWatts = (message.getUnsignedData()[7] << 8)
						| message.getUnsignedData()[6];
				powerWatts = (int) averagePower.add(powerWatts);
				count = message.getUnsignedData()[1] & 0xFF;
			}

			int rpm = -1;
			if (message.getUnsignedData()[3] != 255) {
				rpm = (byte) (message.getUnsignedData()[3] & 0xFF);
			}

			if (lastTime == -1) {
				lastTime = System.currentTimeMillis();
				return;
			}

			double speed = 0;
			double distanceKM = 0;
			double timeS = 0;
			long currentTime = System.currentTimeMillis();
			long tDiff = currentTime - lastTime;
			lastTime = currentTime;

			Telemetry t = new Telemetry();
			t.setPower(powerWatts);

			// if we have GPX Data and Simulspeed is enabled calculate speed
			// based on power and gradient using magic sauce
			if (simulSpeed && routeData != null) {
				Point p = routeData.getPoint(distance);
				if (routeData.routeType() == RouteReader.SLOPE) {
					if (p == null) {
						// end of the road
						distance = 0.0;
						return;
					}
					if (powerWatts > 0) {
						// only works when power is positive, this is most of
						// the time on a turbo
						speed = (power.getRealSpeed(mass,
								p.getGradient() / 100, powerWatts)) * 3.6;
						// d = s * t
						distanceKM = (speed * tDiff) / 360000;
						// System.out.println("speed " + speed + " distanceKM "
						// + distanceKM + " watts " + powerWatts + " mass " +
						// mass + " tDiff " + tDiff);
					}
				} else {
					// power profile, speed is the ratio of our trainer power to
					// the expected power
					double ratio = (powerWatts / p.getPower());
					// speed is video speed * power ratio
					speed = p.getSpeed() * ratio;
					distanceKM = (speed / 3600) * timeS;
				}
			} else {
				speed = power.getRealSpeed(mass, 0, powerWatts);
				distanceKM = (speed * tDiff) / 360000;

			}

			t.setDistanceMeters(distance * 1000);
			if (routeData != null) {
				Point p = routeData.getPoint(distance);
				if (p == null) {
					// end of the road
					distance = 0.0;
					return;
				}
				t.setElevation(p.getElevation());
				t.setGradient(p.getGradient());
				t.setLatitude(p.getLatitude());
				t.setLongitude(p.getLongitude());
			}
			t.setSpeed(speed);
			t.setTime(currentTime);
			distance += distanceKM;

			MessageBus.INSTANCE.send(Messages.SPEED, t);
			if (cadenceSensor == false && rpm != -1) {
				MessageBus.INSTANCE.send(Messages.CADENCE, rpm);
			}

		}
	}

	@Override
	public void callback(Messages message, Object o) {
		switch (message) {
		case START:
			// get up to date values
			mass = userPrefs.getTotalWeight();
			wheelSize = userPrefs.getWheelSizeCM();
			resistance = userPrefs.getResistance();
			power = userPrefs.getPowerProfile();
			simulSpeed = userPrefs.isVirtualPower();
			lastTime = -1;
			System.out.println(userPrefs.getPowerSmoothing());
			averagePower = new Rolling(userPrefs.getPowerSmoothing());
			break;
		case STARTPOS:
			distance = (Double) o;
			break;
		case GPXLOAD:
			this.routeData = (RouteReader) o;
			distance = 0.0;
			break;
		}
	}

	@Override
	public int getChannelId() {
		return 0;
	}

	@Override
	public int getChannelPeriod() {
		return MESSAGE_PERIOD;
	}

	@Override
	public int getDeviceType() {
		return DEVICE_TYPE;
	}

	@Override
	public String getName() {
		return name;
	}
}