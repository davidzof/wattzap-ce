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

/* Elite Volaire Mag Speed
 * 
 * (c) 2014 David George / Wattzap.com
 * 
 * @author David George
 * @date 11 January 2014
 * 
 * 5 resistance levels. Can generate 680 watts at 50kph on level 5
 * 
 * L1: 0.035x2 + 0.95x
 * L2: 0.055x2 + 1.65x   
 * L3: -0.0005x3 + 0.115x2 + 2.8x 
 * L4:-0.0015x3 + 0.21x2 + 3.55x
 * L5: 0.00267x3 + 0.33x2 + 3.76667x
 */

@PowerAnnotation
public class EliteVolareSpeed extends Power {
	private final Cubic cubic = new Cubic();
	private final static int levels = 5;

	public int getPower(double speed, int resistance) {
		double power = 0;

		switch (resistance) {
		case 0:
			power = (0.95 * speed) + (0.035 * speed * speed);
			break;
		case 1:
			power = (1.65 * speed) + (0.055 * speed * speed);
			break;
		case 2:
			power = (2.8 * speed) + (0.115 * speed * speed)
					- (0.0005 * (speed * speed * speed));
			break;
		case 3:
			power = (3.55 * speed) + (0.21 * speed * speed)
					- (0.0015 * (speed * speed * speed));
			break;
		case 4:
			power = (3.76667 * speed) + (0.33 * speed * speed)
					- (0.00267 * (speed * speed * speed));
			break;
		}
		if (power < 0) {
			power = 0;
		}

		return (int) power;
	}

	public double getSpeed(int power, int resistance) {
		double speed = 0;

		switch (resistance) {
		case 0:
			speed = this.quadraticEquationRoot1(0.035, 0.95, 0 - power);
			break;
		case 1:
			speed = this.quadraticEquationRoot1(0.055, 1.65, 0 - power);
			break;
		case 2:
			cubic.solve(-0.0005, 0.115, 2.8, 0 - power);
			speed = cubic.x2;
			break;
		case 3:
			cubic.solve(-0.0015, 0.21, 3.55, 0 - power);
			speed = cubic.x2;
			break;
		case 4:
			cubic.solve(-0.00267, 0.33, 3.76667, 0 - power);
			speed = cubic.x2;
			break;
		}
		return speed;
	}

	public String toString() {
		return "Elite Volare Mag Speed";
	}

	@Override
	public int getResitanceLevels() {
		return levels;
	}
}