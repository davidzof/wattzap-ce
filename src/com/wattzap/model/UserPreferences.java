package com.wattzap.model;

import java.util.UUID;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.wattzap.model.power.Power;
import com.wattzap.model.power.PowerProfiles;

/**
 * Singleton helper to read/write user preferences to a backing store
 * 
 * @author David George / 15 September 2013 (C) Copyright 2013
 * 
 */
public enum UserPreferences {
	INSTANCE;
	Power powerProfile;
	String user;
	DataStore ds;
	private static int evalTime = 600;

	UserPreferences() {

		user = System.getProperty("user.name");
		String wd = getWD();
		ds = new DataStore(wd);
	}

	public double getWeight() {
		return getDouble("weight", 80.0);
	}

	public void setWeight(double weight) {
		setDouble("weight", weight);
	}

	public double getBikeWeight() {
		return getDouble("bikeweight", 10.0);
	}

	public double getTotalWeight() {
		return getDouble("weight", 80.0) + getDouble("bikeweight", 10.0);
	}

	public void setBikeWeight(double weight) {
		setDouble("bikeweight", weight);
	}

	// 2133 is 700Cx23
	public int getWheelsize() {
		return getInt(user, "wheelsize", 2133);
	}

	public double getWheelSizeCM() {
		return getInt(user, "wheelsize", 2133) / 10.0;
	}

	public void setWheelsize(int wheelsize) {
		setInt(user, "wheelsize", wheelsize);
	}

	public int getMaxHR() {
		return getInt(user, "maxhr", 0);
	}

	public void setMaxHR(int maxhr) {
		setInt(user, "maxhr", maxhr);
	}

	public int getMaxPower() {
		return getInt(user, "maxpower", 0);
	}

	public void setMaxPower(int maxPower) {
		setInt(user, "maxpower", maxPower);
	}

	public boolean isMetric() {
		return getBoolean("units", true);
	}

	public void setUnits(boolean value) {
		setBoolean("units", value);
	}

	public boolean isDebug() {
		return getBoolean("debug", false);
	}

	public void setDebug(boolean value) {
		setBoolean("debug", value);
	}
	
	
	public boolean isVirtualPower() {
		return getBoolean("virtualPower", false);
	}

	public void setVirtualPower(boolean value) {
		setBoolean("virtualPower", value);
	}

	public Power getPowerProfile() {
		String profile = get(user, "profile", "Tacx Satori / Blue Motion");

		if (powerProfile == null) {
			PowerProfiles pp = PowerProfiles.INSTANCE;
			powerProfile = pp.getProfile(profile);
		}
		return powerProfile;
	}

	public void setPowerProfile(String profile) {
		String p = get(user, "profile", null);
		if (!profile.equals(p)) {
			set(user, "profile", profile);
			PowerProfiles pp = PowerProfiles.INSTANCE;
			powerProfile = pp.getProfile(profile);
		}
	}

	public int getResistance() {
		return getInt(user, "resistance", 1);
	}

	public void setResistance(int r) {
		setInt(user, "resistance", r);
	}

	public int getSCId() {
		return getInt(user, "sandcId", 0);
	}

	public void setSCId(int i) {
		setInt(user, "sandcId", i);
	}

	public int getHRMId() {
		return getInt(user, "hrmid", 0);
	}

	public void setHRMId(int i) {
		setInt(user, "hrmid", i);
	}

	/** Registration Stuff **/
	public String getSerial() {
		String id = get("", "ssn", null);

		if (id == null) {
			// not yet initialized
			id = UUID.randomUUID().toString();
			set("", "ssn", id);

		}
		return id;
	}

	public boolean isRegistered() {
		if (get("", "rsnn", null) == null) {
			return false;
		}
		return true;
	}

	public String getRegistrationKey() {
		return get("", "rsnn", null);
	}

	public void setRegistrationKey(String key) {
		set("", "rsnn", key);
	}

	public int getEvalTime() {
		return getInt("", "evalTime", evalTime);
	}

	public void setEvalTime(int t) {
		setInt("", "evalTime", t);
	}

	public String getRouteDir() {
		return get("", "videoLocation", this.getAppData() + "/Routes");
	}

	public void setRouteDir(String s) {
		set("", "videoLocation", s);
	}

	// Data Access Functions

	private double getDouble(String key, double d) {
		String v = ds.getProp(user, key);
		if (v != null) {
			try {
				d = Double.parseDouble(v);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return d;
	}

	private void setDouble(String key, double d) {
		ds.insertProp(user, key, Double.toString(d));
	}

	private int getInt(String user, String key, int i) {
		String v = ds.getProp(user, key);
		if (v != null) {
			try {
				i = Integer.parseInt(v);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return i;
	}

	private void setInt(String user, String key, int i) {
		ds.insertProp(user, key, Integer.toString(i));
	}

	private boolean getBoolean(String key, boolean b) {
		String v = ds.getProp(user, key);
		if (v != null) {
			try {
				b = Boolean.parseBoolean(v);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return b;
	}

	private void setBoolean(String key, boolean b) {
		ds.insertProp(user, key, Boolean.toString(b));
	}

	private String get(String user, String key, String s) {
		String v = ds.getProp(user, key);
		if (v != null) {
			s = v;
		}

		return s;
	}

	private void set(String user, String key, String s) {
		ds.insertProp(user, key, s);
	}

	public void shutDown() {
		ds.close();

	}

	public String getWD() {
		String workingDirectory;
		// here, we assign the name of the OS, according to Java, to a
		// variable...
		String OS = (System.getProperty("os.name")).toUpperCase();
		// to determine what the workingDirectory is.
		// if it is some version of Windows
		if (OS.contains("WIN")) {
			// it is simply the location of the "AppData" folder
			workingDirectory = System.getenv("ALLUSERSPROFILE") + "/Wattzap";
		} else {
			// in either case, we would start in the user's home directory
			workingDirectory = System.getProperty("user.home") + "/.wattzap";
		}

		return workingDirectory;
	}

	public String getAppData() {
		String workingDirectory;
		// here, we assign the name of the OS, according to Java, to a
		// variable...
		String OS = (System.getProperty("os.name")).toUpperCase();
		// to determine what the workingDirectory is.
		// if it is some version of Windows
		if (OS.contains("WIN")) {
			// it is simply the location of the "AppData" folder
			workingDirectory = System.getenv("APPDATA") + "/Wattzap";
		} else {
			// in either case, we would start in the user's home directory
			workingDirectory = System.getProperty("user.home") + "/wattzap";
		}

		return workingDirectory;
	}
}
