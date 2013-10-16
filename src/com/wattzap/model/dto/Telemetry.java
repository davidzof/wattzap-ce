package com.wattzap.model.dto;


public class Telemetry {
	private double speed;
	private int cadence;
	private double distance = 0.0;
	private int power;
	private double elevation;
	private double gradient;
	private double latitude;
	private double longitude;
	private int heartRate = -1;
	private long time;

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public int getHeartRate() {
		return heartRate;
	}

	public void setHeartRate(int heartRate) {
		this.heartRate = heartRate;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getElevation() {
		return elevation;
	}

	public void setElevation(double elevation) {
		this.elevation = elevation;
	}

	public double getGradient() {
		return gradient;
	}

	public void setGradient(double gradient) {
		this.gradient = gradient;
	}

	public int getPower() {
		return power;
	}

	public void setPower(int power) {
		this.power = power;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public void setCadence(int cadence) {
		this.cadence = cadence;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public double getSpeed() {
		return speed;
	}

	public int getCadence() {
		return cadence;
	}

	@Override
	public String toString() {
		return "Telemetry [speed=" + speed + ", cadence=" + cadence
				+ ", distance=" + distance + ", power=" + power
				+ ", elevation=" + elevation + ", gradient=" + gradient
				+ ", latitude=" + latitude + ", longitude=" + longitude
				+ ", heartRate=" + heartRate + "]";
	}

	public double getDistance() {
		return distance;
	}

}
