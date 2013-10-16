package com.wattzap.model.power;


/*
 * From https://groups.google.com/forum/#!msg/golden-cheetah-users/9f89rwYCD6c/PyAjwOwE_LkJ
 */
@PowerAnnotation
public class Flow extends Power {

	public int getPower(double speed, int resistance) {
		double[] slope = { 4.74, 6.36, 7.75, 9.51, 11.03, 12.81, 14.37 };
		double[] intercept = { -34.9, -43.57, -47.27, -66.69, -72.59, -95.05,
				-102.43 };
		resistance--;
		int power = (int) ((speed * slope[resistance]) + intercept[resistance]);
		if (power < 0) {
			return 0;
		}
		return power;
	}

	public String description() {
		return "Tacx Flow";
	}

	@Override
	public int getResitanceLevels() {
		return 7;
	}
}
