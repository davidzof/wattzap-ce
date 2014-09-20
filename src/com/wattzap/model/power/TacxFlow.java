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

/**
 * (c) 2013 David George / Wattzap.com
 * 
 * 
 * @author David George
 * @date 11 June 2013 From
 * 
 *       https://groups.google.com/forum/#!msg/golden-cheetah-users
 *       /9f89rwYCD6c/PyAjwOwE_LkJ
 */
@PowerAnnotation
public class TacxFlow extends Power {
	private final double[] slope = { 4.74, 6.36, 7.75, 9.51, 11.03, 12.81,
			14.37 };
	private final double[] intercept = { -34.9, -43.57, -47.27, -66.69, -72.59,
			-95.05, -102.43 };

	public int getPower(double speed, int resistance) {

		resistance--;
		int power = (int) ((speed * slope[resistance]) + intercept[resistance]);
		if (power < 0) {
			return 0;
		}
		return power;
	}

	public String description() {
		return "Tacx Flow";
	}

	public double getSpeed(int power, int resistance) {
		resistance--;
		double speed = (power - intercept[resistance]) / slope[resistance];
		if (speed < 0) {
			return 0;
		}
		return speed;
	}

	@Override
	public int getResitanceLevels() {
		return slope.length;
	}
}
