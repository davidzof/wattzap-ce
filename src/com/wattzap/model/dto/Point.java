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
 * @author David George (c) Copyright 2013-2016
 * @date 19 June 2013
 */
public class Point {
	private double latitude;
	private double longitude;
	private double elevation;
	private double distanceFromStart;
	private double gradient;
	private int power;
	private double speed;
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
		return gradient;
	}

	public int getPower() {
		return power; // overload gradient with power
	}

	public void setGradient(double gradient) {
		this.gradient = gradient;
	}

	public void setPower(int power) {
		this.power = power;
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
				+ distanceFromStart + ", gradient=" + gradient + ", power="
				+ power + ", speed=" + speed + ", time=" + time + "]";
	}
	
	public String getTcxExtensionsXml() {
		return "";
	}

	public static Point middlePoint(Point p1, Point p2, float ratio){
		Point p = new Point();
		p.latitude = ratio * p2.latitude + (1-ratio) * p1.latitude;
		p.longitude = ratio * p2.longitude + (1-ratio) * p1.longitude;
		p.elevation = ratio * p2.elevation + (1-ratio) * p1.elevation;
		p.distanceFromStart = ratio * p2.distanceFromStart + (1-ratio) * p1.distanceFromStart;
		p.gradient = ratio * p2.gradient + (1-ratio) * p1.gradient;
		p.power = (int)(ratio * p2.power + (1-ratio) * p1.power);
		p.speed = ratio * p2.speed + (1-ratio) * p1.speed;
		p.time = (long)(ratio * p2.time + (1-ratio) * p1.time);
		return p;
	}
}
