package com.wattzap.model.power;

public abstract class Power {
	/**
	 * Returns power in watts for a given trainer resistance
	 * 
	 * @param speed in km/h
	 * @param resistance
	 */
	public abstract int getPower(double speed, int resistance);
	
	/**
	 * Returns a description of the Power class
	 * @return
	 */
	public abstract String description();
	
	public abstract int getResitanceLevels();
	
	/*
	 * P = krMs + kaAsv2d+ giMs
	 * 
	 * P = 0.45s + 0.37s³ + 35.28s
	 * 
	 * where
	 * 
	 * P = power required (in watts) kr= rolling resistance coefficient M = mass
	 * of bike + rider s = speed of the bike on the road ka= wind resistance
	 * coefficient A = the frontal area of the bike and rider v = speed of the
	 * bike through the air (i.e. bike speed + headwind or – tailwind) d = air
	 * density g = gravitational constant i = gradient (an approximation²)
	 */

	public double getRealSpeed(double M, double slope, double power) {
		double kr = 0.005;
		double ka = 0.5;
		double A = 0.6;
		double d = 1.266;
		double g = 9.81;

		double c = (kr * M) + (g * slope * M);
		double a = ka * A * d;

		Cubic cubic = new Cubic();
		cubic.solve(a, 0, c, 0.0 - power);
		
		return cubic.x1;
	}
}
