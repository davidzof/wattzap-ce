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
import com.wattzap.controller.Messages;
import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.Telemetry;

/**
 * (c) 2013 David George / Wattzap.com
 * 
 * Speed and Cadence ANT+ processor.
 * 
 * @author David George
 * @date 11 June 2013
 */
public class AdvancedSpeedCadenceListener extends AntListener {
	public static final String name = "C:SC";
	private static final int ANT_SPORT_SandC_TYPE = 121; // 0x79
	private static final int ANT_SPORT_SPEED_PERIOD = 8086;
	private final static Logger logger = LogManager.getLogger("ASCL");
	private final SpeedListener speedListener;
	private final CadenceListener cadenceListener;

	public AdvancedSpeedCadenceListener() {
		speedListener = new SpeedListener();
		cadenceListener = new CadenceListener();
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
	 * 
	 * FIX: convert distance to meters
	 */
	@Override
	public void receiveMessage(BroadcastDataMessage message) {
		int[] data = message.getUnsignedData();

		// Bytes 0 and 1: TTTT / 1024 = milliSeconds since the last
		// rollover for cadence
		int tC = data[0] + (data[1] << 8);

		// Bytes 2 and 3: Cadence rotation Count
		int cR = data[2] + (data[3] << 8);

		// Bytes 4 and 5: TTTT / 1024 = milliSeconds since the last
		// rollover for speed
		int tS = data[4] | (data[5] << 8);
		
		// Bytes 6 and 7: speed rotation count.
		int sR = data[6] | (data[7] << 8);

		logger.debug("tC " + tC + " cR " + cR + " tS " + tS + " sR " + sR);

		Telemetry t = speedListener.getTelemetry(tS, sR);
		if (t != null) {
			MessageBus.INSTANCE.send(Messages.SPEED, t);
		}

		int cadence = cadenceListener.getCadence(tC, cR);
		if (cadence < 0 || cadence > 250) {
			return; // bogus value
		}
		MessageBus.INSTANCE.send(Messages.CADENCE, cadence);

	}

	@Override
	public int getChannelId() {
		return UserPreferences.INSTANCE.getSCId();
	}

	@Override
	public int getChannelPeriod() {
		return ANT_SPORT_SPEED_PERIOD;
	}

	@Override
	public int getDeviceType() {
		return ANT_SPORT_SandC_TYPE;
	}

	@Override
	public String getName() {
		return name;
	}
}
