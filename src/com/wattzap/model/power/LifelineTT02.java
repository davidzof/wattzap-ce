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
 * (c) 2020 David George / Wattzap.com
 * 
 * @author David George
 * @date 22 September 2020
 * 
 * 100w / 23kph
 * 190w / 30kph
 * 300w / 35kph
 * 400w / 40kph
 * 
y = 0.005575803736x3 - 0.02085437656=x2 + 1.898396261 x - 0.01631642496
http://www.xuru.org/rt/PR.asp#Manually
 */
@PowerAnnotation
public class LifelineTT02 extends Power {
	private static final double a = 0.005575803736;
	private static final double b = -0.02085437656;
	private static final double c = 1.898396261;
	private static final double d = -0.01631642496;
	private final Cubic cubic = new Cubic();
	
	public int getPower(double speed, int resistance) {
		double power = (c * speed) + (b * speed * speed)
				+ (a * speed * speed * speed) + d;
		
		return (int) power;
	}

	public String toString() {
		return "Lifeline TT-02";
	}

	@Override
	public int getResistance(double gradient) {
		return 1;
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