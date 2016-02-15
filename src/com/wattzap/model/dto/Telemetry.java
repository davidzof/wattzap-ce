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

import java.io.Serializable;

/**
 * Data object for telemetry coming from the ANT Speed, Cadence and Heart Rate
 * Sensors
 * 
 * @author David George (c) Copyright 2013
 * @date 19 June 2013
 */
public class Telemetry extends Point implements Serializable {
	private static final double KMTOMILES = 1.609344;

	private int cadence;
	private double distance;
	private int heartRate;
	private long time;
	private int resistance;

	public Telemetry() {
		setSpeed(-1);
		setPower(-1);
		setLatitude(91);
		setLongitude(181);
		heartRate = -1;
	}

	public Telemetry(Telemetry t) {
		setSpeed(t.getSpeed());
		cadence = t.cadence;
		distance = t.distance;
		setPower(t.getPower());
		setElevation(t.getElevation());
		setGradient(t.getGradient());
		setLatitude(t.getLatitude());
		setLongitude(t.getLongitude());
		heartRate = t.heartRate;
		time = t.time;
	}

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

	public double getSpeedMPH() {
		return getSpeed() / KMTOMILES;
	}

	public double getSpeedKMH() {
		return getSpeed();
	}
	
	public void setCadence(int cadence) {
		this.cadence = cadence;
	}

	/**
	 * Distance in meters
	 */
	public void setDistanceMeters(double distance) {
		this.distance = distance;
	}

	public double getDistanceMeters() {
		return distance;
	}

	public double getDistanceKM() {
		return distance / 1000;
	}

	public double getDistanceMiles() {
		return distance / (KMTOMILES * 1000);
	}

	public int getCadence() {
		return cadence;
	}

	// for player only mode
	public void setVirtualSpeed(double v) {
		cadence = (int) v;

	}

	public double getTrainerSpeed() {
		return cadence;
	}

	// for player only mode
	public void setResistance(int v) {
		resistance = v;

	}

	public int getResistance() {
		return resistance;
	}

	@Override
	public String toString() {
		return "Telemetry [cadence=" + cadence
				+ ", distance=" + distance + ", heartRate=" + heartRate + " tt " + heartRate + ", time="
				+ time / 1000 + "]" + super.toString();
	}
	
	@Override
	public String getTcxExtensionsXml() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("          <HeartRateBpm>");
		sb.append("<Value>");
		sb.append(heartRate);
		sb.append("</Value>");
		sb.append("</HeartRateBpm>\n");
		sb.append("          <Cadence>");
		sb.append(Math.min(254, cadence));
		sb.append("</Cadence>\n");
		sb.append("          <Extensions>");
		sb.append("<TPX xmlns=\"http://www.garmin.com/xmlschemas/ActivityExtension/v2\">");
		sb.append("<Watts>");
		sb.append(getPower());
		sb.append("</Watts>");
		sb.append("<Speed>");
		sb.append(getSpeedKMH());
		sb.append("</Speed>");
		sb.append("</TPX></Extensions>");
		
		return sb.toString();
	}
}
