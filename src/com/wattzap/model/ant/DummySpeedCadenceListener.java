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

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

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
 * (c) 2013 David George / WattzAp.com
 * 
 * Speed and Cadence ANT+ processor.
 * 
 * @author David George
 * @date 11 June 2013
 */
public class DummySpeedCadenceListener extends Thread implements
		MessageCallback {
	private boolean running = true;
	private double distance = 0.0;
	private final UserPreferences userPrefs = UserPreferences.INSTANCE;

	RouteReader routeData = null;
	Rolling rPower;

	double mass;
	double wheelSize;
	int resistance;
	Power power;
	boolean virtualPower;

	private static Logger logger = LogManager
			.getLogger("DummySpeedCadenceListener");

	public DummySpeedCadenceListener() {
		MessageBus.INSTANCE.register(Messages.START, this);
		MessageBus.INSTANCE.register(Messages.STARTPOS, this);
		MessageBus.INSTANCE.register(Messages.STOP, this);
		MessageBus.INSTANCE.register(Messages.GPXLOAD, this);
	}

	public void run() {
		while (true) {
			// sleep first
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (running) {
				Point p;

				Telemetry t = new Telemetry();

				// half FTP
				// int powerWatts = (int)
				// ((userPrefs.getMaxPower() + (Math
				// .random() * 10)) * 0.5);

				int powerWatts = (int) (userPrefs.getMaxPower() * 0.5);

				double speed = 0;
				// if ant disabled always use this calculation
				if ((virtualPower || !userPrefs.isAntEnabled())
						&& routeData != null) {

					if (routeData.routeType() == RouteReader.SLOPE) {
						p = routeData.getPoint(distance);

						/*
						 * increase power for hills to FTP
						 */
						if (p != null) {
							powerWatts += powerWatts
									* (p.getGradient() / routeData
											.getMaxSlope());

							if (userPrefs.getResistance() == power.getResitanceLevels() ) {
								// AUTO
								int r = power.getResistance(p.getGradient());
								t.setResistance(r);
								speed = power.getSpeed(powerWatts, r);
							} else {
								// speed corresponding to this power
								speed = power.getSpeed(powerWatts,
										userPrefs
												.getResistance());
							}

							BigDecimal bd = new BigDecimal(speed).setScale(2,
									RoundingMode.HALF_UP);

							t.setVirtualSpeed(bd.intValue());

							// we need to now calculate video speed but we need
							// to
							// display real speed
							// what would be our real speed for those watts -
							// show
							// in odo

							double realSpeed = power.getRealSpeed(mass,
									p.getGradient() / 100, powerWatts);
							speed = (realSpeed * 3600) / 1000;
						}
					} else {
						// here we are a power file
						// TODO For power files we just want to play at normal
						// speed. OK we no there is no ANT here.

						p = routeData.getPoint(distance);
						// power comes from video (gradient)
						// powerWatts = (int) ((p.getGradient()) +
						// (Math.random() * 4));
						powerWatts = (int) p.getPower();

						// apply some smoothing
						rPower.add(powerWatts);
						powerWatts = (int) rPower.getAverage();

						if (userPrefs.getResistance() == 0) {
							t.setResistance(1);
							speed = power.getSpeed(powerWatts, 1);
						} else {
							// speed corresponding to this power
							speed = power.getSpeed(powerWatts,
									userPrefs.getResistance());
							BigDecimal bd = new BigDecimal(speed).setScale(2,
									RoundingMode.HALF_UP);

							t.setVirtualSpeed(bd.intValue());
							t.setResistance(userPrefs
									.getResistance());
							if (routeData.getExtension().equals("pwr")) {
								speed = p.getSpeed();
							}
						}
					}
				} else {
					speed = power.getRealSpeed(mass, 0, powerWatts) * 3.6;
				}

				t.setPower(powerWatts);

				if (routeData != null) {
					p = routeData.getPoint(distance);
					if (p == null) {
						// end of the road
						t.setDistanceMeters(distance * 1000);
						distance = 0.0;
						t.setSpeed(0);
						t.setTime(System.currentTimeMillis());
						MessageBus.INSTANCE.send(Messages.SPEED, t);
						return;
					}
					t.setElevation(p.getElevation());
					t.setGradient(p.getGradient());
					t.setLatitude(p.getLatitude());
					t.setLongitude(p.getLongitude());
				}

				t.setSpeed(speed);
				t.setDistanceMeters(distance * 1000);
				t.setTime(System.currentTimeMillis());

				MessageBus.INSTANCE.send(Messages.SPEED, t);

				// d = s * t
				distance += (speed / 3600) * 0.25;
			}
		}

	}

	public void callback(Messages message, Object o) {
		switch (message) {

		case START:
			// get uptodate values
			mass = userPrefs.getTotalWeight();
			wheelSize = userPrefs.getWheelSizeCM();
			resistance = userPrefs.getResistance();
			power = userPrefs.getPowerProfile();
			virtualPower = userPrefs.isVirtualPower();
			if (this.getState() == Thread.State.NEW) {
				start();
			}
			running = true;
			break;
		case STOP:
			running = false;
			break;
		case STARTPOS:
			distance = (Double) o;
			break;
		case GPXLOAD:
			// code to see if we are registered
			if (!userPrefs.isRegistered()
					&& (userPrefs.getEvalTime()) <= 0) {
				logger.info("Out of time "
						+ userPrefs.getEvalTime());
				userPrefs.shutDown();
				System.exit(0);
			}

			this.routeData = (RouteReader) o;
			power = userPrefs.getPowerProfile();
			power.setGrades(routeData.getMaxSlope(), routeData.getMinSlope());

			distance = 0.0;
			rPower = new Rolling(10);
			break;
		}
	}
}