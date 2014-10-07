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
 * Elite Novo Power
 * 
 * (c) 2013-2014 David George / Wattzap.com
 * 
 * @author David George
 * @date 11 November 2013
 * 
 * From GC
 * 
 *         0 5 10 15 20 25 30 35 40 45 50 kph
 * Level 1 0 10 28 48 68 96 124 154 185 218 260
 * Level 2 0 14 37 67 103 137 180 227 271 315 360
 * Level 3 0 18 50 93 140 194 247 301 365 422 474
 * Level 4 0 20 60 111 170 236 305 371 440 501 560
 * Level 5 0 21 69 131 201 275 360 434 517 590 660
 * Level 6 0 25 78 150 233 322 410 504 590 680 760
 * Level 7 0 28 89 171 265 363 464 566 665 760 850
 * Level 8 0 31 100 192 298 405 518 630 730 835 930
 *
 * A3*X^3+A2*x^2+A1*x+A0 where:
 *
 * A3  A2  A1  A0
 *
 * Lev1 -0.000192696 0.068298368 2.252136752 -1.167832168
 * Lev2 -0.001417249 0.170862471 2.204895105 -0.363636364
 * Lev3 -0.002200466 0.237202797 3.161305361 -1.48951049
 * Lev4 -0.003599068 0.33981352 3.211888112 -1.734265734
 * Lev5 -0.004141414 0.394102564 3.875679876 -3.188811189
 * Lev6 -0.004683761 0.445967366 4.642501943 -4.188811189
 * Lev7 -0.005337995 0.498764569 5.436829837 -5.118881119
 * Lev8 -0.006119658 0.551748252 6.341103341 -6.48951049
 */
@PowerAnnotation
public class EliteNovoPower extends Power {
	private final Cubic cubic = new Cubic();

	public int getPower(double speed, int resistance) {
		double power = 0;

		switch (resistance) {
		case 1:
			power = -(0.000192696 * (speed * speed * speed))
					+ (0.068298368 * speed * speed) + (2.252136752 * speed)
					- 1.167832168;
			break;
		case 2:
			power = -(0.001417249 * (speed * speed * speed))
					+ (0.170862471 * speed * speed) + (2.204895105 * speed)
					- 0.363636364;
			break;
		case 3:
			power = -(0.002200466 * (speed * speed * speed))
					+ (0.237202797 * speed * speed) + (3.161305361 * speed)
					- 1.48951049;
			break;
		case 4:
			power = -(0.003599068 * (speed * speed * speed))
					+ (0.33981352 * speed * speed) + (3.875679876 * speed)
					- 1.734265734;
			break;
		case 5:
			power = -(0.004141414 * (speed * speed * speed))
					+ (0.394102564 * speed * speed) + (3.875679876 * speed)
					- 3.188811189;
			break;
		case 6:
			power = -(0.004683761 * (speed * speed * speed))
					+ (0.445967366 * speed * speed) + (4.642501943 * speed)
					- 4.188811189;
			break;
		case 7:
			power = -(0.005337995 * (speed * speed * speed))
					+ (0.498764569 * speed * speed) + (5.436829837 * speed)
					- 5.118881119;
			break;
		case 8:
			power = -(0.006119658 * (speed * speed * speed))
					+ (0.551748252 * speed * speed) + (6.341103341 * speed)
					- 6.48951049;
			break;
		}
		if (power < 0) {
			power = 0;
		}

		return (int) power;
	}

	public String description() {
		return "Elite Novo Power";
	}

	public double getSpeed(int power, int resistance) {
		switch (resistance) {
		case 1:
			cubic.solve(-0.000192696, 0.068298368, 2.252136752, -1.167832168d
					- power);

			break;
		case 2:
			cubic.solve(-0.0014172496, 0.1708624718, 2.204895105, -0.363636364
					- power);

			break;
		case 3:
			cubic.solve(-0.002200466, 0.237202797, 3.161305361, -1.48951049
					- power);

			break;
		case 4:
			cubic.solve(-0.003599068, 0.33981352, 3.875679876, -1.734265734
					- power);

			break;
		case 5:
			cubic.solve(-0.004141414, 0.394102564, 3.875679876, -3.188811189
					- power);

			break;
		case 6:
			cubic.solve(-0.004683761, 0.445967366, 4.642501943, -4.188811189
					- power);

			break;
		case 7:
			cubic.solve(-0.005337995, 0.498764569, 5.436829837, -5.118881119
					- power);

			break;
		case 8:
			cubic.solve(-0.006119658, 0.551748252, 6.341103341, -6.48951049
					- power);

			break;
		}

		if (cubic.nRoots == 3) {
			return cubic.x2;
		}
		return cubic.x1;
	}

	@Override
	public int getResitanceLevels() {
		return 8;
	}
}