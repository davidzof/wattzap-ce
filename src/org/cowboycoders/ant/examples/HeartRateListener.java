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
package org.cowboycoders.ant.examples;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cowboycoders.ant.events.BroadcastListener;
import org.cowboycoders.ant.messages.data.BroadcastDataMessage;

/**
 * Heart Rate ANT+ processor.
 * 
 * @author David George
 * @date 11 June 2013
 */
public class HeartRateListener implements
		BroadcastListener<BroadcastDataMessage> {
	public static int heartRate = 0;

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
		int rate = message.getUnsignedData()[7];
		if (rate > 0 || rate < 220) {
			heartRate = rate;
			System.out.println("HR " + heartRate);
		}
	}
}