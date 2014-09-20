package com.wattzap.model.ant;

import org.cowboycoders.ant.events.BroadcastListener;
import org.cowboycoders.ant.messages.data.BroadcastDataMessage;

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
 * (c) 2013 David George / Wattzap.com
 * 
 * Speed and Cadence ANT+ processor.
 * 
 * @author David George
 * @date 11 June 2013
 */
public class AdvancedSpeedCadenceListener extends SpeedCadenceListener
		implements BroadcastListener<BroadcastDataMessage>, MessageCallback {

	private static int lastTs = -1;
	private static int lastTc = -1;
	private static int sRR = 0; // previous speed rotation measurement
	private static int cRR = 0; // previous cadence rotation measurement
	private static int sCount = 0;
	private static int cCount = 0;
	private static long elapsedTime;
	Telemetry oldT = null;
	//
	private double distance = 0.0;
	private int cadence;

	RouteReader routeData;
	private double mass;
	private final UserPreferences userPrefs = UserPreferences.INSTANCE;

	// initialize for pairing
	private double wheelSize = userPrefs.getWheelSizeCM();
	private int resistance = userPrefs.getResistance();
	private Power power = userPrefs.getPowerProfile();
	private boolean simulSpeed;
	private boolean initializing;

	public AdvancedSpeedCadenceListener() {
		MessageBus.INSTANCE.register(Messages.START, this);
		MessageBus.INSTANCE.register(Messages.STARTPOS, this);
		MessageBus.INSTANCE.register(Messages.GPXLOAD, this);
	}

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

		//System.out.println("tC " + tC + " cR " + cR + " tS " + tS + " sR " + sR
		//		+ " lastTs " + lastTs + " lastTc " + lastTc + " sRR " + sRR
		//		+ " cRR " + cRR);

		if (lastTs == -1) {
			// first time through, initialize counters and return
			//System.out.println("initialize counters and return");
			lastTs = tS;
			lastTc = tC;
			sRR = sR;
			cRR = cR;
			initializing = true;
			return;
		}
		if (initializing) {
			// in intial phase we reset counters when they change for first time
			if (tS != lastTs && tC != lastTc) {
				lastTs = tS;
				lastTc = tC;
				sRR = sR;
				cRR = cR;
				initializing = false;
			}
		}

		int tD; // time delta
		if (tS < lastTs) {
			// we have rolled over
			//System.out.println("rollover");
			tD = tS + (65536 - lastTs);
			if (tD > 5000) {
				// Time delta more than 5 seconds is almost certainly bogus,
				// just drop it
				return;
			}
		} else {
			tD = tS - lastTs;
		}

		int sRD; // speed rotation delta
		if (sR < sRR) {
			// we have rolled over
			sRD = sR + (65536 - sRR);
		} else {
			sRD = sR - sRR;
		}

		//System.out.println(" sRD " + sRD + " tD " + tD);
		double distanceKM = 0;
		if (tD > 0) {
			// We have a time value and rotation value, lets calculate the
			// speed
			distanceKM = (sRD * wheelSize) / 100000;
			double timeS = ((double) tD) / 1024;
			elapsedTime += (int) (timeS * 1000);

			double speed = distanceKM / (timeS / (3600));
			int powerWatts = power.getPower(speed, resistance);
			// System.out.println("Speed " + speed + " distanceKM " + distanceKM
			// + " timeS " + timeS);

			t.setPower(powerWatts);

			// if we have GPX Data and Simulspeed is enabled calculate speed
			// based on power and gradient using magic sauce
			if (simulSpeed && routeData != null) {
				// System.out.println("gettng point at distance " + distance);
				Point p = routeData.getPoint(distance);
				if (routeData.routeType() == RLVReader.SLOPE) {
					if (p == null) {
						// end of the road
						distance = 0.0;
						return;
					}
					double realSpeed = power.getRealSpeed(mass,
							p.getGradient() / 100, powerWatts);
					realSpeed = (realSpeed * 3600) / 1000;
					distanceKM = (realSpeed / speed) * distanceKM;
					speed = realSpeed;

				} else {
					double ratio = (powerWatts / p.getGradient());
					// speed is video speed * power ratio
					speed = p.getSpeed() * ratio;
					distanceKM = (speed / 3600) * timeS;

					// System.out.println("speed " + speed + " powerWatts "
					// + powerWatts + " video Power " + p.getGradient() +
					// " distanceKM " + distanceKM);
				}
			}

			t.setSpeed(speed);

			sCount = 0;
		} else if (sCount < 6) {
			// speed reading is zero, ignore the first 12 of these as sometimes
			// readings don't change with every message
			sCount++;
			// System.out.println("ACSL sCount " + sCount);
			t.setSpeed(-1.0);
		}

		t.setDistance(distance);
		if (routeData != null) {
			Point p = routeData.getPoint(t.getDistance());
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
		t.setTime(elapsedTime);
		//t.setHeartRate(HeartRateListener.heartRate);
		t.setCadence(cadence);

		/*
		 * Cadence caculations
		 */
		int cTD; // cadence time delta
		if (tC < lastTc) {
			// we have rolled over
			cTD = tC + (65536 - lastTc);
			//System.out.println("rollover ctD " + cTD);
		} else {
			cTD = tC - lastTc;
		}

		//System.out.println(" cR " + cR + " tC " + tC + " cTD " + cTD);
		if (cTD < 5000) {
			// Time deltas of > 5 seconds are bogus
			int cRD; // cadence rotation delta
			if (cR < cRR) {
				// we have rolled over
				cRD = cR + (65536 - cRR);
			} else {
				cRD = cR - cRR;
			}

			if (cRD > 0) {
				double timeC = ((double) cTD) / 1024.0;
				//System.out.println("timeC" + timeC + " cRD " + cRD);
				cadence = ((int) (cRD * ((1 / timeC) * 60.0)));
				cCount = 0;
			} else if (cCount < 6) {
				//System.out.println("ACSL cCount " + cCount);
				cCount++;
			} else {
				cadence = 0;
			}
		}

		lastTs = tS;
		sRR = sR;
		if (tC < lastTc || cTD < 5000) {
			// no rollover or delta less than 5000.
			lastTc = tC;
			cRR = cR;
		}

		if (t.getSpeed() >= 0.0) {
			// some sanity checks

			if (cadence > 250 || t.getPower() > 2500) {
				//System.out.println("Bogosity!!!! > " + t);
				return;
			}

			distance += distanceKM;
			//System.out.println(t);
			MessageBus.INSTANCE.send(Messages.SPEEDCADENCE, t);

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
			elapsedTime = System.currentTimeMillis();
			lastTs = -1;
			lastTc = -1;
			initializing = false;
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
