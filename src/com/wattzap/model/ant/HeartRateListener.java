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

/**
 * Heart Rate ANT+ processor.
 * 
 * @author David George
 * @date 11 June 2013
 * 
 *       (c) 2013, 2014 David George / Wattzap.com
 */
public class HeartRateListener extends AntListener {
	public static String name = "C:HRM";
	private static final int HRM_CHANNEL_PERIOD = 8070;
	private static final int HRM_DEVICE_TYPE = 120; // 0x78
	private int lastHeartRate = 0;

	@Override
	public void receiveMessage(BroadcastDataMessage message) {

		/*
		 * getData() returns the 8 byte payload. The current heart rate is
		 * contained in the last byte.
		 * 
		 * Note: remember the lack of unsigned bytes in java, so unsigned values
		 * should be converted to ints for any arithmetic / display -
		 * getUnsignedData() is a utility method to do this.
		 */
		int heartRate = message.getUnsignedData()[7];

		if (heartRate > 0 || heartRate < 220) {
			if (heartRate == lastHeartRate) {
				return; // don't bother sending repeating values
			}
			lastHeartRate = heartRate;
			MessageBus.INSTANCE.send(Messages.HEARTRATE, heartRate);
		}
	}

	@Override
	public int getChannelId() {
		return UserPreferences.INSTANCE.getHRMId();
	}

	@Override
	public int getChannelPeriod() {
		return HRM_CHANNEL_PERIOD;
	}

	@Override
	public int getDeviceType() {
		return HRM_DEVICE_TYPE;
	}

	@Override
	public String getName() {
		return name;
	}
}