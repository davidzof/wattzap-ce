package com.wattzap.model;

import com.wattzap.model.GPXReader;

public class GPXDataTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GPXReader gpxData = new GPXReader();
		gpxData.load("multiseg.gpx");
	}

}
