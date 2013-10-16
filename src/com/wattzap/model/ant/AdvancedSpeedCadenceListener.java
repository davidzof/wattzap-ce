package com.wattzap.model.ant;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cowboycoders.ant.events.BroadcastListener;
import org.cowboycoders.ant.messages.data.BroadcastDataMessage;

import com.wattzap.model.GPXData;
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
public class AdvancedSpeedCadenceListener extends SpeedCadenceListener
		implements BroadcastListener<BroadcastDataMessage>, ActionListener {
	private List<ChangeListener> listeners = new ArrayList<ChangeListener>();

	private static int lastTs = 0;
	private static int lastTc = 0;
	private static int sRR = 0; // previous speed rotation measurement
	private static int cRR = 0; // previous cadence rotation measurement
	private static int sCount = 0;
	private static int cCount = 0;
	private static long elapsedTime;
	//
	private double distance = 0.0;
	private int cadence;

	GPXData gpxData;
	private double mass;
	private double wheelSize;
	private int resistance;
	private Power power;
	private boolean virtualPower;

	/**
	 * Speed and cadence data is contained in the 8 byte data payload in the
	 * message. Speed and Cadence have the same format. A short integer giving
	 * time since the last reading and a short integer giving the number of
	 * revolutions since the last reading.
	 * <p>
	 * The format is:<br/>
	 * [0][1] - Cadence timing<br/>
	 * [2][3] - Cadence revolutions<br/>
	 * [4][5] - Speed timing<br/>
	 * [6][7] - Speed revolutions<br/>
	 * <p>
	 * Values are little Endian (MSB byte is on the right)
	 * <p>
	 * So for timing: [0] + ([1] << 8) / 1024 gives the time in milliseconds
	 * since the last rollover. Note that you have to account for rollovers of
	 * both time and rotations which happen every 16 seconds/16384 revolutions.
	 * <p>
	 * There is another wrinkle. Messages are sent at at 4Hz rate. Below a
	 * certain rate (240rpm) we will see messages with the same number of
	 * rotations. This doesn't mean the wheel is stopped, just there was no new
	 * data since the last reading. To distinguish this from a stopped wheel a
	 * certain number of same value readings are ignored for speed or cadence
	 * updates.
	 */
	@Override
	public void receiveMessage(BroadcastDataMessage message) {
		// super.receiveMessage(message);

		int[] data = message.getUnsignedData();
		Telemetry t = new Telemetry();

		// Bytes 0 and 1: TTTT / 1024 = milliSeconds since the last
		// rollover for cadence
		int tC = data[0] + (data[1] << 8);

		// Bytes 2 and 3: Cadence rotation Count
		int cR = data[2] + (data[3] << 8);

		// Bytes 4 and 5: TTTT / 1024 = milliSeconds since the last
		// rollover for speed
		int tS = data[4] + (data[5] << 8);

		// Bytes 6 and 7: speed rotation count.
		int sR = data[6] + (data[7] << 8);

		System.out
				.println("tC " + tC + " cR " + cR + " tS " + tS + " sR " + sR);

		if (lastTs == 0 || lastTc == 0) {
			// first time through, initialize counters and return
			lastTs = tS;
			lastTc = tC;
			sRR = sR;
			cRR = cR;
			elapsedTime = System.currentTimeMillis();
			return;
		}

		int tD; // time delta
		if (tS < lastTs) {
			// we have rolled over

			tD = tS + (65536 - lastTs);

			System.out.println("rollover tD " + tD);
			if (tD > 10000) {
				// Time delta more than 10 seconds is almost certainly bogus,
				// update lastTs and return
				lastTs = tS; // reset lastTs
				return;
			}
			// System.out.println("*** Roll over tS " + tS + " lastTs " + lastTs
			// + " td " + tD);
		} else {
			tD = tS - lastTs;
			// System.out.println("no roll over tS " + tS + " lastTs " + lastTs
			// + " td " + tD);
			System.out.println("no rollover tD " + tD);
		}

		int sRD; // speed rotation delta
		if (sR < sRR) {
			// we have rolled over
			sRD = sR + (65536 - sRR);
			// System.out.println("+++ Roll over sR " + sR + " sRR " + sRR);
		} else {
			sRD = sR - sRR;
			// System.out.println(" sR " + sR + " sRR " + sRR);
		}

		if (tD > 0) {
			// ok we have a time value and rotation value, lets calculate the
			// speed

			// System.out.println(" sRD " + sRD + " wheelSize " + wheelSize);
			double distanceKM = (sRD * wheelSize) / 100000;
			double timeS = ((double) tD) / 1024.0;
			elapsedTime += (int) (timeS * 1000);
			t.setTime(elapsedTime);

			double speed = distanceKM / (timeS / (60.0 * 60.0));
			int powerWatts = power.getPower(speed, resistance);
			t.setPower(powerWatts);

			// if we have GPX Data and Simulspeed is enabled calculate speed
			// based on power and gradient using magic sauce
			if (virtualPower && gpxData != null) {
				Point p = gpxData.getCoords(distance);
				double realSpeed = power.getRealSpeed(mass,
						p.getGradient() / 100, powerWatts);
				realSpeed = (realSpeed * 3600) / 1000;
				distanceKM = (realSpeed / speed) * distanceKM;
				speed = realSpeed;
			}
			t.setSpeed(speed);
			distance += distanceKM;
			t.setDistance(distance);
			if (gpxData != null) {
				Point p = gpxData.getCoords(t.getDistance());
				t.setElevation(p.getElevation());
				t.setGradient(p.getGradient());
				t.setLatitude(p.getLatitude());
				t.setLongitude(p.getLongitude());
			}
			t.setHeartRate(HeartRateListener.heartRate);
			t.setCadence(cadence);

			sCount = 0;
		} else if (sCount < 12) {
			sCount++;
			System.out.println("ACSL sCount " + sCount);
			t.setSpeed(-1.0);
		}

		int cTD; // cadence time delta
		if (tC < lastTc) {
			// we have rolled over
			cTD = tC + (65536 - lastTc);
			
			System.out.println("rollover ctD " + cTD);
		} else {
			cTD = tC - lastTc;
		}

		int cRD; // cadence rotation delta
		if (cR < cRR) {
			// we have rolled over
			cRD = cR + (65536 - cRR);
		} else {
			cRD = cR - cRR;
		}

		if (cRD > 0) {
			double timeC = ((double) cTD) / 1024.0;
			System.out.println("timeC" + timeC);
			cadence = ((int) (cRD * ((1 / timeC) * 60.0)));
			cCount = 0;
		} else if (cCount < 12) {
			// System.out.println("ACSL cCount " + cCount);
			cCount++;
		}

		lastTs = tS;
		lastTc = tC;
		cRR = cR;
		sRR = sR;

		if (t.getSpeed() >= 0.0) {
			notifyListeners(t);
		}
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		// System.out.println("ASCL " + command);
		if ("gpxload".equals(command)) {
			this.gpxData = (GPXData) e.getSource();
			distance = 0.0;
		}
		if ("stop".equals(command)) {

		} else if ("start".equals(command)) {
			// get up to date values
			mass = UserPreferences.INSTANCE.getTotalWeight();
			wheelSize = UserPreferences.INSTANCE.getWheelSizeCM();
			resistance = UserPreferences.INSTANCE.getResistance();
			power = UserPreferences.INSTANCE.getPowerProfile();
			virtualPower = UserPreferences.INSTANCE.isVirtualPower();
		}
	}
}
