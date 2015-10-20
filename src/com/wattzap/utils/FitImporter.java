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

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import javax.xml.bind.DatatypeConverter;

import com.garmin.fit.DateTime;
import com.garmin.fit.Decode;
import com.garmin.fit.Field;
import com.garmin.fit.Mesg;
import com.garmin.fit.MesgBroadcaster;
import com.garmin.fit.MesgDefinition;
import com.garmin.fit.MesgDefinitionListener;
import com.garmin.fit.MesgListener;
import com.wattzap.model.GPXReader;
import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.Telemetry;
import com.wattzap.model.dto.WorkoutData;
import com.wattzap.model.power.Power;

/**
 * Import Garmin FIT format files into WattzAp
 * 
 * @author David George
 * @date 22nd May 2014
 */
public class FitImporter implements MesgListener, MesgDefinitionListener {
	ArrayList<Telemetry> data = new ArrayList<Telemetry>();
	Telemetry last = null;
	double totalDistance = 0;
	private final UserPreferences userPrefs = UserPreferences.INSTANCE;
	public static final long OFFSET = 631065600000l; // Offset between Garmin
														// (FIT) time and Unix
														// time in ms (Dec 31,
														// 1989 - 00:00:00
														// January 1, 1970).
	private boolean isPower = false;
	Rolling gAve = new Rolling(30);

	public FitImporter(String fileName) {
		FileInputStream fitFile = null;

		try {
			Decode decode = new Decode();
			MesgBroadcaster broadcaster = new MesgBroadcaster(decode);
			decode.addListener((MesgDefinitionListener) this);
			decode.addListener((MesgListener) this);

			fitFile = new FileInputStream(fileName);
			broadcaster.run(fitFile);
			// calculate powers here???
			if (!isPower) {

				for (Telemetry point : data) {
					int p = (int) Power.getPower(userPrefs.getTotalWeight(),
							point.getGradient(), point.getSpeedKMH());

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
						//point.setResistance(p);
						point.setPower(p);
					} else {
						//point.setResistance(0);
						point.setPower(0);
					}
					
				}// for
			}
				/* else {
				double powerSum1 = 0;
				double powerSum2 = 0;
				double stdev = 0;
				int i = 0;
				
				for (Telemetry point : data) {
						double p = point.getPower();
				    powerSum1 += p;
				    powerSum2 += Math.pow(p, 2);
				    stdev = Math.sqrt(i*powerSum2 - Math.pow(powerSum1, 2))/i;
				    if (p>300)
				    System.out.println(">>" + p + " stdev " + stdev);
				    else
				    	System.out.println("" + p + " stdev " + stdev);
				    i++;
				}// for
			}*/
		} catch (Exception fex) {

			try {
				fitFile.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			fex.printStackTrace();
		} finally {
			if (fitFile != null) {
				try {
					fitFile.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void onMesgDefinition(MesgDefinition arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMesg(Mesg mesg) {
		if ((mesg != null) && ("lap".equals(mesg.getName()))) {
			// System.out.println("got here");
		} else if ((mesg != null) && ("record".equals(mesg.getName()))) {
			Telemetry point = new Telemetry();

			// Search relevant info
			Collection<Field> fields = mesg.getFields();

			for (Field f : fields) {
				f.getName();
			}

			Field time = getField("timestamp", fields);

			Field cadence = getField("cadence", fields);
			if (cadence != null) {
				point.setCadence(cadence.getByteValue());
			}

			Field hr = getField("heart_rate", fields);
			if (hr != null) {
				point.setHeartRate(hr.getIntegerValue());
			}

			Field posLat = getField("position_lat", fields);
			Field posLon = getField("position_long", fields);
			

			// Convert semicircles latitude to decimals
			if (posLat != null) {
				BigDecimal posLatDec = semicircleToDms(posLat);
				point.setLatitude(posLatDec.doubleValue());
			}
			if (posLon != null) {
				BigDecimal posLonDec = semicircleToDms(posLon);
				point.setLongitude(posLonDec.doubleValue());
			}

			Field elevation = getField("altitude", fields);
			if (elevation != null) {
				point.setElevation(elevation.getDoubleValue());
			}

			Field distance = getField("distance", fields);
			if (distance != null) {
				point.setDistanceMeters(distance.getDoubleValue());
			}

			if (time != null) {
				point.setTime(time.getLongValue() * 1000 + OFFSET);
			}

			Field speed = getField("speed", fields);
			if (speed != null) {
				point.setSpeed(speed.getDoubleValue() * 3.6);
			}

			// use power from file
			Field power = getField("power", fields);
			if (power != null) {

				point.setPower(power.getShortValue());
				if (point.getPower() > 0) {
					isPower = true; // contains power values
				}
			}

			if (last != null) {
				if (distance == null) {
					if (posLat == null || posLon == null) {
						// no latitude or longitude, drop
						// point
						return;
					}
					// calculate distance from GPS points
					double d = GPXReader.distance(point.getLatitude(),
							last.getLatitude(), point.getLongitude(),
							last.getLongitude(), point.getElevation(),
							last.getElevation());

					if (d > 1000) {
						// large value, drop.
						return;
					}
					totalDistance += d;
					point.setDistanceMeters(totalDistance);

				} else if (point.getDistanceMeters() == last.getDistanceMeters()) {
					// no change to distance, drop point
					return;
				}

				if (speed == null) {
					// calculate speed, s = d / t
					// double speed = d * 3600
					// / ((point.getTime() - adjust) - last.getTime());
				}

				double gradient = (point.getElevation() - last.getElevation())
						/ (point.getDistanceMeters() - last.getDistanceMeters());

				point.setGradient(gAve.add(gradient));
			} else {
				// first time through
				point.setResistance(WorkoutData.FIT);

			}
			last = point;
			data.add(point);
		}

	}

	/*
	 * Search the field with name fieldName in the list of fields.
	 */
	private Field getField(String fieldName, Collection<Field> fields) {
		for (Iterator<Field> iterator = fields.iterator(); iterator.hasNext();) {
			Field field = (Field) iterator.next();
			if (fieldName.equals(field.getName()))
				return field;
		}
		return null; // not found
	}

	/*
	 * Convert a fit field coördinate to a GPX coördinate. It will only convert
	 * fit fields with unit "semicircles".
	 * 
	 * dms=semicircles*(180/2^31)
	 */
	public static BigDecimal semicircleToDms(Field field) {
		BigDecimal dms = null;
		if ((field != null) && ("semicircles".equals(field.getUnits()))) {
			dms = semicircleToDms(field.getLongValue());
		}
		return dms; // 42.059009616
	}

	private static final BigDecimal MULTIPLICANT = new BigDecimal(
			180.0d / Math.pow(2L, 31L));

	/**
	 * Convert a semicircles coördinate (lattitude or longitude) value to a
	 * degrees value using the formula : dms=semicircles*(180/2^31)
	 * 
	 * s * (180.0D / 2^31)
	 * 
	 * 
	 * @see http 
	 *      ://www.gps-forums.net/accuracy-converting-semicircles-degrees-t31488
	 *      .html
	 */
	public static BigDecimal semicircleToDms(long semicircle) {
		BigDecimal dms = new BigDecimal(semicircle).multiply(MULTIPLICANT);
		return dms;
	}

	/**
	 * Convert the fit time field to the gpx time format (standard UTC time).
	 * Will only convert "s" seconds.
	 * 
	 * @param time
	 * @return
	 */
	public static String convertTime(Field time) {
		String result = null;
		if ((time != null) && ("s".equals(time.getUnits()))) {
			DateTime dateTime = new DateTime(time.getLongValue());
			result = convertTime(dateTime.getDate());
		}
		return result;
	}

	public static String convertTime(Date date) {
		TimeZone zone = TimeZone.getTimeZone("UTC");
		Calendar cal = Calendar.getInstance(zone);
		cal.setTime(date);
		return DatatypeConverter.printDateTime(cal);
	}

	
	

}
