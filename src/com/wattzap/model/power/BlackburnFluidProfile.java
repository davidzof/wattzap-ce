package com.wattzap.model.power;

/*
 * Blackburn Basic Mag
 * 
 * mph to kmh = 1.60934, 2.5899752356, 4.168150745660504
 * 
 * Resistance Options
 * 1: 9.00528 * speed + 0.009996 * speed ^3
 * 
 * Speed in MPG
 */
public class BlackburnFluidProfile extends Power {
	public int getPower(double speed, int resistance) {
		double power = 0.0;

		
			power = (4.93333 * speed) + (0.2 * speed * speed)
					+ (-0.00433 * speed * speed * speed);



		return (int) power;
	}

	public String description() {
		return "Blackburn Fluid";
	}

	@Override
	public int getResitanceLevels() {
		return 3;
	}
}
