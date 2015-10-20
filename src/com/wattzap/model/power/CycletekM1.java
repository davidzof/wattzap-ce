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
package com.wattzap.model.power;

/*
 * Cylcletek M1
 *  
 * @author David George
 * @date 2 January 2015
 * 
 * 
 * 16.1  32.2  48.2  64.4 kmh
 * 33    244   794   2093
 * 
 * y = 0.00662x³ + 0.02344x² -0.04456x
 *  
 */
@PowerAnnotation
public class CycletekM1 extends Power {
	private static final double a = 0.00662;
	private static final double b = 0.02344;
	private static final double c = -0.04456;
	private static final double d = 0;
	private static final Cubic cubic = new Cubic();

	public int getPower(double speed, int resistance) {

		double power = (c * speed) + (b * speed * speed)
				+ (a * speed * speed * speed);

		return (int) power;
	}

	public String toString() {
		return "Cycletek M1";
	}

	@Override
	public int getResitanceLevels() {
		return 1;
	}

	public double getSpeed(int power, int resistance) {
		cubic.solve(a, b, c, d - power);
		return cubic.x1;
	}
}
