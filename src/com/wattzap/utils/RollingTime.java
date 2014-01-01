package com.wattzap.utils;

import java.util.LinkedList;

/**
 * Rolling Average Calculator for a time delta
 * Not thread safe
 * 
 * @author David George
 * @date 2nd September 2013
 */
public class RollingTime {
	LinkedList<Value> fifo = new LinkedList<Value>();
	long delta;
	int total;

	public RollingTime(long delta) {
		this.delta = delta;
	}

	/**
	 * 
	 * @param i - value
	 * @param t - time in seconds value was taken
	 */
	public void add(int i, long t) {
		Value value = new Value(i, t);

		fifo.add(value);
		total += i;

		Value first = fifo.getFirst();
		// keep just delta values in fifo
		while (value.t > first.t + delta) {
			fifo.removeFirst();
			total -= first.i;
			first = fifo.getFirst();
		}
	}

	public int getAverage() {
		if (fifo.getLast().t < (fifo.getFirst().t + delta)) {
			return 0;
		}

		return (int) total / fifo.size();
	}

	private class Value {
		int i;
		long t;

		Value(int i, long t) {
			this.i = i;
			this.t = t;
		}
	}


}