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

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cowboycoders.ant.events.BroadcastListener;
import org.cowboycoders.ant.messages.data.BroadcastDataMessage;

import com.wattzap.model.dto.Telemetry;

/**
 * (c) 2013 David George
 * 
 * Speed and Cadence ANT+ processor.
 * 
 * @author David George
 * @date 11 June 2013
 */
public class SpeedCadenceListener implements
		BroadcastListener<BroadcastDataMessage> {
	private List<ChangeListener> listeners = new ArrayList<ChangeListener>();

	static int lastTs = 0;
	static int lastTc = 0;
	static int sRR = 0; // previous speed rotation measurement
	static int cRR = 0; // previous cadence rotation measurement
	private static int sCount = 0;
	private static int cCount = 0;
	//
	private double distance = 0.0;
	private int cadence;

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

		// System.out
		// .println("tC " + tC + " cR " + cR + " tS " + tS + " sR " + sR);

		if (lastTs == 0 || lastTc == 0) {
			// first time through, initialize counters and return
			lastTs = tS;
			lastTc = tC;
			sRR = sR;
			cRR = cR;
			return;
		}

		int tD; // time delta
		if (tS < lastTs) {
			// we have rolled over
			tD = tS + (65536 - lastTs);
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

		if (tD > 0) {
			double distanceKM = (sRD * 2013) / 100000;
			double timeS = ((double) tD) / 1024.0;
			t.setTime(t.getTime() + (int) (timeS * 1000));
			double speed = distanceKM / (timeS / (60.0 * 60.0));

			distance += distanceKM;
			
			t.setSpeed(speed);
			t.setDistanceMeters(distance * 1000);
			t.setCadence(cadence);

			sCount = 0;
		} else if (sCount < 12) {
			sCount++;
		}

		int cTD; // cadence time delta
		if (tC < lastTc) {
			// we have rolled over
			cTD = tC + (65536 - lastTc);
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
			cadence = ((int) (cRD * ((1 / timeC) * 60.0)));
			cCount = 0;
		} else if (cCount < 12) {
			cCount++;
		}

		lastTs = tS;
		lastTc = tC;
		cRR = cR;
		sRR = sR;
		
		if (t.getSpeedKMH() >= 0.0) {
			notifyListeners(t);
		}
	}
	
	void notifyListeners(Telemetry t) {
		ChangeEvent event = new ChangeEvent(t);

		for (ChangeListener l : listeners) {
			l.stateChanged(event);
		}
	}

	public void addChangeListener(ChangeListener l) {
		listeners.add(l);
	}
}