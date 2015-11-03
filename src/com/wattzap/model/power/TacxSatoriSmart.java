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
 * @date 11 June 2013
 */
@PowerAnnotation
public class TacxSatoriSmart extends Power {
	private final double[] slope = { 11.75, 13, 14.2, 15.45, 16.65, 17.9, 19.1, 20.35, 21.65, 22.85 };
	private final double[] intercept = { -10, -9, -7, -6, -4, -3, -1, 0, 0, 0 };

	public int getPower(double speed, int resistance) {
		int power = (int) ((speed * slope[resistance]) + intercept[resistance]);
		if (power < 0) {
			return 0;
		}
		return power;
	}

	public double getSpeed(int power, int resistance) {
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

	public String toString() {
		return "Tacx Satori Smart";
	}
}
