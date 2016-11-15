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
 * CycleOps Super Magneto Pro Rollers
 * 
 * (c) 2016 David George / Wattzap.com
 * 
 * @author David George
 * @date 30 October 2016
 * 
 * Has 4 levels:
 * 
 * Power curve is a polynomial until around 25kph, linear afterwards.
 * 
 * From here http://blog.trainerroad.com/cycleops-supermagneto-pro-added/
 * 
 */
@PowerAnnotation
public class CycleOpsSuperMagnetoPro extends Power {
	private final Cubic cubic = new Cubic();

	public int getPower(double speed, int resistance) {
		double power = 0.0;

		switch (resistance) {
		case 0: // EASY
			power = (0.10156 * speed * speed) + (2.20833 * speed)
					- (0.00033 * speed * speed * speed);
			break;
		case 1: // ROAD
			if (speed < 25) {
				power = (3.04167 * speed) + (-0.02344 * speed * speed)
						+ (0.00423 * speed * speed * speed);
			} else {
				power = (12.5 * speed) - 185;
			}
			break;
		case 2: // Interval
			if (speed < 25) {
				power = (4.91667 * speed) + (-0.25 * speed * speed)
						+ (0.01107 * speed * speed * speed);
			} else {
				power = (speed * 17.35) - 288;
			}
			break;
		case 3:// Mountain
			if (speed < 21) {

				power = (5.25 * speed) + (-0.4375 * speed * speed)
						+ (0.02148 * speed * speed * speed);
			} else {
				power = (speed * 18.44) - 271;
			}
			break;
		}

		return (int) power;
	}

	// used for virtual power calculations in DummySpeedCadence Listener
	public double getSpeed(int power, int resistance) {
		double speed = 0;

		switch (resistance) {
		case 0: // EASY
			// x3, x2, x1
			cubic.solve(-0.00033, 0.10156, 2.20833, 0 - power);
			speed = cubic.x1;

			break;
		case 1:
			if (power < 118) {
				cubic.solve(0.00423, -0.02344, 3.04167, 0 - power);
				speed = cubic.x1;
			} else {
				speed = (185 + power)/12.5;

			}
			break;
		case 2:
			if (power < 128) {
				cubic.solve(0.01107, -0.25, 4.91667, 0 - power);
				speed = cubic.x1;
			} else {
				speed = (power + 288)/17.35;
			}
			break;
		case 3:
			if (power < 117) {
				cubic.solve(0.02148, -0.4375, 5.25 , 0 - power);
				speed = cubic.x1;
			} else {
				speed = (power + 271)/18.44;

			}
			break;
		}
		return speed;
	}

	public String toString() {
		return "CycleOpsSuperMagnetoPro";
	}

	@Override
	public int getResitanceLevels() {
		return 4;
	}
}
