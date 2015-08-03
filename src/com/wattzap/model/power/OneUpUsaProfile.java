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
 * 1UpUsa Trainer
 * 
 * Resistance Options
 * 6 pockets: 6.0341 + -1.3882 x speed + 0.2543 x speed^2 + 0.0388 x speed ^ 3
 * CPR A-2000 (Black Flyweight): 25 + 2.65 x speed + -0.415 x speed^2 + 0.058 x speed ^ 3
 * Inner pockets: 10.82 + -2.0392 x speed + 0.3586 x speed^2 + 0.0097 x speed ^ 3
 * Outer pockets: 11.61 + -1.8042 x speed + 0.364 x speed^2 + 0.0103 x speed ^ 3
 * 
 * Speed in MPG
 * 
 * http://www.1upusa.com/product-trainer.html
 * 
 */
public class OneUpUsaProfile extends Power {
	public int getPower(double speed, int resistance) {
		double power = 0.0;

		switch (resistance) {
		case 0:
			power = (4.93333 * speed) + (0.2 * speed * speed)
					+ (-0.00433 * speed * speed * speed);

		case 1:
		case 2:
		}


		return (int) power;
	}

	public String description() {
		return "OneUp USA";
	}

	@Override
	public int getResitanceLevels() {
		return 3;
	}

	@Override
	public double getSpeed(int power, int resistance) {
		// TODO Auto-generated method stub
		return 0;
	}
}
