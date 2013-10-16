package com.wattzap.model.power;


/*
 * Kinetic Rock and Roll
 * see: http://www.kurtkinetic.com/
 * 
 * mph to kmh = 1.60934, 2.5899752356, 4.168150745660504
 * 
 * Resistance Options
 * P = 1.293811 * S + 0.351211 * S^2 + 0.021354 * S^3 (mph)
 * P = 0.803939 * S + 0.135604 * S^2 + 0.005123 * S^3 (kph)
 * 
 * e.g for 25.91kmh
 * P = 84.44 + 79.995
 * P = 164 watts
 */
@PowerAnnotation
public class KineticRockAndRoll extends Power {
	public int getPower(double speed, int resistance) {
		double power = (0.803939 * speed) + (0.135604 * speed * speed) + (0.005123 * speed * speed * speed);

		return (int) power;
	}

	public String description() {
		return "Kinetic Rock and Roll";
	}

	@Override
	public int getResitanceLevels() {
		return 1;
	}
}