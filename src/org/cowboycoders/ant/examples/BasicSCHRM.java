package org.cowboycoders.ant.examples;

import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

import org.cowboycoders.ant.Channel;
import org.cowboycoders.ant.NetworkKey;
import org.cowboycoders.ant.Node;
import org.cowboycoders.ant.events.MessageCondition;
import org.cowboycoders.ant.events.MessageConditionFactory;
import org.cowboycoders.ant.interfaces.AntTransceiver;
import org.cowboycoders.ant.messages.ChannelType;
import org.cowboycoders.ant.messages.SlaveChannelType;
import org.cowboycoders.ant.messages.commands.ChannelRequestMessage;
import org.cowboycoders.ant.messages.commands.ChannelRequestMessage.Request;
import org.cowboycoders.ant.messages.data.BroadcastDataMessage;
import org.cowboycoders.ant.messages.responses.ChannelIdResponse;

/**
 * Gets data from Ant device and calculates speed, distance, cadence etc.
 * 
 * @author David George
 * @date 30 July 2013
 */
public class BasicSCHRM {
	private static Channel channel = null;
	private static Node node = null;
	private static NetworkKey key = null;

	private static final int HRM_CHANNEL_PERIOD = 8070;
	private static final int ANT_SPORT_SPEED_PERIOD = 8086;
	private static final int ANT_SPORT_FREQ = 57; // 0x39

	/*
	 * This should match the device you are connecting with. Some devices are
	 * put into pairing mode (which sets this bit).
	 * 
	 * Note: Many ANT+ sport devices do not set this bit (eg. HRM strap).
	 * 
	 * See ANT+ docs.
	 */
	private static final boolean PAIRING_FLAG = false;

	/*
	 * Should match device transmission id (0-255). Special rules apply for
	 * shared channels. See ANT+ protocol.
	 * 
	 * 0: wildcard, matches any value (slave only)
	 */
	private static final int HRM_TRANSMISSION_TYPE = 1;

	/*
	 * Device type, see ANT+ specs
	 */
	private static final int HRM_DEVICE_TYPE = 120; // 0x78
	private static final int ANT_SPORT_SandC_TYPE = 121; // 0x78

	/*
	 * You should make a note of the device id and use it in preference to the
	 * wild card to pair to a specific device.
	 * 
	 * 0: wild card, matches all device ids any other number: match specific
	 * device id
	 */
	private static final int HRM_DEVICE_ID = 0;
	private static final int SC_DEVICE_ID = 0;
	
	public static final Level LOG_LEVEL = Level.SEVERE;

	public static void setupLogging() {
		// set logging level
		AntTransceiver.LOGGER.setLevel(LOG_LEVEL);
		ConsoleHandler handler = new ConsoleHandler();
		// PUBLISH this level
		handler.setLevel(LOG_LEVEL);
		AntTransceiver.LOGGER.addHandler(handler);
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
		node = new Node(antchip);

		// ANT+ key
		key = new NetworkKey(0xB9, 0xA5, 0x21, 0xFB, 0xBD, 0x72, 0xC3, 0x45);
		key.setName("N:ANT+");

		/* must be called before any configuration takes place */
		node.start();

		/* sends reset request : resets channels to default state */
		node.reset();

		// specs say wait 500ms after reset before sending any more host
		// commands
		try {
			Thread.sleep(500);
		} catch (InterruptedException ex) {

		}

		// sets network key of network zero
		node.setNetworkKey(0, key);

		System.out.println("max channels " + node.getMaxChannels());

		channel = node.getFreeChannel();
		System.out.println("channel number " + channel.getNumber() + " isfree "
				+ channel.isFree());

		// Arbitrary name : useful for identifying channel
		channel.setName("C:SC");

		// choose slave or master type. Constructors exist to set
		// two-way/one-way and shared/non-shared variants.
		ChannelType channelType = new SlaveChannelType();

		// use ant network key "N:ANT+"
		channel.assign("N:ANT+", channelType);

		// registers our Heart Rate and Speed and Cadence callbacks with the
		// channel
		channel.registerRxListener(new SpeedCadenceListener(),
				BroadcastDataMessage.class);

		// ******* start device specific configuration ******
		channel.setId(SC_DEVICE_ID, ANT_SPORT_SandC_TYPE,
				HRM_TRANSMISSION_TYPE, PAIRING_FLAG);

		channel.setFrequency(ANT_SPORT_FREQ);

		channel.setPeriod(ANT_SPORT_SPEED_PERIOD);

		// ******* end device specific configuration ******

		// timeout before we give up looking for device
		channel.setSearchTimeout(Channel.SEARCH_TIMEOUT_NEVER);

		// start listening
		channel.open();

		// *** Heart Rate Monitor ***

		Channel hrchannel = node.getFreeChannel();
		System.out.println("channel number " + hrchannel.getNumber()
				+ " isfree " + hrchannel.isFree());

		// Arbitrary name : useful for identifying channel
		hrchannel.setName("C:HRM");

		ChannelType channelType2 = new SlaveChannelType(); // use ant
															// network key
															// "N:ANT+"
		hrchannel.assign("N:ANT+", channelType2);

		// registers an instance of our callback with the channel
		hrchannel.registerRxListener(new HeartRateListener(),
				BroadcastDataMessage.class);

		// start device specific configuration

		hrchannel.setId(HRM_DEVICE_ID, HRM_DEVICE_TYPE, HRM_TRANSMISSION_TYPE,
				PAIRING_FLAG);

		hrchannel.setFrequency(ANT_SPORT_FREQ);

		hrchannel.setPeriod(HRM_CHANNEL_PERIOD);

		// ******* end device specific configuration

		// timeout before we give up looking for device
		hrchannel.setSearchTimeout(Channel.SEARCH_TIMEOUT_NEVER);

		// start listening
		hrchannel.open();
		
		// Listen for 60 seconds
		Thread.sleep(30000);

		// optional : demo requesting of channel configuration. If device
		// connected
		// this will reflect actual device id, transmission type etc. This
		// info
		// will allow
		// you to only connect to this device in the future.
		printChannelConfig(channel);
		printChannelConfig(hrchannel);


		// stop listening
		channel.close();
		hrchannel.close();

		// resets channel configuration
		channel.unassign();
		hrchannel.unassign();

		// return the channel to the pool of available channels
		node.freeChannel(channel);

		// cleans up : gives up control of usb device etc.
		node.stop();

	}

}
