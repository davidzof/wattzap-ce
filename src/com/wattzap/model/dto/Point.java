package com.wattzap.model.dto;

import java.util.Date;

public class Point {
	private double latitude;
	private double longitude;
	private double elevation;
	private double distanceFromStart;
	private double gradient;
	private double speed;
	private double level;
	private long time;

	public double getSpeed() {
		return speed;
	}
	
	public void setSpeed(double speed) {
		this.speed = speed;
	}
	
	public double getLevel() {
		return level;
	}
	
	public void setLevel(double level) {
		this.level = level;
	}
	
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public double getGradient() {
		return gradient;
	}
	public void setGradient(double gradient) {
		this.gradient = gradient;
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
	public double getDistanceFromStart() {
		return distanceFromStart;
	}
	public void setDistanceFromStart(double distanceFromStart) {
		this.distanceFromStart = distanceFromStart;
	}

	@Override
	public String toString() {
		return "Point [latitude=" + latitude + ", longitude=" + longitude
				+ ", elevation=" + elevation + ", distanceFromStart="
				+ distanceFromStart + ", gradient=" + gradient + ", speed="
				+ speed + ", level=" + level + ", time=" + time + "]";
	}
}
