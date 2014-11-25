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
 * Power Meter
 * 
 * @author David George
 * @date 14th November 2014
 * 
 * (c) 2014 David George / Wattzap.com
 */
public class PowerListener extends AntListener {
	public static String name = "C:POW";
	private static final byte DEVICE_TYPE = (byte) 11; // 0x7A
	private static final short MESSAGE_PERIOD = 8182;

	@Override
	public void receiveMessage(BroadcastDataMessage message) {
		if (message.getUnsignedData()[0] == 16) {
			// simple power message
			int watt = (message.getUnsignedData()[7] << 8)
					| message.getUnsignedData()[6];
			int rpm = (byte) (message.getUnsignedData()[3] & 0xFF);
			System.out.println("rev count " + watt + " evTime " + rpm + " "
					+ message.getUnsignedData()[0]);
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