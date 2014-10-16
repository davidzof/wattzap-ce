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
 * Interface for loading route files. Should be subclassed to implement a new file type.
 * 
 * @author David George (c) Copyright 2013
 * @date 19 November 2013
 */
public abstract class RouteReader {
	// Slope (Integer) 0 = Watt program, 1 = Slope program, 2 = Pulse (HR)
	public final static int POWER = 0;
	public final static int SLOPE = 1;
	public final static int HEARTRATE = 2;
	Point[] points = null;
	int currentPoint = 0;

	public abstract String getExtension();

	public abstract String getFilename();

	public abstract String getName();

	public abstract GPXFile getGpxFile();

	/**
	 * Used by profile view, gives distance/altitude values
	 * 
	 * @return
	 */
	public abstract XYSeries getSeries();

	/**
	 * returns a point relative to the current position
	 */
	public Point getPoint(double distance) {
		while (currentPoint < points.length
				&& (points[currentPoint].getDistanceFromStart() < (distance * 1000))) {
			currentPoint++;
		}// while
		if (currentPoint == points.length) {
			return null;
		} else if (currentPoint > 0) {
			return points[currentPoint - 1];
		} else {
			return points[0];
		}
	}

	/**
	 * Returns a Point relative to the start of the track, resets current point
	 * 
	 * @param distance
	 * @return
	 */
	public Point getAbsolutePoint(double distance) {
		if (points != null) {
			currentPoint = 0;
			return getPoint(distance);
		}
		return null;
	}

	/**
	 * Returns Point immediately before distance
	 * 
	 * @return
	 */
	public abstract Point[] getPoints();

	public abstract void load(String filename);

	public abstract void close();

	public abstract double getDistanceMeters();

	public abstract int routeType();

	public abstract double getMaxSlope();

	public abstract double getMinSlope();
}
