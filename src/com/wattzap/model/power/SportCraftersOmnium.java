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
 * (c) 2014 David George / Wattzap.com
 * 
 * 
 * @author David George
 * @date 11 January 2014
 * 
 * SportCrafters Omnium
 * http://sportcrafters.com/
 * 
 * mph to kmh = 1.60934, 2.5899752356, 4.168150745660504
 * 
 * Resistance Options
 * 
 * 
 * P = 1.0545 * S + 0.1504 * S^2 (kph
 */
@PowerAnnotation
public class SportCraftersOmnium extends Power {
	private static final double b = 0.1504;
	private static final double c = 1.0545;
	private static final double d = 0;

	public int getPower(double speed, int resistance) {
		double power = (c * speed) + (b * (speed * speed));
		return (int) power;
	}

	public double getSpeed(int power, int resistance) {
		return quadraticEquationRoot1(b, c, d - power);
	}

	public String toString() {
		return "SportCrafters Omnium";
	}
	
	@Override
	public int getResistance(double gradient) {
		return 1;
	}

	@Override
	public int getResitanceLevels() {
		return 1;
	}
}