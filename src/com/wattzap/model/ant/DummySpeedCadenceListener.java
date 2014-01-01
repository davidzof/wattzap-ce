package com.wattzap.model.ant;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

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
				double random = Math.random();
				double speed = random + 30.0;
				int powerWatts = power.getPower(speed, resistance);

				t.setPower(powerWatts);

				if (virtualPower && routeData != null) {
					if (routeData.routeType() == RLVReader.SLOPE) {
						p = routeData.getPoint(distance);
						// TODO NPE
						double realSpeed = power.getRealSpeed(mass,
								p.getGradient() / 100, powerWatts);
						speed = (realSpeed * 3600) / 1000;
					} else {
						p = routeData.getPoint(distance);
						speed = p.getSpeed() * (powerWatts/p.getGradient());
						System.out.println("distance " + distance + " video Power " + p.getGradient());
					}
				}


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
				

				t.setSpeed(speed);
				t.setDistance(distance);
				t.setCadence((int) (random + speed * 2.0));
				t.setHeartRate((int) (speed * 4.0 + random));
				t.setTime(System.currentTimeMillis());

				// notifyListeners(t);
				MessageBus.INSTANCE.send(Messages.SPEEDCADENCE, t);
				
				// d = s * t
				distance += (speed / 3600) * 0.25;
				}
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
			if (running == true) {
				start();
			} else {
				running = true;
			}
			break;
		case STOP:
			running = false;
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
}