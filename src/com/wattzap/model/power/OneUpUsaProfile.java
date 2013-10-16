package com.wattzap.model.power;

/*
 * 1UpUsa Trainer
 * 
 * Resistance Options
 * 6 pockets: 6.0341 + -1.3882 x speed + 0.2543 x speed^2 + 0.0388 x speed ^ 3
 * CPR A-2000 (Black Flyweight): 25 + 2.65 x speed + -0.415 x speed^2 + 0.058 x speed ^ 3
 * Inner pockets: 10.82 + -2.0392 x speed + 0.3586 x speed^2 + 0.0097 x speed ^ 3
 * Outer pockets: 11.61 + -1.8042 x speed + 0.364 x speed^2 + 0.0103 x speed ^ 3
 * 
 * Speed in MPG
 * 
 * http://www.1upusa.com/product-trainer.html
 * 
 */
public class OneUpUsaProfile extends Power {
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
