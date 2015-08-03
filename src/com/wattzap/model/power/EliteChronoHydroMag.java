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
 * @date 21 March 2014
 * 
 * 5 resistance levels. Can generate 2724 watts at 64.4kph on level 5
 * 
 * L1: 0.00141x3 + 0.09596x2 + 2.5x
 * L2: 0.00135x3 + 0.18566x2 + 2.18944x 
 * L3: -0.00047x3 + 0.41424x2 + -0.95756x
 * L4: 0.00307x3 + 0.28018x2 + 2.14803x
 * L5: 0.00757x3 + 0.07234x2 + 6.25259x
 */
@PowerAnnotation
public class EliteChronoHydroMag extends Power {
	private final Cubic cubic = new Cubic();
	private final static int neutral = 2;

	public int getPower(double speed, int resistance) {
		double power = 0;

		switch (resistance) {
		case 0:
			power = (0.00141 * (speed * speed * speed))
					+ (0.09596 * speed * speed) + (2.5 * speed);
			break;
		case 1:
			power = (0.00135 * (speed * speed * speed))
					+ (0.18566 * speed * speed) + (2.18944 * speed);
			break;
		case 2:
			power = (-0.00047 * (speed * speed * speed))
					+ (0.41424 * speed * speed) + (-0.95756 * speed);
			break;
		case 3:
			power = (0.00307 * (speed * speed * speed))
					+ (0.28018 * speed * speed) + (2.1480 * speed);
			break;
		case 4:
			power = (0.00757 * (speed * speed * speed))
					+ (0.07234 * speed * speed) + (6.25259 * speed);
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
			cubic.solve(0.00141, 0.09596, 2.5, -power);
			speed = cubic.x1;

			break;
		case 1:
			cubic.solve(0.00135, 0.18566, 2.18944, -power);
			speed = cubic.x1;

			break;
		case 2:
			cubic.solve(-0.00047, 0.41424, -0.95756, -power);
			speed = cubic.x2;

			break;
		case 3:
			cubic.solve(0.00307, 0.28018, 2.14803, -power);
			speed = cubic.x1;

			break;
		case 4:
			cubic.solve(0.00757, 0.07234, 6.25259, -power);
			speed = cubic.x1;
			break;
		}

		return speed;
	}

	public String description() {
		return "Elite Chrono HydroMag";
	}

	@Override
	public int getResitanceLevels() {
		return 5;
	}

	public int getNeutral() {
		return 2;
	}
}