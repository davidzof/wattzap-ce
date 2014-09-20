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
package com.wattzap.utils;

import java.util.LinkedList;

/**
 * Rolling Average Calculator for a time delta Not thread safe
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
	 * @param v
	 *            - value
	 * @param t
	 *            - time in seconds value was taken
	 */
	public int average(int v, long t) {
		int time = 1;
		
		if (!fifo.isEmpty()) {
			Value last = fifo.getLast();
			time = (int) (t - last.t);
			if (v > delta) {
				return v;
			}
		}
		for (int i = 0; i < time; i++) {
			Value value = new Value(v, t);
			fifo.add(value);
			total += v;

			Value first = fifo.getFirst();

			// keep just delta number of values in fifo
			while (t > first.t + delta) {
				fifo.removeFirst();
				total -= first.v;
				first = fifo.getFirst();
			}// while
		}// for

		if (fifo.getLast().t < (fifo.getFirst().t + delta)) {
			return 0;
		}

		return (int) total / fifo.size();
	}

	private class Value {
		int v;
		long t;

		Value(int i, long t) {
			this.v = i;
			this.t = t;
		}
	}
}