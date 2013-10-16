package com.wattzap.model.power;


/*
 * Kinetic Road Machine
 * see: http://www.kurtkinetic.com/powercurve.php
 * 
 * mph to kmh = 1.60934, 2.5899752356, 4.168150745660504
 * 
 * Resistance Options
 * P = 5.244820 * S + 0.019168 * S^3 (mph)
 * P = 3.258988 * S + 0.004599 * S^3
 * 
 * e.g for 25.91kmh
 * P = 84.44 + 79.995
 * P = 164 watts
 */
@PowerAnnotation
public class KineticRoadMachineProfile extends Power {
	public int getPower(double speed, int resistance) {
		double power = (3.258988 * speed) + (0.004599 * speed * speed * speed);

		return (int) power;
	}

	public String description() {
		return "Kinetic Road Machine";
	}

	@Override
	public int getResitanceLevels() {
		return 1;
	}
}