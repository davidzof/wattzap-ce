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
 * (c) 2015 David George / Wattzap.com
 * 
 * @author David George
 * @date 31 March 2015
 * 
 *  Elite Travel Generic Wind trainer
 *  
 *  y = 0.002x3 - 0.01x2 + 1.8191x + 0.8516
 *  
 *  http://www.mhstar.co.uk/Ebay/mhstarukltd/Sport/5661-0020B/3.jpg
 *  
 *  */
@PowerAnnotation
public class EliteTravelWind extends Power {
	private static final double a = 0.002;
	private static final double b = -0.01;
	private static final double c = 1.8191;
	private static final double d = 0.8516;
	private final Cubic cubic = new Cubic();
	
	public int getPower(double speed, int resistance) {
		double power = (a * (speed * speed * speed)) + (b * (speed * speed)) + (c * speed) ;
		
		return (int) power;
	}

	public String description() {
		return "Elite Travel Generic Wind Trainer";
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