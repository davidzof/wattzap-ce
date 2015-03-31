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
 * (c) 2013 David George / Wattzap.com
 * 
 * @author David George
 * @date 21 November 2013
 * 
 * 10km/h=44w, 20km/h=142w, 30km/h=367w, 40km/h=756, 50km/h=1376w, 60km/h=2200w
 * 
 * Elite Qubo Fluid
 * 
 * y = 0.00932x³ + 0.019x² + 3.27833x 
 */
@PowerAnnotation
public class EliteQuboFluid extends Power {
	private static final double a = 0.00932;
	private static final double b = 0.019;
	private static final double c = 3.27833;
	private static final double d = 0;
	private final Cubic cubic = new Cubic();
	
	public int getPower(double speed, int resistance) {
		double power = (a * (speed * speed * speed)) + (b * (speed * speed)) + (c * speed) ;
		
		return (int) power;
	}

	public String description() {
		return "Elite Qubo Fluid";
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