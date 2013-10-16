package com.wattzap.model.dto;

import com.wattzap.model.UserPreferences;

public class TrainingItem {

	int hr;
	int power;
	int cadence;
	long time;
	String description;

	public double getHR() {
		return hr;
	}

	public void setMass(int hr) {
		this.hr = hr;
	}

	public int getHr() {
		return hr;
	}

	public void setHr(int hr) {
		this.hr = hr;
	}

	public int getPower() {
		return power;
	}

	public void setPower(int power) {
		this.power = power;
	}

	public int getCadence() {
		return cadence;
	}

	public void setCadence(int cadence) {
		this.cadence = cadence;
	}

	public long getTime() {
		return time;
	}

	public long getTimeInSeconds() {
		return time/1000;
	}

	public void setTime(long time) {
		this.time = time * 1000;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
