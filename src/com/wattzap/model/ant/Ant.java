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

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
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

import com.wattzap.controller.MessageBus;
import com.wattzap.controller.MessageCallback;
import com.wattzap.controller.Messages;
import com.wattzap.model.UserPreferences;

/**
 * Gets data from Ant device and calculates speed, distance, cadence etc.
 * 
 * @author David George
 * @date 30 May 2013
 */
public class Ant implements MessageCallback {
	private Node node = null;
	private NetworkKey key = null;

	private AntListener SCListener;
	private AntListener HRListener;
	private HashMap<String,AntListener> antListeners;
	private HashMap<String,Channel> antChannels;
	
	private UserPreferences userPrefs = UserPreferences.INSTANCE;

	private static final int ANT_SPORT_FREQ = 57; // 0x39

	private static Logger logger = LogManager.getLogger("Ant");

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
	private static final int TRANSMISSION_TYPE = 0/*1*/;

	public static final Level LOG_LEVEL = Level.SEVERE;

	public Ant(HashMap<String,AntListener> listeners) {
		antListeners = listeners;
		
		AntTransceiver antchip = null;
		if (userPrefs.isANTUSB()) {
			antchip = new AntTransceiver(0, AntTransceiver.ANTUSBM_ID);
		} else {
			antchip = new AntTransceiver(0);
		}
		// initialises node with chosen driver
		node = new Node(antchip);

		// ANT+ key
		key = new NetworkKey(0xB9, 0xA5, 0x21, 0xFB, 0xBD, 0x72, 0xC3, 0x45);
		key.setName("N:ANT+");
	}

	public Ant(AntListener scListener, AntListener hrmListener) {
		// optional: enable console logging with Level = LOG_LEVEL
		setupLogging();
		/*
		 * Choose driver: AndroidAntTransceiver or AntTransceiver
		 * 
		 * AntTransceiver(int deviceNumber) deviceNumber : 0 ... number of usb
		 * sticks plugged in 0: first usb ant-stick
		 */
		SCListener = scListener;
		HRListener = hrmListener;
		AntTransceiver antchip = null;
		if (userPrefs.isANTUSB()) {
			antchip = new AntTransceiver(0, AntTransceiver.ANTUSBM_ID);
		} else {
			antchip = new AntTransceiver(0);
		}
		// initialises node with chosen driver
		node = new Node(antchip);

		// ANT+ key
		key = new NetworkKey(0xB9, 0xA5, 0x21, 0xFB, 0xBD, 0x72, 0xC3, 0x45);
		key.setName("N:ANT+");
	}

	public void register() {
		MessageBus.INSTANCE.register(Messages.START, this);
		MessageBus.INSTANCE.register(Messages.STOP, this);
	}

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

			/*
			 * System.out.println();
			 * System.out.println("Device configuration: ");
			 * System.out.println("deviceID: " + response.getDeviceNumber());
			 * System.out.println("deviceType: " + response.getDeviceType());
			 * System.out.println("transmissionType: " +
			 * response.getTransmissionType());
			 * System.out.println("pairing flag set: " +
			 * response.isPairingFlagSet()); System.out.println();
			 */

		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
		}
	}



	private static int getChannelId(Channel channel) {
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

			return response.getDeviceNumber();
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
		}

		return 0;
	}

	public void close() {
		// stop listening
		for (Channel channel : antChannels.values()) {
			if (!channel.isFree()) {
				logger.debug("Stopping ANT device");
				channel.close();
				
				// resets channel configuration
				channel.unassign();
				
				// return the channel to the pool of available channels
				node.freeChannel(channel);
			}
		}
		// cleans up : gives up control of usb device etc.
		node.stop();
	}
	
	public void open() {
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
		
		antChannels = new HashMap<String,Channel>();
		for (AntListener listener : antListeners.values()) {
			Channel channel = node.getFreeChannel();
			antChannels.put(listener.getName(), channel);
			
			// Arbitrary name : useful for identifying channel
			channel.setName(listener.getName());
		
		System.out.println("opening " + listener.getName());

		// choose slave or master type. Constructors exist to set
		// two-way/one-way and shared/non-shared variants.
		ChannelType channelType = new SlaveChannelType();

		// use ant network key "N:ANT+"
		channel.assign("N:ANT+", channelType);

		// registers our Heart Rate and Speed and Cadence callbacks with the
		// channel
		channel.registerRxListener(listener, BroadcastDataMessage.class);

		// ******* start device specific configuration ******
		channel.setId(listener.getChannelId(), listener.getDeviceType(),
				TRANSMISSION_TYPE,  PAIRING_FLAG);

		channel.setFrequency(ANT_SPORT_FREQ);
		channel.setPeriod(listener.getChannelPeriod());

		// ******* end device specific configuration ******

		// timeout before we give up looking for device
		channel.setSearchTimeout(Channel.SEARCH_TIMEOUT_NEVER);

		// start listening
		channel.open();
		}

		
	}

	
//	public void open() {
//		/* must be called before any configuration takes place */
//		node.start();
//
//		/* sends reset request : resets channels to default state */
//		node.reset();
//
//		// specs say wait 500ms after reset before sending any more host
//		// commands
//		try {
//			Thread.sleep(500);
//		} catch (InterruptedException ex) {
//
//		}
//
//		// sets network key of network zero
//		node.setNetworkKey(0, key);
//
//		scChannel = node.getFreeChannel();
//
//		// Arbitrary name : useful for identifying channel
//		scChannel.setName(SCListener.getName());
//		
//		System.out.println("opening " + SCListener.getName());
//
//		// choose slave or master type. Constructors exist to set
//		// two-way/one-way and shared/non-shared variants.
//		ChannelType channelType = new SlaveChannelType();
//
//		// use ant network key "N:ANT+"
//		scChannel.assign("N:ANT+", channelType);
//
//		// registers our Heart Rate and Speed and Cadence callbacks with the
//		// channel
//		scChannel.registerRxListener(SCListener, BroadcastDataMessage.class);
//
//		// ******* start device specific configuration ******
//		scChannel.setId(SCListener.getChannelId(), SCListener.getDeviceType(),
//				TRANSMISSION_TYPE,  PAIRING_FLAG);
//
//		scChannel.setFrequency(ANT_SPORT_FREQ);
//		scChannel.setPeriod(SCListener.getChannelPeriod());
//
//		// ******* end device specific configuration ******
//
//		// timeout before we give up looking for device
//		scChannel.setSearchTimeout(Channel.SEARCH_TIMEOUT_NEVER);
//
//		// start listening
//		scChannel.open();
//
//		// *** Heart Rate Monitor ***
//
//		hrChannel = node.getFreeChannel();
//
//		// Arbitrary name : useful for identifying channel
//		hrChannel.setName(HRListener.getName());
//
//		ChannelType channelType2 = new SlaveChannelType(); // use ant
//															// network key
//															// "N:ANT+"
//		hrChannel.assign("N:ANT+", channelType2);
//
//		// registers an instance of our callback with the channel
//		hrChannel.registerRxListener(HRListener, BroadcastDataMessage.class);
//
//		// start device specific configuration
//
//		hrChannel.setId(HRListener.getChannelId(), HRListener.getDeviceType(),
//				TRANSMISSION_TYPE, PAIRING_FLAG);
//
//		hrChannel.setFrequency(ANT_SPORT_FREQ);
//
//		hrChannel.setPeriod(HRListener.getChannelPeriod());
//
//		// ******* end device specific configuration
//
//		// timeout before we give up looking for device
//		hrChannel.setSearchTimeout(Channel.SEARCH_TIMEOUT_NEVER);
//
//		// start listening
//		hrChannel.open();
//	}

	@Override
	public void callback(Messages message, Object o) {
		switch (message) {
		case STOP:
			close();
			break;
		case START:
			open();
			break;
		}
	}
	
	public int getHRMChannelId() {
		Channel channel = antChannels.get(HeartRateListener.name);
		return getChannelId(channel);
	}

	public int getSCChannelId() {
		Channel channel = antChannels.get(AdvancedSpeedCadenceListener.name);
		return getChannelId(channel);

	}
}
