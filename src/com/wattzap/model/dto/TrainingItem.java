/* This file is part of Wattzap Community Edition.
 *
 * Wattzap Community Edtion is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Wattzap Community Edition is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Wattzap.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.wattzap.model.dto;

import com.wattzap.model.UserPreferences;

/* 
 * @author David George (c) Copyright 2013
 * @date 19 August 2013
 */
public class TrainingItem {
	int hrLow = 0;
	int hrHigh;
	int powerLow = 0;
	int powerHigh;
	int cadenceLow = 0;
	int cadenceHigh;

	int heartRate;
	int power;
	int cadence;
	long time;
	double distance;
	String description;

	public int getHr() {
		return heartRate;
	}

	public String getHRMsg() {
		String msg = "";
		if (hrLow != 0) {
			if (hrHigh != 0) {
				msg = " HR: " + hrLow + " to " + hrHigh + " bpm";
			} else {
				msg = " HR > " + hrLow + " bpm";
			}
		} else if (hrHigh != 0) {
			msg = " HR < " + hrHigh + " bpm";
		}

		return msg;

	}

	/**
	 * Says if Heart Rate is within range
	 * 
	 * @param hr
	 * @return
	 */
	public int isHRInRange(int hr) {
		if (hr >= hrLow) {
			if (hr <= hrHigh || hrHigh == 0) {
				return 0;
			} else {
				return 1;
			}
		}

		return -1;
	}

	public void setHr(String v) {
		v = v.trim();
		if (v.charAt(0) == '<') {
			// less than
			heartRate = Integer.parseInt(v.substring(1));
			heartRate = (heartRate * UserPreferences.INSTANCE.getMaxHR()) / 100;
			hrHigh = heartRate;
			hrLow = 0;
		} else if (v.charAt(0) == '>') {
			// greater than
			heartRate = Integer.parseInt(v.substring(1));
			heartRate = (heartRate * UserPreferences.INSTANCE.getMaxHR()) / 100;
			hrHigh = 0;
			hrLow = heartRate;
		} else if (v.indexOf('-') != -1) {
			int i = v.indexOf('-');
			hrLow = (Integer.parseInt(v.substring(0, i).trim()) * UserPreferences.INSTANCE
					.getMaxHR()) / 100;
			hrHigh = (Integer.parseInt(v.substring(i + 1).trim()) * UserPreferences.INSTANCE
					.getMaxHR()) / 100;
			heartRate = hrLow + ((hrHigh - hrLow) / 2);
		} else {
			heartRate = Integer.parseInt(v.trim());
			if (heartRate <= 5) {
				// training level
				switch (heartRate) {
				case 1:
					// active recovery < 68%
					hrHigh = (int) ((double) UserPreferences.INSTANCE
							.getMaxHR() * 0.68);
					hrLow = 0;
					heartRate = hrHigh;
					break;
				case 2:
					// Endurance 69 - 83%
					hrHigh = (int) ((double) UserPreferences.INSTANCE
							.getMaxHR() * 0.75);
					hrLow = (int) ((double) UserPreferences.INSTANCE.getMaxHR() * 0.69);
					heartRate = hrLow + ((hrHigh - hrLow) / 2);
					break;
				case 3:
					// Tempo 84 - 94%
					hrHigh = (int) ((double) UserPreferences.INSTANCE
							.getMaxHR() * 0.95);
					hrLow = (int) ((double) UserPreferences.INSTANCE.getMaxHR() * 0.84);
					heartRate = hrLow + ((hrHigh - hrLow) / 2);
					break;
				case 4:
					// Lactate Threshold 95-105%
					hrHigh = (int) ((double) UserPreferences.INSTANCE
							.getMaxHR() * 1.05);
					hrLow = (int) ((double) UserPreferences.INSTANCE.getMaxHR() * 0.96);
					heartRate = hrLow + ((hrHigh - hrLow) / 2);
					break;
				case 5:
					// VO2Max
					hrHigh = 0;
					hrLow = (int) ((double) UserPreferences.INSTANCE.getMaxHR() * 1.06);
					heartRate = hrLow;
					break;
				}
			} else {
				// percentage of max hearrate
				heartRate = (heartRate * UserPreferences.INSTANCE.getMaxHR()) / 100;
				hrHigh = (int) ((double) heartRate * 1.02);
				hrLow = (int) ((double) heartRate * 0.98);
			}
		}

	}

	public int getPower() {
		return power;
	}

	public String getPowerMsg() {
		String msg = "";
		if (powerLow != 0) {
			if (powerHigh != 0) {
				msg = " Power : " + powerLow + " to " + powerHigh + " watts";
			} else {
				msg = " Power > " + powerLow + " watts";
			}
		} else if (powerHigh != 0) {
			msg = " Power < " + powerHigh + " watts";
		}

		return msg;

	}

	/**
	 * Says if power is within range
	 * 
	 * @param power
	 * @return
	 */
	public int isPowerInRange(int power) {
		if (power >= powerLow) {
			if (power <= powerHigh || powerHigh == 0) {
				return 0;
			} else {
				return 1;
			}
		}

		return -1;
	}

	public void setPower(double power) {
		// absolute power in watts
		this.power = (int) power;
		powerHigh = (int) (power * 1.025);
		powerLow = (int) (power * 0.975);
	}

	public void setPower(String v) {
		v = v.trim();
		if (v.charAt(0) == '<') {
			// less than
			power = Integer.parseInt(v.substring(1));
			power = (power * UserPreferences.INSTANCE.getMaxPower()) / 100;
			powerHigh = power;
			powerLow = 0;
		} else if (v.charAt(0) == '>') {
			// greater than
			power = Integer.parseInt(v.substring(1));
			power = (power * UserPreferences.INSTANCE.getMaxPower()) / 100;
			powerHigh = 0;
			powerLow = power;
		} else if (v.indexOf('-') != -1) {
			int i = v.indexOf('-');
			powerLow = (Integer.parseInt(v.substring(0, i).trim()) * UserPreferences.INSTANCE
					.getMaxPower()) / 100;
			powerHigh = (Integer.parseInt(v.substring(i + 1).trim()) * UserPreferences.INSTANCE
					.getMaxPower()) / 100;
			power = powerLow + ((powerHigh - powerLow) / 2);
		} else if (v.indexOf('w') != -1) {
			// absolute power in watts
			power = Integer.parseInt(v.substring(0, v.indexOf('w')).trim());
			powerHigh = (int) ((double) power * 1.025);
			powerLow = (int) ((double) power * 0.975);
		} else {
			power = Integer.parseInt(v.trim());
			if (power <= 7) {
				// training level
				switch (power) {
				case 1:
					// active recovery < 55%
					powerHigh = (int) ((double) UserPreferences.INSTANCE
							.getMaxPower() * 0.55);
					powerLow = 0;
					power = powerHigh;
					break;
				case 2:
					// Endurance 56 - 75%
					powerHigh = (int) ((double) UserPreferences.INSTANCE
							.getMaxPower() * 0.75);
					powerLow = (int) ((double) UserPreferences.INSTANCE
							.getMaxPower() * 0.56);
					power = powerLow + ((powerHigh - powerLow) / 2);
					break;
				case 3:
					// Tempo 76 - 90%
					powerHigh = (int) ((double) UserPreferences.INSTANCE
							.getMaxPower() * 0.9);
					powerLow = (int) ((UserPreferences.INSTANCE.getMaxPower()) * 0.66);
					power = powerLow + ((powerHigh - powerLow) / 2);
					break;
				case 4:
					// Lactate Threshold 91-105%
					powerHigh = (int) ((double) UserPreferences.INSTANCE
							.getMaxPower() * 1.05);
					powerLow = (int) ((double) UserPreferences.INSTANCE
							.getMaxPower() * 0.91);
					power = powerLow + ((powerHigh - powerLow) / 2);
					break;
				case 5:
					// VO2Max 106-120
					powerHigh = (int) ((double) UserPreferences.INSTANCE
							.getMaxPower() * 1.2);
					powerLow = (int) ((double) UserPreferences.INSTANCE
							.getMaxPower() * 1.06);
					power = powerLow + ((powerHigh - powerLow) / 2);
					break;
				case 6:
					// Anaerobic Capacity
					powerHigh = (int) ((double) UserPreferences.INSTANCE
							.getMaxPower() * 1.50);
					powerLow = (int) ((double) UserPreferences.INSTANCE
							.getMaxPower() * 1.21);
					power = powerLow + ((powerHigh - powerLow) / 2);
					break;
				case 7:
					// Neuromuscular
					powerHigh = 0;
					powerLow = (int) ((double) UserPreferences.INSTANCE
							.getMaxPower() * 1.50);
					power = powerLow;
					break;
				}
			} else {
				// percentage of max power
				power = (power * UserPreferences.INSTANCE.getMaxPower()) / 100;
				powerHigh = (int) ((double) power * 1.025);
				powerLow = (int) ((double) power * 0.975);
			}
		}

	}

	public int getCadence() {
		return cadence;
	}

	public String getCadenceMsg() {
		String msg = "";
		if (cadenceLow != 0) {
			if (cadenceHigh != 0) {
				msg = " Cadence: " + cadenceLow + " to " + cadenceHigh + " rpm";
			} else {
				msg = " Cadence > " + cadenceLow + " rpm";
			}
		} else if (cadenceHigh != 0) {
			msg = " Cadence < " + cadenceHigh + " rpm";
		}

		return msg;

	}

	/**
	 * Says if Heart Rate is within range
	 * 
	 * @param hr
	 * @return
	 */
	public int isCadenceInRange(int c) {
		if (c >= cadenceLow) {
			if (c <= cadenceHigh || cadenceHigh == 0) {
				return 0;
			} else {
				return 1;
			}
		}

		return -1;
	}

	public void setCadence(String v) {
		v = v.trim();
		if (v.charAt(0) == '<') {
			// less than
			cadence = Integer.parseInt(v.substring(1));
			cadenceHigh = cadence;
			cadenceLow = 0;
		} else if (v.charAt(0) == '>') {
			// greater than
			cadence = Integer.parseInt(v.substring(1));
			cadenceHigh = 0;
			cadenceLow = cadence;
		} else if (v.indexOf('-') != -1) {
			int i = v.indexOf('-');
			cadenceLow = Integer.parseInt(v.substring(0, i).trim());
			cadenceHigh = Integer.parseInt(v.substring(i + 1).trim());
			cadence = cadenceLow + ((cadenceHigh - cadenceLow) / 2);
		} else {
			// absolute power in watts
			cadence = Integer.parseInt(v.trim());
			cadenceHigh = (int) ((double) cadence * 1.025);
			cadenceLow = (int) ((double) cadence * 0.975);
		}

	}

	public long getTime() {
		return time;
	}

	public long getTimeInSeconds() {
		return time / 1000;
	}

	public void setTime(long time) {
		this.time = time * 1000;
	}

	public void setTimeMillis(long time) {
		this.time = time;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	@Override
	public String toString() {
		return "TrainingItem [power=" + power + ", time=" + time + "]";
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public static int getTrainingLevel(int power) {
		// active recovery < 55%
		int level1 = (int) ((double) UserPreferences.INSTANCE.getMaxPower() * 0.55);
		// Endurance 56 - 75%
		int level2 = (int) ((double) UserPreferences.INSTANCE.getMaxPower() * 0.75);
		// Tempo 76 - 90%
		int level3 = (int) ((double) UserPreferences.INSTANCE.getMaxPower() * 0.9);
		// Lactate Threshold 91-105%
		int level4 = (int) ((double) UserPreferences.INSTANCE.getMaxPower() * 1.05);
		// VO2Max 106-120
		int level5 = (int) ((double) UserPreferences.INSTANCE.getMaxPower() * 1.2);
		// Anaerobic Capacity
		int level6 = (int) ((double) UserPreferences.INSTANCE.getMaxPower() * 1.50);
		// Neuromuscular

		if (power >= 0 && power <= level1) {
			return 1;
		} else if (power > level1 && power <= level2) {
			return 2;
		} else if (power > level2 && power <= level3) {
			return 3;
		} else if (power > level3 && power <= level4) {
			return 4;
		} else if (power > level4 && power <= level5) {
			return 5;
		} else if (power > level5 && power <= level6) {
			return 6;
		}
		return 7;
	}

	public static String getTrainingName(int level) {
		switch (level) {
		case 1:
			return "Active Recovery";
		case 2:
			return "Endurance";
		case 3:
			return "Tempo";
		case 4:
			return "Lactate";
		case 5:
			return "VO2Max";
		case 6:
			return "Anaerobic";
		}

		return "Neuromuscular";
	}
}
