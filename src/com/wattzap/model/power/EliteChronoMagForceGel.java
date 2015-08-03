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

/* Elite Chrono Mag Force
 * 
 * (c) 2013 David George / Wattzap.com
 * 
 * @author David George
 * @date 21 November 2013
 * 
 * 5 resistance levels. Can generate 850 watts at 50kph on level 5
 * 
 * L1: 0.00083x3 -0.05x2 + 7.81667x + -46
 * L2: -0.00042x3 + 0.055x2 + 7.64167x -39.5
 * L3: -0.00075x3 + 0.075x2 + 10.075x -45.5
 * L4: 0.00054x3 -0.0675x2 + 18.94583x -104.25
 * L5: -0.00121x3 + 0.1075x2 + 16.72083x -61.75
 * 
 * L5: 1.91667x + 0.5875X2 -0.00792X3

 */

@PowerAnnotation
public class EliteChronoMagForceGel extends Power {
	private final Cubic cubic = new Cubic();

	public int getPower(double speed, int resistance) {
		double power = 0;

		switch (resistance) {
		case 0:
			power = (7.82 * speed) - (0.05 * speed * speed)
					+ (0.00083 * (speed * speed * speed)) - 46;
			break;
		case 1:
			power = (7.64167 * speed) + (0.055 * speed * speed)
					- (0.00042 * (speed * speed * speed)) - 39.5;
			break;
		case 2:
			power = (10.075 * speed) + (0.075 * speed * speed)
					- (0.00075 * (speed * speed * speed)) - 45.5;
			break;
		case 3:
			power = (18.94583 * speed) - (0.0675 * speed * speed)
					+ (0.00054 * (speed * speed * speed)) - 104.25;
			break;
		case 4:
			power = (1.91667 * speed) + (0.5875 * speed * speed)
					- (0.00121 * (speed * speed * speed));
			break;
		}
		if (power < 0) {
			power = 0;
		}

		return (int) power;
	}

	@Override
	public double getSpeed(int power, int resistance) {
		double speed = 0;

		switch (resistance) {
		case 0:
			cubic.solve(0.00083, -0.05, 7.82, -(46 + power));
			speed = cubic.x1;

			break;
		case 1:
			cubic.solve(-0.00042, 0.055, 7.64167, -(39.5 + power));
			speed = cubic.x2;

			break;
		case 2:
			cubic.solve(-0.00075, 0.075, 10.075, -(45.5 + power));
			speed = cubic.x2;

			break;
		case 3:
			cubic.solve(0.00054, -0.0675, 18.94583, -104.25 - power);
			speed = cubic.x1;

			break;
		case 4:
			cubic.solve(-0.00121, 0.5875, 1.91667, 0 - power);
			speed = cubic.x2;
			break;
		}

		return speed;
	}

	public String description() {
		return "Elite Chrono Mag Force Gel";
	}

	@Override
	public int getResitanceLevels() {
		return 5;
	}
	
	public int getNeutral() {
		return 2;
	}
}