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

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EliteAranMagTest {
	Power p;

	@Before
	public void setup() {
		p = new EliteArionMag();
	}
	
	@Test
	public void checkSpeed() {
		BigDecimal bd;
		for (int s = 6; s < 60; s++) {
			for (int i = 1; i <= p.getResitanceLevels(); i++) {
				int power = p.getPower(s, i);

				double speed = p.getSpeed(power, i);
				bd = new BigDecimal(speed).setScale(0, RoundingMode.HALF_UP);
				Assert.assertEquals(bd.intValue(), s);
			}
		}
	}

	@Test
	public void checkPower() {
		System.out.println("speed,power");

		for (int speed = 0; speed < 60; speed++) {
			for (int resistance = 1; resistance <= p.getResitanceLevels(); resistance++) {
				if (resistance == 1) {
					System.out.printf("%d", speed);
				}
				int power = p.getPower(speed, resistance);
				System.out.printf(",%d",power);
			}
			System.out.println();

		}
	}

}
