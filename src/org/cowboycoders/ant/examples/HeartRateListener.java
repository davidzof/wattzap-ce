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