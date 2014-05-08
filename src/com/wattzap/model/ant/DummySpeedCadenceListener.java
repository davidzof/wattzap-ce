package com.wattzap.model.ant;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.swing.JOptionPane;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.wattzap.controller.MessageBus;
import com.wattzap.controller.MessageCallback;
import com.wattzap.controller.Messages;
import com.wattzap.model.RLVReader;
import com.wattzap.model.RouteReader;
import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.Point;
import com.wattzap.model.dto.Telemetry;
import com.wattzap.model.power.Power;
import com.wattzap.utils.Rolling;

/**
 * (c) 2013 David George / TrainingLoops.com
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

	RouteReader routeData = null;
	Rolling rPower;

	double mass;
	double wheelSize;
	int resistance;
	Power power;
	boolean virtualPower;
	private double levels = 1;

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
				// ((UserPreferences.INSTANCE.getMaxPower() + (Math
				// .random() * 10)) * 0.5);

				int powerWatts = (int) (UserPreferences.INSTANCE.getMaxPower() * 0.5);

				double speed = 0;
				// if ant disabled always use this calculation
				if ((virtualPower || !UserPreferences.INSTANCE.isAntEnabled())
						&& routeData != null) {
					if (routeData.routeType() == RLVReader.SLOPE) {
						p = routeData.getPoint(distance);

						/*
						 * increase power for hills to FTP
						 */
						powerWatts += powerWatts
								* (p.getGradient() / routeData.getMaxSlope());

						if (UserPreferences.INSTANCE.getResistance() == 0) {
							int r = power.getResistance(p.getGradient());
							t.setResistance(r);
							speed = power.getSpeed(powerWatts, r);
						} else {
							// speed corresponding to this power
							speed = power.getSpeed(powerWatts,
									UserPreferences.INSTANCE.getResistance());
						}

						BigDecimal bd = new BigDecimal(speed).setScale(2,
								RoundingMode.HALF_UP);

						t.setVirtualSpeed(bd.intValue());

						// we need to now calculate video speed but we need to
						// display real speed
						// what would be our real speed for those watts - show
						// in odo
						double realSpeed = power.getRealSpeed(mass,
								p.getGradient() / 100, powerWatts);
						speed = (realSpeed * 3600) / 1000;
					} else {
						p = routeData.getPoint(distance);
						// power comes from video (gradient)
						//powerWatts = (int) ((p.getGradient()) + (Math.random() * 4));
						powerWatts = (int) p.getGradient();
						
						// apply some smoothing
						rPower.add(powerWatts);
						powerWatts = (int) rPower.getAverage();

						if (UserPreferences.INSTANCE.getResistance() == 0) {
							t.setResistance(1);
							speed = power.getSpeed(powerWatts, 1);
						} else {
							// speed corresponding to this power
							speed = power.getSpeed(powerWatts,
									UserPreferences.INSTANCE.getResistance());
							BigDecimal bd = new BigDecimal(speed).setScale(2,
									RoundingMode.HALF_UP);

							t.setVirtualSpeed(bd.intValue());
							t.setResistance(UserPreferences.INSTANCE.getResistance());
						}
					}
				} else {
					speed = power.getRealSpeed(mass, 0, powerWatts) * 3.6;
				}

				//t.setHeartRate((int) (110 + (powerWatts * 60 / 400)));
				// t.setCadence((int) (60 + ((powerWatts * 40 / 300))));
				t.setPower(powerWatts);

				if (routeData != null) {
					p = routeData.getPoint(distance);
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
				t.setDistance(distance);

				t.setTime(System.currentTimeMillis());

				// notifyListeners(t);
				MessageBus.INSTANCE.send(Messages.SPEEDCADENCE, t);

				// d = s * t
				distance += (speed / 3600) * 0.25;
			}
		}

	}

	public void callback(Messages message, Object o) {
		switch (message) {

		case START:
			// get uptodate values
			mass = UserPreferences.INSTANCE.getTotalWeight();
			wheelSize = UserPreferences.INSTANCE.getWheelSizeCM();
			resistance = UserPreferences.INSTANCE.getResistance();
			power = UserPreferences.INSTANCE.getPowerProfile();
			virtualPower = UserPreferences.INSTANCE.isVirtualPower();
			if (this.getState() == Thread.State.NEW ) {
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
			if (!UserPreferences.INSTANCE.isRegistered()
					&& (UserPreferences.INSTANCE.getEvalTime()) <= 0) {
				logger.info("Out of time " + UserPreferences.INSTANCE.getEvalTime());
				UserPreferences.INSTANCE.shutDown();
				System.exit(0);
			}
			
			this.routeData = (RouteReader) o;
			power = UserPreferences.INSTANCE.getPowerProfile();
			power.setGrades(routeData.getMaxSlope(), routeData.getMinSlope());

			distance = 0.0;
			rPower = new Rolling(10);
			break;
		}
	}
}