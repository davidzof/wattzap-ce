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
 * Elite Paraboloic Rollers
 * 
 * (c) 2013 Pierre Engles / Wattzap.com
 * 
 * @author David George
 * @date 11 November 2013
 * 
 * Has 1 levels:
 * 
 * Level 0: 0.00002038 x3 + 0.01798x2 + 4.577x +3.05
 * 
 * calculate with libreoffice calc
 * 
 */
@PowerAnnotation
public class EliteParabolicRollers extends Power {
	private final Cubic cubic = new Cubic();
	
	public int getPower(double speed, int resistance) {
		double power = 0.0;
			if(speed > 0){
				power = 3.05 + (4.577 * speed) + (0.01798 * speed * speed)
					+ (-0.00002038 * speed * speed * speed);
			}
		return (int) power;
	}

	public double getSpeed(int power, int resistance) {
		double speed = 0;
		if(power > 0){
			cubic.solve(0.00002038, 0.01798, 4.577, 3.05 - power);
			speed = cubic.x2;
		}
		return speed;
	}

	public String description() {
		return "Elite Parabolic rollers";
	}

	@Override
	public int getResitanceLevels() {
		return 1;
	}
}
