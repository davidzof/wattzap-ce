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

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
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
 * Speed Sensor
 * 
 * @author David George
 * @date 24th November 2014
 * 
 *       (c) 2014 David George / Wattzap.com
 */
public class SpeedListener extends AntListener implements MessageCallback {
	public static String name = "C:SPD";
	private static final byte DEVICE_TYPE = (byte) 0x7B;
	private static final short MESSAGE_PERIOD = 8118;
	private final static Logger logger = LogManager.getLogger("SpeedListener");
	private int lastCount = -1;
	private int lastTime = -1;
	private int cCount = 0;
	private boolean initializing = false;
	private Rolling powerRatio;
	private boolean simulSpeed;
	private static long elapsedTime;
	private double distance = 0.0;

	RouteReader routeData;
	private double mass;
	private final UserPreferences userPrefs = UserPreferences.INSTANCE;

	// initialize for pairing
	private double wheelSize = userPrefs.getWheelSizeCM();
	private int resistance = userPrefs.getResistance();
	private Power power = userPrefs.getPowerProfile();

	public SpeedListener() {
		MessageBus.INSTANCE.register(Messages.START, this);
		MessageBus.INSTANCE.register(Messages.STARTPOS, this);
		MessageBus.INSTANCE.register(Messages.GPXLOAD, this);
	}

	@Override
	public void receiveMessage(BroadcastDataMessage message) {
		int time = (message.getUnsignedData()[5] << 8)
				| message.getUnsignedData()[4];

		int count = (message.getUnsignedData()[7] << 8)
				| message.getUnsignedData()[6];
		logger.debug("time " + time + " count " + count);

		Telemetry t = getTelemetry(time, count);
		if (t != null) {
			MessageBus.INSTANCE.send(Messages.SPEED, t);
		}
	}

	/**
	 * Works out Power, Distance, Speed and other values from the instantaneous
	 * roller speed.
	 * 
	 * @param time
	 *            Time since last reading
	 * @param count
	 *            Number of wheel rotations
	 * @return Telemetry or null if no reliable data could be calculated.
	 */
	Telemetry getTelemetry(int time, int count) {
		
		
		if (lastCount == -1) {
			// first time thru, initialize counters
			lastCount = count;
			lastTime = time;
			return null;
		} else if (initializing && lastCount != count) {
			// reset counters when we see first change, some devices seem to
			// have garbage in the pipe at startup.
			initializing = false;
			lastCount = count;
			lastTime = time;
			elapsedTime = System.currentTimeMillis();
			return null;
		}

		int tDiff = ((time - lastTime) & 0xffff);
		int sDiff = ((count - lastCount) & 0xffff);
		
		if (tDiff > 5000) {
			/*
			 * these values occur sometimes in the data stream with the combo
			 * speed and cadence listener. Apparently they are none-standard
			 * battery level messages. Log and return.
			 */
			logger.error("Bogus value: time " + time + " tDiff " + tDiff
					+ " count " + count);
			lastCount = -1;
			return null;
		}

		
		double speed = 0;
		double distanceKM = 0;
		Telemetry t = new Telemetry();
		if (tDiff > 0) {
			double timeS = ((double) tDiff) / 1024;
			elapsedTime += (int) (timeS * 1000);
			distanceKM = (sDiff * wheelSize) / 100000;

			speed = distanceKM / (timeS / (3600));
			int powerWatts = power.getPower(speed, resistance);
			t.setPower(powerWatts);

			// if we have GPX Data and Simulspeed is enabled calculate speed
			// based on power and gradient using magic sauce
			if (simulSpeed && routeData != null) {
				Point p = routeData.getPoint(distance);
				if (routeData.routeType() == RouteReader.SLOPE) {
					if (p == null) {
						// end of the road
						distance = 0.0;
						return null;
					}
					if (powerWatts > 0) {
						// only works when power is positive, this is most of
						// the time on a turbo
						double realSpeed = (power.getRealSpeed(mass,
								p.getGradient() / 100, powerWatts)) * 3.6;

//						realSpeed = (realSpeed * 3.600) / 1000;
						distanceKM = (realSpeed / speed) * distanceKM;
						speed = realSpeed;
					}
				} else {
					/*
					 * Power Profile: speed is the ratio of our trainer power to
					 * the expected power, we also apply a bit of smoothing
					 */
					double ratio = powerRatio.add(powerWatts / p.getPower());

					// speed is video speed * power ratio
					speed = p.getSpeed() * ratio;
					distanceKM = (speed / 3600) * timeS;
				}
			}

			cCount = 0;
		} else if (cCount < 6) {
			// a zero value may just be due to too fast sensor update, wait 6 messages before sending zero
			cCount++;
			return null;
		}

		lastTime = time;
		lastCount = count;
		t.setDistanceMeters(distance * 1000);
		if (routeData != null) {
			Point p = routeData.getPoint(distance);
			if (p == null) {
				// end of the road
				distance = 0.0;
				return null;
			}
			t.setElevation(p.getElevation());
			t.setGradient(p.getGradient());
			t.setLatitude(p.getLatitude());
			t.setLongitude(p.getLongitude());
		}
		t.setSpeed(speed);
		t.setTime(elapsedTime);
		distance += distanceKM;

		logger.debug("sending " + t);
		return t;
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
			initializing = true;
			lastCount = -1;
			powerRatio = new Rolling(10);
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