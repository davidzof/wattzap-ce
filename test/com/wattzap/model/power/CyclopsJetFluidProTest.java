package com.wattzap.model.power;

import org.junit.Before;
import org.junit.Test;

import com.wattzap.model.power.PowerProfiles;

public class CyclopsJetFluidProTest {
	CyclopsJetFluidPro p;

	@Before
	public void setup() {
		p = new CyclopsJetFluidPro();
	}

	@Test
	public void checkPower() {
		System.out.println("speed,power");
		for (int resistance = 0; resistance < p.getResitanceLevels(); resistance++) {
			for (int speed = 10; speed < 50; speed++) {
				int power = p.getPower(speed, resistance);
				System.out.printf("%d,%d\n", speed, power);
			}

		}
	}

}
