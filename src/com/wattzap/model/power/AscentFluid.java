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
 * Ascent Fluid
 * Power: 10km/h=50w, 20km/h=112w, 30km/h=232w, 40km/h=415w, 50km/h=700w, 60km/h=1100w
 * 
 * Function: y = 0.00442x³ -0.04x² + 4.95833x
 * 
 * @author David George
 * @date 2 January 2015
 */
@PowerAnnotation
public class AscentFluid extends Power {
	private static final double a = 0.00422;
	private static final double b = -0.04;
	private static final double c = 4.95833;
	private static final double d = 0;
	private static final Cubic cubic = new Cubic();


	public int getPower(double speed, int resistance) {

		double power = (c * speed) + (b * speed * speed)
				+ (a * speed * speed * speed);
		return (int) power;
	}

	public String toString() {
		return "Ascent Fluid";
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
