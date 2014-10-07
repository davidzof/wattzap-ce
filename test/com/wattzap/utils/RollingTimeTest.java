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

import org.junit.Test;

public class RollingTimeTest {
	@Test
	public void rolling() {
		RollingTime rt = new RollingTime(30);
		rt.average(10, 1);
		rt.average(10, 12);
		
		System.out.println("ave1 " + rt.average(15, 17));
		rt.average(10, 25);
		
		System.out.println("ave2 " + rt.average(10, 31));
		rt.average(20, 33);
		
		System.out.println("ave3 " + rt.average(15, 40));
		
		System.out.println("ave4 " + rt.average(15, 42));
		
		System.out.println("ave5 " + rt.average(15, 45));
	}
}
