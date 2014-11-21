package com.wattzap.model.ant;

import org.cowboycoders.ant.events.BroadcastListener;
import org.cowboycoders.ant.messages.data.BroadcastDataMessage;

public abstract class AntListener implements
		BroadcastListener<BroadcastDataMessage> {
	
	public abstract int getChannelId();
	public abstract int getChannelPeriod();
	public abstract int getDeviceType();
	public abstract String getName();
}
