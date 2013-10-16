package com.wattzap.model.power;


@PowerAnnotation
public class SatoriBlueMotion extends Power {

	public int getPower(double speed, int resistance) {
		double[] slope = { 3.73, 5.33, 6.87, 8.27, 10.07, 11.4, 13.13, 14.4,
				15.93, 17.73 };
		double[] intercept = { -28.67, -36.67, -43.33, -47.33, -66.33, -67.00,
				-83.67, -82.00, -89.67, -114.67 };
		resistance--;
		int power = (int) ((speed * slope[resistance]) + intercept[resistance]);
		if (power < 0) {
			return 0;
		}
		return power;
	}

	public String description() {
		return "Tacx Satori / Blue Motion";
	}

	@Override
	public int getResitanceLevels() {
		return 10;
	}
}
