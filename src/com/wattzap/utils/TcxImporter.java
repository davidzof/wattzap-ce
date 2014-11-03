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
package com.wattzap.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import com.wattzap.model.GPXReader;
import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.Telemetry;
import com.wattzap.model.dto.WorkoutData;
import com.wattzap.model.power.Power;

/**
 * Import TCX Format files
 * 
 * @author David George
 * @date 2nd May 2014
 */
public class TcxImporter extends DefaultHandler {
	State currentState = State.UNDEFINED;
	StringBuilder buffer;
	protected static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	private final SimpleDateFormat timestampFormatter;
	ArrayList<Telemetry> data;
	Telemetry point;
	double distance = 0;

	Rolling rSpeed = new Rolling(20);
	Rolling pAve = new Rolling(20);
	ExponentialMovingAverage gradeAve = new ExponentialMovingAverage(0.8);

	private final UserPreferences userPrefs = UserPreferences.INSTANCE;
	private static Logger logger = LogManager.getLogger("TCX Importer");

	public TcxImporter() {
		super();
		currentState = State.UNDEFINED;
		timestampFormatter = new SimpleDateFormat(TIMESTAMP_FORMAT);
		data = new ArrayList<Telemetry>();
	}

	public void startElement(String uri, String name, String qName,
			Attributes atts) {
		if (currentState == State.TRACKPOINT) {
			// Only if we are in a TRACKPOINT state can we enter any of these
			// states
			if ("Cadence".equalsIgnoreCase(name)) {
				buffer = new StringBuilder();
			} else if ("Time".equalsIgnoreCase(name)) {
				buffer = new StringBuilder();
			} else if ("DistanceMeters".equalsIgnoreCase(name)) {
				buffer = new StringBuilder();
			} else if ("AltitudeMeters".equalsIgnoreCase(name)) {
				buffer = new StringBuilder();
			} else if ("HeartRateBpm".equalsIgnoreCase(name)) {
				buffer = new StringBuilder();
			} else if ("Extensions".equalsIgnoreCase(name)) {
				currentState = State.EXTENSIONS;
			} else if ("Position".equalsIgnoreCase(name)) {
				currentState = State.POSITION;
			}
		} else if (currentState == State.EXTENSIONS) {
			if ("Watts".equalsIgnoreCase(name)) {
				buffer = new StringBuilder();
			} else if ("Speed".equalsIgnoreCase(name)) {
				buffer = new StringBuilder();
			}
		} else if (currentState == State.POSITION) {
			if ("LatitudeDegrees".equalsIgnoreCase(name)) {
				buffer = new StringBuilder();
			} else if ("LongitudeDegrees".equalsIgnoreCase(name)) {
				buffer = new StringBuilder();
			}
		} else if ("Trackpoint".equalsIgnoreCase(name)) {
			point = new Telemetry();

			currentState = State.TRACKPOINT;
		} else if ("DistanceMeters".equalsIgnoreCase(name)) {
			buffer = new StringBuilder();
		}
	}

	public void endElement(String uri, String name, String qName) {

		try {
			if (currentState == State.TRACKPOINT) {
				// Only if we are in a TRACKPOINT state can we enter any of
				// these
				// states
				if ("Cadence".equalsIgnoreCase(name)) {
					int cadence = Integer.parseInt(buffer.toString().trim());
					point.setCadence(cadence);
					currentState = State.TRACKPOINT;

				} else if ("Time".equalsIgnoreCase(name)) {
					String tt = buffer.toString().trim();

					Date d = timestampFormatter.parse(tt);
					point.setTime(d.getTime());
					currentState = State.TRACKPOINT;
				} else if ("HeartRateBpm".equalsIgnoreCase(name)) {
					int hr = Integer.parseInt(buffer.toString().trim());
					point.setHeartRate(hr);
					currentState = State.TRACKPOINT;
				} else if ("DistanceMeters".equalsIgnoreCase(name)) {
					double distance = Double.parseDouble(buffer.toString()
							.trim());
					point.setDistanceMeters(distance);
					currentState = State.TRACKPOINT;
				} else if ("AltitudeMeters".equalsIgnoreCase(name)) {
					double altitude = Double.parseDouble(buffer.toString()
							.trim());
					point.setElevation(altitude);
					currentState = State.TRACKPOINT;
				} else if ("Trackpoint".equalsIgnoreCase(name)) {
					// finalize data
					int current = data.size();
					if (current > 0) {
						Telemetry last = data.get(current - 1);

						double d = GPXReader.distance(point.getLatitude(),
								last.getLatitude(), point.getLongitude(),
								last.getLongitude(), point.getElevation(),
								last.getElevation());

						if (point.getSpeedKMH() == -1) {
							// calculate speed, s = d / t
							double speed = rSpeed.add(d * 3600
									/ (point.getTime() - last.getTime()));
							point.setSpeed(speed);
						}
						double gradient = (point.getElevation() - last
								.getElevation()) / d;

						if (point.getPower() == -1) {
							int p = (int) pAve.add(Power.getPower(
									userPrefs.getTotalWeight(), gradient,
									point.getSpeedKMH()));

							if (p > userPrefs.getMaxPower()
									&& (p > (last.getPower() * 2.0))) {
								// We are above FTP and power has doubled,
								// remove power
								// spikes
								p = (int) (last.getPower() * 1.05);
							}
							if (p > (userPrefs.getMaxPower() * 4)) {
								// power is 4 x FTP, this is a spike
								p = last.getPower();
							}
							if (p > 0) {
								point.setPower(p);
							}
						}

						distance += d;
						point.setDistanceMeters(distance);
						point.setGradient((gradient) * 100);
					} else {
						if (point.getPower() != -1) {
							point.setResistance(WorkoutData.POWERMETER);
						} else {
							point.setResistance(WorkoutData.GPS);
						}
					}
					data.add(point);

					currentState = State.UNDEFINED;
				}
			} else if (currentState == State.EXTENSIONS) {
				if ("Watts".equalsIgnoreCase(name)) {
					int power = Integer.parseInt(buffer.toString().trim());
					point.setPower(power);
				} else if ("Speed".equalsIgnoreCase(name)) {
					double speed = Double.parseDouble(buffer.toString().trim());
					point.setSpeed(speed);
				} else if ("Extensions".equalsIgnoreCase(name)) {
					currentState = State.TRACKPOINT;
				}
			} else if (currentState == State.POSITION) {
				if ("LatitudeDegrees".equalsIgnoreCase(name)) {
					double latitude = Double.parseDouble(buffer.toString()
							.trim());
					point.setLatitude(latitude);
				} else if ("LongitudeDegrees".equalsIgnoreCase(name)) {
					double longitude = Double.parseDouble(buffer.toString()
							.trim());
					point.setLongitude(longitude);
				} else if ("Position".equalsIgnoreCase(name)) {
					currentState = State.TRACKPOINT;
				}
			}
			if ("DistanceMeters".equalsIgnoreCase(name)) {
				distance = Double.parseDouble(buffer.toString().trim());
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void characters(char ch[], int start, int length) {
		if (buffer != null) {
			buffer.append(ch, start, length);
		}
		// System.out.println("buffer " + buffer);
	}

	public enum State {
		TIME, HR, CADENCE, WATTS, SPEED, TRACKPOINT, EXTENSIONS, POSITION, UNDEFINED
	}
}
