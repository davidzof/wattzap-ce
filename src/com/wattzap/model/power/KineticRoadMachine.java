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
 * @date 11 November 2013
 * 
 * Kinetic Road Machine
 * see: http://www.kurtkinetic.com/powercurve.php
 * http://www.kurtkinetic.com/calibration_chart.php
 * 
 * mph to kmh = 1.60934, 2.5899752356, 4.168150745660504
 * 
 * Resistance Options
 * 5.244820x + 0.01968x3
 * 
 * P = 5.244820 * S + 0.019168 * S^3 (mph)
 * P = 3.258988 * S + 0.004599 * S^3 (kph)
 * 
 * e.g for 25.91kmh
 * P = 84.44 + 79.995
 * P = 164 watts
 */
@PowerAnnotation
public class KineticRoadMachine extends Power {
	private static final double a = 0.004599;
	private static final double b = 0;
	private static final double c = 3.258988;
	private static final double d = 0;
	private final Cubic cubic = new Cubic();
	
	public int getPower(double speed, int resistance) {
		double power = (c * speed) + (a * (speed * speed * speed));
		
		return (int) power;
	}

	public String description() {
		return "Kinetic Road Machine/Rock And Roll";
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