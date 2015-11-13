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
 * Power curve for Tacx Genius Smart in passive mode. It is similar to a fluid
 * trainer.
0.00309x³ +
    0.06183x² +
    0.67539x +
    -1.02517
 * 
 * @author David George
 * @date 1st November 2015
 */
@PowerAnnotation
public class TacxGeniusSmart extends Power {
	private static final double a = 0.00309;
	private static final double b = 0.0618;
	private static final double c = 0.675;
	private static final double d = -1.03;
	private final Cubic cubic = new Cubic();

	public int getPower(double speed, int resistance) {

			double power = (a * (speed * speed * speed))
					+ (b * (speed * speed)) + (c * speed) + d;
			return (int) power;


	}

	public String toString() {
		return "Tacx Genius Smart";
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
