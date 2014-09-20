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

import java.text.SimpleDateFormat;
import java.util.Date;

public class WorkoutData {
	// Power
	private int fiveSecondPwr;
	private int oneMinutePwr;
	private int fiveMinutePwr;
	private int twentyMinutePwr;

	private int totalPower;
	private int maxPower;
	private int avePower;
	private int quadraticPower;

	// heart rate
	private int maxHR = 0;
	private int aveHR = 0;
	private int minHR = 220;
	private int ftHR = 0;

	// cadence
	private int maxCadence = 0;
	private int aveCadence = 0;

	// ride
	private double distance;
	private long date;
	private long time;

	// personal
	private double weight;
	private int ftp;

	private int dataSource;
	
	private String tcxFile;
	
	private String description;
	
	// source type
	public static final int WATTZAP = 1;
	public static final int POWERMETER = 2;
	public static final int GPS = 3;
	public static final int FITLOG = 4;
	public static final int FIT = 5;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getTwentyMinutePwr() {
		return twentyMinutePwr;
	}

	public void setTwentyMinutePwr(int twentyMinutePwr) {
		this.twentyMinutePwr = twentyMinutePwr;
	}

	public int getTotalPower() {
		return totalPower;
	}

	public void setTotalPower(int totalPower) {
		this.totalPower = totalPower;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public int getFiveSecondPwr() {
		return fiveSecondPwr;
	}

	public void setFiveSecondPwr(int fiveSecondPwr) {
		this.fiveSecondPwr = fiveSecondPwr;
	}

	public int getOneMinutePwr() {
		return oneMinutePwr;
	}

	public void setOneMinutePwr(int oneMinutePwr) {
		this.oneMinutePwr = oneMinutePwr;
	}

	public int getFiveMinutePwr() {
		return fiveMinutePwr;
	}

	public void setFiveMinutePwr(int fiveMinutePwr) {
		this.fiveMinutePwr = fiveMinutePwr;
	}

	public int getMaxPower() {
		return maxPower;
	}

	public void setMaxPower(int maxPower) {
		this.maxPower = maxPower;
	}

	public int getAvePower() {
		return avePower;
	}

	public void setAvePower(int avePower) {
		this.avePower = avePower;
	}

	public int getQuadraticPower() {
		return quadraticPower;
	}

	public void setQuadraticPower(int quadraticPower) {
		this.quadraticPower = quadraticPower;
	}

	public int getMaxHR() {
		return maxHR;
	}

	public void setMaxHR(int maxHR) {
		this.maxHR = maxHR;
	}

	public int getAveHR() {
		return aveHR;
	}

	public void setAveHR(int aveHR) {
		this.aveHR = aveHR;
	}

	public int getMinHR() {
		return minHR;
	}

	public void setMinHR(int minHR) {
		this.minHR = minHR;
	}

	public int getFtHR() {
		return ftHR;
	}

	public void setFtHR(int ftHR) {
		this.ftHR = ftHR;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getTcxFile() {
		return tcxFile;
	}

	public void setTcxFile(String tcxFile) {
		this.tcxFile = tcxFile;
	}

	public double getIntensity() {
		if (ftp > 0) {
			return (double) quadraticPower / ftp;
		}

		return 0;
	}
	
	public String getDateAsString() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
		return sdf.format(new Date(date));	
	}

	/*
	 * IF^2 * Thours * 100
	 */
	public int getStress() {
		double iff = this.getIntensity();

		if (iff > 0) {
			return (int) (iff * iff * ((double)time / 36000));
		}

		return 0;
	}

	@Override
	public String toString() {
		return "WorkoutData [fiveSecondPwr=" + fiveSecondPwr
				+ ", oneMinutePwr=" + oneMinutePwr + ", fiveMinutePwr="
				+ fiveMinutePwr + ", twentyMinutePwr=" + twentyMinutePwr
				+ ", totalPower=" + totalPower + ", maxPower=" + maxPower
				+ ", avePower=" + avePower + ", quadraticPower="
				+ quadraticPower + ", maxHR=" + maxHR + ", aveHR=" + aveHR
				+ ", minHR=" + minHR + ", ftHR=" + ftHR + ", distance="
				+ distance + ", date=" + date + ", time=" + time + ", weight="
				+ weight + ", tcxFile=" + tcxFile + " IF " + getIntensity()
				+ " TSS " + getStress() + "]";
	}

	public int getMaxCadence() {
		return maxCadence;
	}

	public void setMaxCadence(int maxCadence) {
		this.maxCadence = maxCadence;
	}

	public int getAveCadence() {
		return aveCadence;
	}

	public void setAveCadence(int aveCadence) {
		this.aveCadence = aveCadence;
	}

	public int getFtp() {
		return ftp;
	}

	public void setFtp(int ftp) {
		this.ftp = ftp;
	}
	
	public void setSource(int source) {
		this.dataSource = source;
	}

	public int getSource() {
		return dataSource;
	}
	
	public String getSourceAsString() {
		switch(dataSource) {
		case 2:
			return "Power Meter";
		case 3:
			return "GPS";
		case 4:
			return "Fitlog";
		case 5:
			return "FIT";
		}
		
		return "Wattzap";
	}

	static public String dbTable12() {
		return "CREATE TABLE workouts(username VARCHAR(64), filename varchar(64), fivesecp INTEGER, oneminp INTEGER, fiveminp INTEGER , twentyminp INTEGER, qp INTEGER, totalp INTEGER, maxp INTEGER, avep INTEGER, ftp INTEGER, maxhr INTEGER, avehr INTEGER, minhr INTEGER, fthr INTEGER, maxcad INTEGER, avecad INTEGER, distance DOUBLE, weight DOUBLE, ridetime TIME, ridedate DATE, description varchar(256), source INTEGER  DEFAULT 1, primary key (username, filename))";
	}

	static public String delete() {
		return "DELETE FROM workouts WHERE username=? AND filename =?";
	}
	
	static public String insert() {
		return "INSERT INTO workouts VALUES (?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?)";
	}
	
	static public String select() {
		return "Select * FROM workouts WHERE username = ? ORDER BY ridedate";
	}
	
	static public String selectWorkout() {
		return "Select * FROM workouts WHERE username = ? and filename = ?";
	}
}