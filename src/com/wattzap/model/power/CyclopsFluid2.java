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
 * CycleOps power curve for this trainer
 * 
 * http://thebikegeek.blogspot.fr/2009/12/while-we-wait-for-better-and-better.html
 Armann: 1.5981 x + 0.006942 x^3
 *  
 *  or mayble
 *  
 *  y = 0.0115x3 - 0.0137x2 + 8.9788x
 *  0.00276 - 0.00529 + 5.58
 *  
 *   * mph to kmh = 1.60934, 2.5899752356, 4.168150745660504
 *  
 */
@PowerAnnotation
public class CyclopsFluid2 extends Power {
	private static final double a = 0.00276;
	private static final double b = 0.00529;
	private static final double c = 5.58;
	private static final double d = 0;
	private static final Cubic cubic = new Cubic();

	public int getPower(double speed, int resistance) {

		double power = (c * speed)
				+ (a * speed * speed * speed);

		return (int) power;
	}

	public String description() {
		return "CycleOps Fluid2";
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
