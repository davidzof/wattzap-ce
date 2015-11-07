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
 * (c) 2015 David George / Wattzap.com
 * 
 * 
 * @author David George
 * @date 1st November 2015
 */
@PowerAnnotation
public class TacxSatoriSmart extends Power {
	private final double[] slope = {       9.75, 10.95, 12.15,  13.35, 14.55, 15.75, 16.9,  18.1,  19.3, 20.5 };
	private final double[] intercept = { -52.5, -56.5, -60.5, -63.5, -67.5, -71.5, -74.0, -78.0, -81.0, -85.0 };

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
