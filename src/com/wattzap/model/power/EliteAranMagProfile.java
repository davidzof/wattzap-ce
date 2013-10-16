package com.wattzap.model.power;

/*
 * Elite Aran Mag Rollers
 * 
 * Has 3 levels:
 * 
 * Level 0: -0.00433x3 + 0.2x2 + 4.93333x
 * Level 1: -0.001x3 + 0.095x2 + 6.75x 
 * Level 2: -0.0055x3 + 0.435x2 + 4.4x
 * 
 * From here and here http://www.had2know.com/academics/cubic-through-4-points.html
 * 
 */
public class EliteAranMagProfile extends Power {
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
		return "Elite Aran Mag";
	}

	@Override
	public int getResitanceLevels() {
		return 3;
	}
}
