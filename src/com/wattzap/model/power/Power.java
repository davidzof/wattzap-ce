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

public abstract class Power {
	private int neutral = getNeutral();
	private double posDivider = 1;
	private double negDivider = 1;

	/**
	 * Returns power in watts for a given trainer resistance
	 * 
	 * @param speed
	 *            in km/h
	 * @param resistance
	 */
	public abstract int getPower(double speed, int resistance);

	/**
	 * Returns a description of the Power class
	 * 
	 * @return
	 */
	public abstract String description();

	public abstract int getResitanceLevels();

	public int getNeutral() {
		return 3;
	}

	public abstract double getSpeed(int power, int resistance);

	public void setGrades(double max, double min) {

		int levels = getResitanceLevels();

		int posLevels = levels - neutral;
		int negLevels = levels - posLevels - 1;

		posDivider = max / posLevels;
		negDivider = min / negLevels;
	}

	public int getResistance(double gradient) {
		int r = neutral;
		if (gradient >= 0 && posDivider > 0) {
			r = (int) (neutral + (gradient / posDivider));
		} else if (negDivider < 0) {
			r = (int) (neutral - (gradient / negDivider));
		}

		return r;
	}

	/*
	 * P = krMs + kaAsv2d+ giMs
	 * 
	 * P = 0.45s + 0.37s³ + 35.28s
	 * 
	 * where
	 * 
	 * P = power required (in watts)
	 * 
	 * kr= rolling resistance coefficient
	 * 
	 * M = mass of bike + rider
	 * 
	 * s = speed of the bike on the road
	 * 
	 * ka= wind resistance coefficient
	 * 
	 * A = the frontal area of the bike and rider
	 * 
	 * v = speed of the bike through the air (i.e. bike speed + headwind or
	 * tailwind)
	 * 
	 * d = air density
	 * 
	 * g = gravitational constant
	 */
	public double getRealSpeed(double mass, double slope, double power) {
		double kr = 0.005;
		double ka = 0.5;
		double A = 0.6;
		double d = 1.266;
		double g = 9.81;

		double c = (kr * mass) + (g * slope * mass);
		double a = ka * A * d;

		Cubic cubic = new Cubic();
		cubic.solve(a, 0, c, 0.0 - power);

		return cubic.x1;
	}

	/**
	 * P = krMs + kaAsv2d+ giMs
	 * 
	 * P = 0.45s + 0.37s³ + 35.28s
	 * 
	 * where
	 * 
	 * P = power required (in watts)
	 * 
	 * kr= rolling resistance coefficient
	 * 
	 * M = mass of bike + rider
	 * 
	 * s = speed of the bike on the road
	 * 
	 * ka= wind resistance coefficient
	 * 
	 * A = the frontal area of the bike and rider
	 * 
	 * v = speed of the bike through the air (i.e. bike speed + headwind or
	 * tailwind)
	 * 
	 * d = air density
	 * 
	 * g = gravitational constant
	 * 
	 * i = gradient (an approximation)
	 */
	public static int getPower(double mass, double slope, double speed) {
		// temperature = 25 # degrees C
		// pressure = 101325 # pascals
		// bike_rider_mass = 81 # kg
		// cda = 0.324 # rider on hoods

		speed = speed / 3.6; // convert to m/s

		int power = getRiderPower(0.324, 25, 101325, speed, mass,
				Surface.ASPALT, slope, 0);
		return power;
	}

	/*
	 * Compute force due to aerodynamic drag.
	 * 
	 * Formula: 0.5(CdA)(density)V^3
	 * 
	 * @param cda - drag coefficient
	 * 
	 * @param temp - air temperature
	 * 
	 * @param pressure - air pressure
	 * 
	 * @param speed - speed in meters/second
	 * 
	 * Typical figures are: Sea Level 1.226 1500 Meters 1.056 3000 Meters 0.905
	 */
	private static double getDrag(double cda, double temp, double pressure,
			double speed) {
		double airDensity = pressure / (287.05 * (temp + 273.15));
		return 0.5 * cda * airDensity * (speed * speed * speed);
	}

	/**
	 * Compute rolling resistance force. Formula: gm(Croll)
	 */
	private static double getRollingResistance(double mass, Surface surface,
			double speed) {
		double cr = 0.004; // ASPHALT - default

		// Typical rolling resistance values for different surfaces
		switch (surface) {
		case TRACK:
			cr = 0.001;
			break;
		case CONCRETE:
			cr = 0.002;
			break;
		case ROUGH:
			cr = 0.008;
		}

		return 9.81 * mass * cr * speed;
	}

	/*
	 * Compute force required to change elevation.
	 * 
	 * Formula: gm(slope)
	 */
	private static double getGravityForce(double mass, double gradient,
			double speed) {
		return 9.81 * mass * gradient * speed;
	}

	/*
	 * Compute force required to achieve acceleration.
	 * 
	 * Formula: F = ma
	 * 
	 * a = (speed change)^2} / 2 x time to change speed
	 */
	private static double getAccelerationForce(double mass,
			double acceleration, double speed) {
		return mass * acceleration * speed;
	}

	/*
	 * Compute the rider's power output.
	 * 
	 * Formula is: (drag + rolling resistance + climbing force + acceleration
	 * force) * velocity
	 */
	public static int getRiderPower(double cda, double temp, double pressure,
			double speed, double mass, Surface surface, double gradient,
			double acceleration) {
		double drag = getDrag(cda, temp, pressure, speed);
		double roll = getRollingResistance(mass, Surface.ASPALT, speed);
		double grav = getGravityForce(mass, gradient, speed);
		double accel = getAccelerationForce(mass, acceleration, speed);
		int power = (int) (drag + roll + grav + accel + 0.5); // 0.5 will cause
																// rounding up
																// or down

		if (power < 0) {
			power = 0;
		}
		return power;
	}

	// Note that the inputs are now declared as doubles.
	public double quadraticEquationRoot1(double a, double b, double c) {
		double root1, root2; // This is now a double, too.
		root1 = (-b + Math.sqrt(Math.pow(b, 2) - 4 * a * c)) / (2 * a);
		root2 = (-b - Math.sqrt(Math.pow(b, 2) - 4 * a * c)) / (2 * a);
		return Math.max(root1, root2);
	}

	public enum Surface {
		TRACK, CONCRETE, ASPALT, ROUGH
	}
}
