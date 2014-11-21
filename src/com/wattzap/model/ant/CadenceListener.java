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
import com.wattzap.controller.Messages;
import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.Telemetry;

/**
 * Cadence Sensor
 * 
 * @author David George
 * @date 14th November 2014
 * 
 *       http://chet0xhenry-antplus.googlecode.com/hg/MyTracks/src/com/google/
 *       android/apps/mytracks/services/sensors/ant/CadenceCounter.java
 */
public class CadenceListener extends AntListener {
	public static String name = "C:CAD";
	private static final byte DEVICE_TYPE = (byte) 0x7A;
	private static final short MESSAGE_PERIOD = 8102;
	private int lastCount = -1;
	private int lastTime = -1;
	private int cCount = 0;

	@Override
	public void receiveMessage(BroadcastDataMessage message) {
		int time = (message.getUnsignedData()[5] << 8)
				| message.getUnsignedData()[4];

		int count = (message.getUnsignedData()[7] << 8)
				| message.getUnsignedData()[6];

		if (lastCount == -1) {
			// first time thru, set
			lastCount = count;
			lastTime = time;
			return;
		}
		
		int tDiff = ((time - lastTime) & 0xffff);
		int cDiff = ((count - lastCount) &0xffff);
		
		int cadence = 0;
		if (tDiff > 0) {
			cadence = (60 * 1024 * cDiff) / tDiff;
			cCount = 0;
		} else if (cCount < 6) {
			cCount++;
			return;
		}

		lastTime = time;
		lastCount = count;
		
		Telemetry t = new Telemetry();
		t.setCadence(cadence);
		MessageBus.INSTANCE.send(Messages.CADENCE, t);
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