package com.wattzap.model.power;


/*
 * CycleOps power curve for this trainer (the Jet Fluid Pro
 * http://www.e-scape.co.uk/e-scape-blogs/jason-stratford/creating-a-virtual-power-meter/
 * Joe Meir: -0.46095+2.4455x+0.2232x^2+0.01844x^3
 * 
 * http://thebikegeek.blogspot.fr/2009/12/while-we-wait-for-better-and-better.html
 * 
 *  Armann: Power = 1.5981 x + 0.006942 x^3
 *  
 *  y = 0.0115x3 - 0.0137x2 + 8.9788x
 *  p = 0.00276x3 - 0.0052896x2 + 5.579x
 *  mph to kmh = 1.60934, 2.5899752356, 4.168150745660504
 *  
 *  
 *  
 */
public class CyclopsJetFluidPro extends Power {
	public int getPower(double speed, int resistance) {

		//double power = -0.46095 + (2.4455 * speed) + (0.2232 * speed * speed)
		//		+ (0.01844 * speed * speed * speed);
		
		double power =  (5.579 * speed) + (-0.0052896 * speed * speed)
				+ (0.00276 * speed * speed * speed);
		
		return (int) power;
	}

	public String description() {
		return "CycleOps Jet Fluid Pro";
	}

	@Override
	public int getResitanceLevels() {
		return 1;
	}
}
