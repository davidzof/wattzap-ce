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
import java.util.Calendar;
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
 * Import external GPX data, for example from a smartphone or GPS.
 * 
 * The format is pretty simple for our purposes. We are principally interested
 * in track points.
 * 
 * @author David George
 * @date 2nd May 2014
 */
public class GpxImporter extends DefaultHandler {
	State currentState = State.UNDEFINED;
	StringBuilder buffer;
	// GPX files have two data formats
	private static final SimpleDateFormat msdateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	private final SimpleDateFormat timestampFormatter = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss'Z'");
	ArrayList<Telemetry> data;
	Telemetry point;
	double distance;
	Rolling rSpeed = new Rolling(30);
	Rolling gAve = new Rolling(30);
	LowPassFilter powerFilter = new LowPassFilter(2.0);
	long firstTime = 0;

	int index = 0;
	long adjust = 0;
	int tzOffset = 0;

	private final UserPreferences userPrefs = UserPreferences.INSTANCE;
	private static Logger logger = LogManager.getLogger("GPX Importer");

	public GpxImporter() {
		super();
		currentState = State.UNDEFINED;
		data = new ArrayList<Telemetry>();
		distance = 0;
		tzOffset = Calendar.getInstance().getTimeZone().getRawOffset() + Calendar.getInstance().getTimeZone().getDSTSavings();
	}

	public void startElement(String uri, String name, String qName,
			Attributes atts) {

		if (currentState == State.UNDEFINED) {
			if ("trkpt".equalsIgnoreCase(name)) {
				currentState = State.TRKPT;
				point = new Telemetry();
				point.setLatitude(Double.parseDouble(atts.getValue("lat")));
				point.setLongitude(Double.parseDouble(atts.getValue("lon")));
			}
		} else {
			buffer = new StringBuilder();
		}
	}

	public void endElement(String uri, String name, String qName) {
		if (currentState == State.TRKPT) {
			if ("time".equalsIgnoreCase(name)) {
				try {
					String t = buffer.toString().trim();
					if (t.length() == 20) {
						// there are two different formats of time stamp
						Date d = timestampFormatter.parse(t);
						point.setTime(d.getTime()+tzOffset);
					} else if (t.length() == 24) {
						Date d = msdateFormat.parse(t);
						point.setTime(d.getTime()+tzOffset);
					}

				} catch (ParseException e) {
					logger.error(e + " " + buffer);
				}
			}
			if ("ele".equalsIgnoreCase(name)) {
				point.setElevation(gAve.add(Double.parseDouble(buffer
						.toString())));

			} else if ("trkpt".equalsIgnoreCase(name)) {
				index++;
				int current = data.size();
				if (current > 0) {
					Telemetry last = data.get(current - 1);

					double d = GPXReader.distance(point.getLatitude(),
							last.getLatitude(), point.getLongitude(),
							last.getLongitude(), point.getElevation(),
							last.getElevation());

					// calculate speed, s = d / t
					double speed = d * 3600
							/ ((point.getTime() - adjust) - last.getTime());
					// TODO only do this if a FITLOG file is present
					if (speed < 0.5) {
						// less than 0.5km/h - we're stopped, remove point
						adjust += ((point.getTime() - adjust) - last.getTime());
						currentState = State.UNDEFINED;
						return; // drop this point
					}
					point.setTime(point.getTime() - adjust);

					speed = rSpeed.add(speed);

					double gradient = (point.getElevation() - last
							.getElevation()) / d;

					int p = (int) Power.getPower(userPrefs.getTotalWeight(),
							gradient, speed);

					// filter spikes
					p = (int) powerFilter.add(p);

					if (p > userPrefs.getMaxPower()
							&& (p > (last.getPower() * 2.0))) {
						// We are above FTP and power has doubled, remove power
						// spikes
						p = (int) (last.getPower() * 1.05);
					}
					if (p > (userPrefs.getMaxPower() * 4)) {
						// power is 4 x FTP, this is a spike
						p = last.getPower();
					}

					if (p > 0) {
						// only set power if it is greater than zero
						point.setPower(p);
					} else {
						point.setPower(0);
					}

					point.setSpeed(speed);
					distance += d;
					point.setDistanceMeters(distance);
					point.setGradient((gradient) * 100);
				} else {
					point.setDistanceMeters(0);
					point.setResistance(WorkoutData.GPS);
					firstTime = point.getTime();
				}
				data.add(point);
				currentState = State.UNDEFINED;
			}
		}
	}

	public void characters(char ch[], int start, int length) {
		if (buffer != null) {
			buffer.append(ch, start, length);
		}

	}

	public enum State {
		TRKPT, UNDEFINED
	}
}
