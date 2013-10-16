package com.wattzap.model;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.jfree.data.xy.XYSeries;

import com.gpxcreator.gpxpanel.GPXFile;
import com.gpxcreator.gpxpanel.Track;
import com.gpxcreator.gpxpanel.Waypoint;
import com.gpxcreator.gpxpanel.WaypointGroup;
import com.wattzap.model.dto.Point;
import com.wattzap.utils.Rolling;

/*
 * Wrapper class for GPX Track. Performs some analysis such as calculating instantaneous speed, average gradient etc.
 * 
 * Roller resistance calculated from power graphs
 * 
 * Pwr = (mass cyclist + mass bike) * 9.8 * slope (0.1) * m/s; // rolling resistance?
 * so if power is 250 w and we are generating xyz we either need to peddle faster or increase resistance.
 * For example a Satori can only simulate up to a 4.5% slope
 * 
 * @author David George
 * @date 11 June 2013
 */
public class GPXData {
	private GPXFile gpxFile;
	private XYSeries series;
	private Point[] points;
	private int currentPoint = 0;
	private String fileName;
	private static final int gradientDistance = 100; // distance to calculate
														// gradients over.
	private long startTime = 0;

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public String getFilename() {
		return fileName;
	}

	public GPXFile getGpxFile() {
		return gpxFile;
	}

	public XYSeries getSeries() {
		return series;
	}

	public Point[] getPoints() {
		return points;
	}

	public Point getCoords(double distance) {

		int index = 0;
		while ((points[index].getDistanceFromStart() < (distance * 1000))) {
			index++;
			if (index == points.length) {
				return null;
			}
		}

		if (index > 0) {
			currentPoint = index - 1;
		}

		return points[currentPoint];
	}

	/*
	 * 
	 */
	public GPXData(String filename) {
		fileName = filename;
		gpxFile = new GPXFile(new File(filename));

		List<Track> routes = gpxFile.getTracks();

		Track route = routes.get(0);
		if (route == null) {
			System.err.println("no route in GPX file");
			return;
		}

		List<WaypointGroup> segs = route.getTracksegs();
		this.series = new XYSeries("");

		double distance = 0.0;
		long startTime = System.currentTimeMillis();

		/*
		 * A GPX file can contain more than 1 segment. There may, or may not, be
		 * a distance gap between segments. For example, due to a tunnel. We
		 * treat each segment independently even if they correspond to a
		 * contiguous video.
		 */
		for (WaypointGroup group : segs) {
			Rolling altitude = new Rolling(10);
			List<Waypoint> waypoints = group.getWaypoints();

			// group.correctElevation(true);
			Point[] segment = new Point[group.getNumPts()];

			Waypoint last = null;
			int index = 0;
			for (Waypoint wp : waypoints) {
				if (index == 0) {
					last = wp;
				}

				Point p = new Point();
				p.setElevation(wp.getEle());
				p.setLatitude(wp.getLat());
				p.setLongitude(wp.getLon());
				p.setTime(wp.getTime().getTime() - startTime);
				double leg = distance(wp.getLat(), last.getLat(), wp.getLon(),
						last.getLon(), last.getEle(), wp.getEle());
				distance += leg;
				p.setDistanceFromStart(distance);

				// smooth altitudes a bit
				altitude.add(wp.getEle());
				series.add(distance / 1000, altitude.getAverage());

				// speed = distance / time
				long t = wp.getTime().getTime() - last.getTime().getTime();
				p.setSpeed((leg * 3600 / t));

				segment[index++] = p;
				last = wp;
			}// for

			// set initial speed
			segment[0].setSpeed(segment[1].getSpeed());

			/*
			 * Calculate the gradient, we do this using blocks of 100 meters
			 * using a moving average of 10 values.
			 */
			int i = 0;
			int j = 0;
			Rolling gradient = new Rolling(10);
			for (Point p : segment) {
				if (p.getDistanceFromStart() > segment[i]
						.getDistanceFromStart() + gradientDistance) {
					double slope = 100
							* (p.getElevation() - segment[i].getElevation())
							/ (p.getDistanceFromStart() - segment[i]
									.getDistanceFromStart());
					gradient.add(slope);
					segment[i++].setGradient(gradient.getAverage());
				}
				j++;
			}

			while (i < j - 1) {
				double slope = 100
						* (segment[j - 1].getElevation() - segment[i]
								.getElevation())
						/ (segment[j - 1].getDistanceFromStart() - segment[i]
								.getDistanceFromStart());
				gradient.add(slope);
				segment[i++].setGradient(gradient.getAverage());
			}
			segment[i++].setGradient(gradient.getAverage());
			// gradient done

			// resistance levels - use blocks of 500 meters
			// levels done

			// combine segment
			points = ArrayUtils.addAll(points, segment);
		}
	}

	/**
	 * Calculate distance between two points in latitude and longitude taking
	 * into account height difference. If you are not interested in height
	 * difference pass 0.0. Uses Haversine method as its base.
	 * 
	 * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
	 * el2 End altitude in meters
	 */
	private static double distance(double lat1, double lat2, double lon1,
			double lon2, double el1, double el2) {

		final int R = 6371; // Radius of the earth

		Double latDistance = deg2rad(lat2 - lat1);
		Double lonDistance = deg2rad(lon2 - lon1);
		Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
				+ Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
				* Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
		Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distance = R * c * 1000; // convert to meters

		double height = el1 - el2;

		distance = Math.pow(distance, 2) + Math.pow(height, 2);

		return Math.sqrt(distance);
	}

	private static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}
}
