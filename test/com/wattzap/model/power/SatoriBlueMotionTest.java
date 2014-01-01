package com.wattzap.model.power;

import org.junit.Before;
import org.junit.Test;

import com.wattzap.model.power.PowerProfiles;

public class SatoriBlueMotionTest {
	Power p;

	@Before
	public void setup() {
		p = new SatoriBlueMotion();
	}

	@Test
	public void checkPower() {
		System.out.println("speed,power");

		for (int speed = 0; speed < 60; speed++) {
			for (int resistance = 1; resistance <= p.getResitanceLevels(); resistance++) {
				if (resistance == 1) {
					System.out.printf("%d", speed);
				}
				int power = p.getPower(speed, resistance);
				System.out.printf(",%d",power);
			}
			System.out.println();

		}
	}

}
