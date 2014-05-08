package com.wattzap.model.power;

/*
 * Blackburn Basic Mag
 * 
 * Resistance Options - these seem high
 * 1: 14.493 * speed + 0.0416648 * speed ^3
 */

public class BlackburnFluid extends Power {
	private static final double a = 0.0416648;
	private static final double b = 0;
	private static final double c = 14.493;
	private static final double d = 0;
	private final Cubic cubic = new Cubic();

	public int getPower(double speed, int resistance) {
		double power = 0.0;

		power = (c * speed) + (a * speed * speed * speed);

		return (int) power;
	}

	public double getSpeed(int power, int resistance) {
		cubic.solve(a, b, c, d - power);
		return cubic.x1;
	}

	public String description() {
		return "Blackburn Fluid";
	}

	@Override
	public int getResitanceLevels() {
		return 1;
	}
}
