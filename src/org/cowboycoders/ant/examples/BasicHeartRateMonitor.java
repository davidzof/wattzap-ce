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


import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

import org.cowboycoders.ant.Channel;
import org.cowboycoders.ant.NetworkKey;
import org.cowboycoders.ant.Node;
import org.cowboycoders.ant.events.BroadcastListener;
import org.cowboycoders.ant.events.MessageCondition;
import org.cowboycoders.ant.events.MessageConditionFactory;
import org.cowboycoders.ant.interfaces.AntTransceiver;
import org.cowboycoders.ant.messages.ChannelType;
import org.cowboycoders.ant.messages.SlaveChannelType;
import org.cowboycoders.ant.messages.commands.ChannelRequestMessage;
import org.cowboycoders.ant.messages.commands.ChannelRequestMessage.Request;
import org.cowboycoders.ant.messages.data.BroadcastDataMessage;
import org.cowboycoders.ant.messages.responses.ChannelIdResponse;

class Listener implements BroadcastListener<BroadcastDataMessage> {

	/*
	 * Once an instance of this class is registered with a channel, this is
	 * called every time a broadcast message is received on that channel.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.cowboycoders.ant.events.BroadcastListener#receiveMessage(java.lang
	 * .Object)
	 */
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
		System.out.println("Heart rate: " + message.getUnsignedData()[7]);
	}

}

public class BasicHeartRateMonitor {

	/*
	 * See ANT+ data sheet for explanation
	 */
	private static final int HRM_CHANNEL_PERIOD = 8070;

	/*
	 * See ANT+ data sheet for explanation
	 */
	private static final int HRM_CHANNEL_FREQ = 57;

	/*
	 * This should match the device you are connecting with. Some devices are
	 * put into pairing mode (which sets this bit).
	 * 
	 * Note: Many ANT+ sport devices do not set this bit (eg. HRM strap).
	 * 
	 * See ANT+ docs.
	 */
	private static final boolean HRM_PAIRING_FLAG = false;

	/*
	 * Should match device transmission id (0-255). Special rules apply for
	 * shared channels. See ANT+ protocol.
	 * 
	 * 0: wildcard, matches any value (slave only)
	 */
	private static final int HRM_TRANSMISSION_TYPE = 0;

	/*
	 * device type for ANT+ heart rate monitor
	 */
	private static final int HRM_DEVICE_TYPE = 120;

	/*
	 * You should make a note of the device id and use it in preference to the
	 * wild card to pair to a specific device.
	 * 
	 * 0: wild card, matches all device ids any other number: match specific
	 * device id
	 */
	private static final int HRM_DEVICE_ID = 0;

	public static final Level LOG_LEVEL = Level.ALL;

	public static void setupLogging() {
		// set logging level
		AntTransceiver.LOGGER.setLevel(LOG_LEVEL);
		ConsoleHandler handler = new ConsoleHandler();
		// PUBLISH this level
		handler.setLevel(LOG_LEVEL);
		AntTransceiver.LOGGER.addHandler(handler);
		// Don't duplicate messages by sending to parent handler as well
		AntTransceiver.LOGGER.setUseParentHandlers(false);
	}

	public static void printChannelConfig(Channel channel) {

		// build request
		ChannelRequestMessage msg = new ChannelRequestMessage(
				channel.getNumber(), Request.CHANNEL_ID);

		// response should be an instance of ChannelIdResponse
		MessageCondition condition = MessageConditionFactory
				.newInstanceOfCondition(ChannelIdResponse.class);

		try {

			// send request (blocks until reply received or timeout expired)
			ChannelIdResponse response = (ChannelIdResponse) channel
					.sendAndWaitForMessage(msg, condition, 5L,
							TimeUnit.SECONDS, null);

			System.out.println();
			System.out.println("Device configuration: ");
			System.out.println("deviceID: " + response.getDeviceNumber());
			System.out.println("deviceType: " + response.getDeviceType());
			System.out.println("transmissionType: "
					+ response.getTransmissionType());
			System.out.println("pairing flag set: "
					+ response.isPairingFlagSet());
			System.out.println();

		} catch (Exception e) {
			// not critical, so just print error
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws InterruptedException {

		// optional: enable console logging with Level = LOG_LEVEL
		setupLogging();

		/*
		 * Choose driver: AndroidAntTransceiver or AntTransceiver
		 * 
		 * AntTransceiver(int deviceNumber) deviceNumber : 0 ... number of usb
		 * sticks plugged in 0: first usb ant-stick
		 */
		AntTransceiver antchip = new AntTransceiver(0);

		// initialises node with chosen driver
		Node node = new Node(antchip);

		// ANT+ key
		NetworkKey key = new NetworkKey(0xB9, 0xA5, 0x21, 0xFB, 0xBD, 0x72,
				0xC3, 0x45);
		key.setName("N:ANT+");

		/* must be called before any configuration takes place */
		node.start();

		/* sends reset request : resets channels to default state */
		node.reset();

		// sets network key of network zero
		node.setNetworkKey(0, key);

		Channel channel = node.getFreeChannel();

		// Arbitrary name : useful for identifying channel
		channel.setName("C:HRM");

		// choose slave or master type. Constructors exist to set
		// two-way/one-way and shared/non-shared variants.
		ChannelType channelType = new SlaveChannelType();

		// use ant network key "N:ANT+"
		channel.assign("N:ANT+", channelType);

		// registers an instance of our callback with the channel
		channel.registerRxListener(new Listener(), BroadcastDataMessage.class);

		/******* start device specific configuration ******/

		channel.setId(HRM_DEVICE_ID, HRM_DEVICE_TYPE, HRM_TRANSMISSION_TYPE,
				HRM_PAIRING_FLAG);

		channel.setFrequency(HRM_CHANNEL_FREQ);

		channel.setPeriod(HRM_CHANNEL_PERIOD);

		/******* end device specific configuration ******/

		// timeout before we give up looking for device
		channel.setSearchTimeout(Channel.SEARCH_TIMEOUT_NEVER);

		// start listening
		channel.open();

		printChannelConfig(channel);
		// Listen for 60 seconds
		Thread.sleep(60000);

		// stop listening
		channel.close();

		// optional : demo requesting of channel configuration. If device
		// connected
		// this will reflect actual device id, transmission type etc. This info
		// will allow
		// you to only connect to this device in the future.
		printChannelConfig(channel);

		// resets channel configuration
		channel.unassign();

		// return the channel to the pool of available channels
		node.freeChannel(channel);

		// cleans up : gives up control of usb device etc.
		node.stop();

	}

}