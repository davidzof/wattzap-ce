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
 * CatEye CS1000
 *  
 * @author David George
 * @date 2 February 2015
 * 
 * Power: 16.1km/h=49w,	32.2km/h=170, 48.3km/h=473 64.4km/h=1082w
 * 
 * Function: y = 0.00467x³ -0.0913x² + 3.30228x + 0
 */
@PowerAnnotation
public class CatEyeCS1000 extends Power {
	private static final double a = 0.00467;
	private static final double b = -0.0913;
	private static final double c = 3.30228;
	private static final double d = 0;
	private static final Cubic cubic = new Cubic();

	public int getPower(double speed, int resistance) {

		double power = (c * speed) + (b * speed * speed)
				+ (a * speed * speed * speed);
		return (int) power;
	}

	public String toString() {
		return "CatEye CS1000";
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
