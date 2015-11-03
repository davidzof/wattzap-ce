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
 * Elite Aran Mag Rollers
 * 
 * (c) 2013 David George / Wattzap.com
 * 
 * @author David George
 * @date 11 November 2013
 * 
 * Has 3 levels:
 * 
 * Level 0: -0.00146x3 + 0.08875x2 + 5.95833x
 * Level 1: -0.00154x3 + 0.13125x2 + 6.14167x
 * Level 2: -0.00462x3 +0.40375x2 + 4.625x
 * 
 * From here and here http://www.had2know.com/academics/cubic-through-4-points.html
 * 
 */
@PowerAnnotation
public class EliteArionMag extends Power {
	private final Cubic cubic = new Cubic();
	
	public int getPower(double speed, int resistance) {
		double power = 0.0;

		switch (resistance) {
		case 0:
			power = (5.95833 * speed) + (0.08875 * speed * speed)
					+ (-0.00146 * speed * speed * speed);
			break;
		case 1:
			power = (6.14167 * speed) + (0.13125 * speed * speed)
					+ (-0.00154 * speed * speed * speed);
			break;
		case 2:
			power = (4.625 * speed) + (0.40374 * speed * speed)
					+ (-0.00462 * speed * speed * speed);
			break;
		}

		return (int) power;
	}

	public double getSpeed(int power, int resistance) {
		double speed = 0;

		switch (resistance) {
		case 0:
			cubic.solve(-0.00146, 0.08875, 5.95833, 0 - power);
			speed = cubic.x2;
			break;
		case 1:
			cubic.solve(-0.00154, 0.13125, 6.14167, 0 - power);
			speed = cubic.x2;
			break;
		case 2:
			cubic.solve(-0.00462, 0.40374, 4.625, 0 - power);
			speed = cubic.x2;
			break;
		}
		return speed;
	}

	public String toString() {
		return "Elite Arion Mag";
	}
	
	@Override
	public int getResitanceLevels() {
		return 3;
	}
}
