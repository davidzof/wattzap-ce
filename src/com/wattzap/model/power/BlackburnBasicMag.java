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
 * mph to kmh = 1.60934, 2.5899752356, 4.168150745660504
 * 
 * 1: 1.6576 x speed + 0.4144 * speed ^ 2 + -0.00875 * speed ^3
 * 2: 2.688 x speed + 0.179 * speed ^2 + -0.008 * speed ^ 3
 * 3: 6.083 x speed + 0.251 * speed ^ 2 + -0.0029 * speed ^ 3
 * 
 * http://www.1upusa.com/product-trainer.html
 * 
 */
public class BlackburnBasicMag extends Power {
	private final Cubic cubic = new Cubic();
	
	public int getPower(double speed, int resistance) {
		double power = 0.0;

		switch (resistance) {
		case 1:
			power = (1.6576 * speed) + (0.4144 * speed * speed)
					- (0.00875 * speed * speed * speed);
			break;

		case 2:
		case 3:
		}

		return (int) power;
	}
	
	public double getSpeed(int power, int resistance) {
		double speed = 0;
		
		switch (resistance) {
		case 1:
			cubic.solve(-0.00875, 0.4144, 1.6576, -power);
			speed = cubic.x2;
			break;
		case 2:
		case 3:
		}
		
		return speed;
	}

	public String description() {
		return "Blackburn Basic Mag";
	}

	@Override
	public int getResitanceLevels() {
		return 3;
	}
}
