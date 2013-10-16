package com.wattzap.model.ant;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.wattzap.model.GPXData;
import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.Point;
import com.wattzap.model.dto.Telemetry;
import com.wattzap.model.power.Power;

public class DummySpeedCadenceListener extends Thread implements ActionListener {
	private List<ChangeListener> listeners = new ArrayList<ChangeListener>();
	private boolean running = true;
	private double distance = 0.0;

	GPXData gpxData = null;

	double mass;
	double wheelSize;
	int resistance;
	Power power;
	boolean virtualPower;

	private static Logger logger = LogManager
			.getLogger(DummySpeedCadenceListener.class.getName());

	public void run() {
		while (true) {
			if (running) {
				Point p;

				Telemetry t = new Telemetry();
				double speed = Math.random() + 30.0;
				int powerWatts = power.getPower(speed, resistance);

				t.setPower(powerWatts);

				if (virtualPower && gpxData != null) {
					p = gpxData.getCoords(distance);
					double realSpeed = power.getRealSpeed(mass,
							p.getGradient() / 100, powerWatts);
					speed = (realSpeed * 3600) / 1000;

					// get real point based on virtual power

				}
				// d = s * t
				distance += (speed/3600) * 0.25;
				
				if (gpxData != null) {
					p = gpxData.getCoords(distance);
					t.setElevation(p.getElevation());
					t.setGradient(p.getGradient());
					t.setLatitude(p.getLatitude());
					t.setLongitude(p.getLongitude());
				}

				t.setSpeed(speed);
				t.setDistance(distance);
				t.setCadence((int) speed * 6);
				t.setHeartRate((int) speed * 3);
				t.setTime(System.currentTimeMillis());

				notifyListeners(t);
			}
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void notifyListeners(Telemetry t) {
		for (ChangeListener l : listeners) {
			l.stateChanged(new ChangeEvent(t));
		}
	}

	public void addChangeListener(ChangeListener l) {
		listeners.add(l);
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		logger.debug(command);
		if ("stop".equals(command)) {
			running = false;
		} else if ("start".equals(command)) {
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

		} else if ("gpxload".equals(command)) {
			this.gpxData = (GPXData) e.getSource();
			distance = 0.0;
		}

	}
}