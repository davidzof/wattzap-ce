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

/**
 * 
 * Represents a data point from a route or power file (gpx, rlv, pwr etc)
 * 
 * @author David George (c) Copyright 2013
 * @date 19 June 2013
 */
public class Point {
	private double latitude;
	private double longitude;
	private double elevation;
	private double distanceFromStart;
	private double gradientOrPower;
	private double speed;
	// private double level;
	private long time;

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public double getGradient() {
		return gradientOrPower;
	}

	public double getPower() {
		return gradientOrPower; // overload gradient with power
	}

	public void setGradient(double gradient) {
		this.gradientOrPower = gradient;
	}

	public void setPower(double power) {
		this.gradientOrPower = power;
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
				+ distanceFromStart + ", gradient=" + gradientOrPower
				+ ", speed=" + speed + ", time=" + time + "]";
	}
}
