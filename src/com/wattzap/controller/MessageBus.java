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
package com.wattzap.controller;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Message Bus implementation
 * 
 * A Message Bus is a combination of a common data model, a common command set,
 * and a messaging infrastructure to allow different systems to communicate
 * through a shared set of interfaces. This is analogous to a communications bus
 * in a computer system, which serves as the focal point for communication
 * between the CPU, main memory, and peripherals. Just as in the hardware
 * analogy, there are a number of pieces that come together to form the message
 * bus:
 * 
 * (c) 2013-2015 David George / Wattzap.com
 * 
 * @author David George
 * @date 12 November 2013
 */
public enum MessageBus {
	INSTANCE;
	Map<Messages, HashSet<MessageCallback>> objects;

	MessageBus() {
		objects = new EnumMap<Messages, HashSet<MessageCallback>>(
				Messages.class);
	}

	/**
	 * Register a callback to receive specific messages
	 * 
	 * @param m
	 *            - Message type to register (from Messages enum)
	 * @param o
	 *            - Callback implementation - classes implement MessageCallback
	 *            interface.
	 */
	public void register(Messages m, MessageCallback o) {
		HashSet<MessageCallback> listeners;
		if (objects.containsKey(m)) {
			listeners = objects.get(m);
		} else {
			listeners = new HashSet<MessageCallback>();
			objects.put(m, listeners);
		}
		listeners.add(o);
	}

	// TODO Implement this
	public boolean unregister(Messages m, MessageCallback o) {
		if (objects.containsKey(m)) {
			HashSet<MessageCallback> listeners = objects.get(m);
			return listeners.remove(o);
		}
		
		return false;
	};

	public void send(Messages m, Object o) {
		HashSet<MessageCallback> listeners = objects.get(m);
		if (listeners != null) {
			for (MessageCallback callback : listeners) {
				callback.callback(m, o);
			}
		}
	}
}
