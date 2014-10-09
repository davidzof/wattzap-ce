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
package com.wattzap.model;

import org.jfree.data.xy.XYSeries;

import com.gpxcreator.gpxpanel.GPXFile;
import com.wattzap.model.dto.Point;

/*
 * Interface for loading route files
 * 
 * @author David George (c) Copyright 2013
 * @date 19 November 2013
 */
public interface RouteReader {
	// Slope (Integer) 0 = Watt program, 1 = Slope program, 2 = Pulse (HR)
	public final static int POWER = 0;
	public final static int SLOPE = 1;
	public final static int HEARTRATE = 2;
	
	public String getExtension();
	
	public String getFilename();

	public String getName();

	public GPXFile getGpxFile();

	/**
	 * Used by profile view, gives distance/altitude values
	 * 
	 * @return
	 */
	public XYSeries getSeries();

	/**
	 * Returns Point immediately before distance
	 * 
	 * @return
	 */
	public Point[] getPoints();

	/**
	 * returns a point relative to the current position
	 */
	public Point getPoint(double distance);

	/**
	 * Returns a Point relative to the start of the track, resets current point
	 * 
	 * @param distance
	 * @return
	 */
	public Point getAbsolutePoint(double distance);
	
	public void load(String filename);

	public void close();
	
	public double getDistanceMeters();

	public int routeType();

	public double getMaxSlope();

	public double getMinSlope();
}
