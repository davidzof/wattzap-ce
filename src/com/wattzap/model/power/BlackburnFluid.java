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
 * Blackburn Basic Mag
 * 
 * Resistance Options - these seem high
 * 1: 14.493 * speed + 0.0416648 * speed ^3
 */

public class BlackburnFluid extends Power {
	private static final double a = 0.0416648;
	private static final double b = 0;
	private static final double c = 14.493;
	private static final double d = 0;
	private final Cubic cubic = new Cubic();

	public int getPower(double speed, int resistance) {
		double power = 0.0;

		power = (c * speed) + (a * speed * speed * speed);

		return (int) power;
	}

	public double getSpeed(int power, int resistance) {
		cubic.solve(a, b, c, d - power);
		return cubic.x1;
	}

	public String description() {
		return "Blackburn Fluid";
	}

	@Override
	public int getResitanceLevels() {
		return 1;
	}
}
