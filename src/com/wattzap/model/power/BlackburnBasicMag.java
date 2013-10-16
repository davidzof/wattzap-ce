package com.wattzap.model.power;

/*
 * Blackburn Basic Mag
 * 
 * Resistance Options
 * 1: 1.03 x speed + 0.16 * speed ^ 2 + -0.0021 * speed ^3
 * 2: 1.67 x speed + 0.179 * speed ^2 + -0.008 * speed ^ 3
 * 3: 3.78 x speed + 0.251 * speed ^ 2 + -0.0029 * speed ^ 3
 * 
 * Speed in MPG
 * 
 * http://www.1upusa.com/product-trainer.html
 * 
 */
public class BlackburnBasicMag extends Power {
	public int getPower(double speed, int resistance) {
		double power = 0.0;

		switch (resistance) {
		case 1:
			power = (4.93333 * speed) + (0.2 * speed * speed)
					+ (-0.00433 * speed * speed * speed);

		case 2:
		case 3:
		}

		System.out.println("Mag Elite power " + power);

		return (int) power;
	}

	public String description() {
		return "Blackburn Basic Mag";
	}

	@Override
	public int getResitanceLevels() {
		return 3;
	}
}
