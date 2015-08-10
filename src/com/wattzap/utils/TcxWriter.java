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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.wattzap.model.RouteReader;
import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.Telemetry;

/**
 * Write out a a track in the Garmin training center database, tcx format. As
 * defined by: http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2
 * 
 * The TCX file written by this class has been verified as compatible with
 * Garmin Training Center 3.5.3.
 * 
 * @author Sandor Dornbush
 * @author David George
 */
public class TcxWriter /* implements TrackWriter */{
	protected static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	protected static final String FILE_TIMESTAMP_FORMAT = "yyyyMMMdd-HHmmss";
	public final static String WORKOUTDIR = "/Workouts/";

	// These are the only sports allowed by the TCX v2 specification for fields
	// of type Sport_t.
	private static final String TCX_SPORT_BIKING = "Biking";

	RouteReader routeData;

	// Values for fields of type Build_t/Type.
	private static final String TCX_TYPE_RELEASE = "Release";
	private static final String TCX_TYPE_INTERNAL = "Internal";

	private final SimpleDateFormat timestampFormatter;
	private final static SimpleDateFormat fileTSFormatter = new SimpleDateFormat(
			FILE_TIMESTAMP_FORMAT);

	private PrintWriter pw = null;

	private static Logger logger = LogManager.getLogger("TCX Writer");

	public TcxWriter() {
		timestampFormatter = new SimpleDateFormat(TIMESTAMP_FORMAT);
		timestampFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	public void close() {
		if (pw != null) {
			pw.close();
			pw = null;
		}
	}

	public void writeHeader() {
		if (pw == null) {
			return;
		}
		pw.format("<?xml version=\"1.0\" encoding=\"UTF8\" standalone=\"no\" ?>\n");
		pw.print("<TrainingCenterDatabase ");
		pw.print("xmlns=\"http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2\" ");
		pw.print("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
		pw.print("xsi:schemaLocation=\"http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2 ");
		pw.println("http://www.garmin.com/xmlschemas/TrainingCenterDatabasev2.xsd\">");
		pw.println();
	}

	public void writeStartTrack(Telemetry start, Telemetry end) {
		if (pw == null) {
			return;
		}

		String startTime = timestampFormatter.format(new Date(start.getTime()));

		pw.println("  <Activities>");
		pw.format("    <Activity Sport=\"%s\">\n", TCX_SPORT_BIKING);
		pw.format("      <Id>%s</Id>\n", startTime);
		pw.format("      <Lap StartTime=\"%s\">\n", startTime);
		pw.print("        <TotalTimeSeconds>");
		pw.print((end.getTime() - start.getTime()) / 1000);
		pw.println("</TotalTimeSeconds>");
		pw.print("        <DistanceMeters>");
		pw.print(end.getDistanceMeters());
		pw.println("</DistanceMeters>");
		// TODO max speed etc.
		// Calories are a required element just put in 0.
		pw.print("<Calories>0</Calories>");
		pw.println("<Intensity>Active</Intensity>");
		pw.println("<TriggerMethod>Manual</TriggerMethod>");
	}

	public void writeOpenSegment() {
		if (pw != null) {
			pw.println("      <Track>");
		}
	}

	public void writeLocation(Telemetry t, int gpsData) {
		if (pw == null) {
			return;
		}

		pw.println("        <Trackpoint>");
		Date d = new Date(t.getTime());

		pw.println("          <Time>" + timestampFormatter.format(d)
				+ "</Time>");

		if (gpsData == 0) {
			pw.println("          <Position>");

			pw.print("            <LatitudeDegrees>");
			pw.print(t.getLatitude());
			pw.println("</LatitudeDegrees>");

			pw.print("            <LongitudeDegrees>");
			pw.print(t.getLongitude());
			pw.println("</LongitudeDegrees>");

			pw.println("          </Position>");

		}
		pw.print("          <AltitudeMeters>");
		pw.print(t.getElevation());
		pw.println("</AltitudeMeters>");
		pw.print("          <DistanceMeters>");
		pw.print(t.getDistanceMeters());
		pw.println("</DistanceMeters>");
		pw.print("          <HeartRateBpm>");
		pw.print("<Value>");
		pw.print(t.getHeartRate());
		pw.print("</Value>");
		pw.println("</HeartRateBpm>");
		pw.print("          <Cadence>");
		pw.print(Math.min(254, t.getCadence()));
		pw.println("</Cadence>");
		pw.print("          <Extensions>");
		pw.print("<TPX xmlns=\"http://www.garmin.com/xmlschemas/ActivityExtension/v2\">");
		pw.print("<Watts>");
		pw.print(t.getPower());
		pw.print("</Watts>");
		pw.print("<Speed>");
		pw.print(t.getSpeedKMH());
		pw.print("</Speed>");
		pw.println("</TPX></Extensions>");
		pw.println("        </Trackpoint>");
	}

	void writeCloseSegment() {
		if (pw != null) {
			pw.println("      </Track>");
		}
	}

	public void writeEndTrack() {
		if (pw == null) {
			return;
		}
		pw.println("      </Lap>");
		pw.print("      <Creator xsi:type=\"Device_t\">");
		pw.format("<Name>Wattzap Analyzer running on %s</Name>\n", "2.0.0");

		// The following code is correct. ID is inconsistently capitalized in
		// the TCX schema.
		pw.println("<UnitId>0</UnitId>");
		pw.println("<ProductID>0</ProductID>");

		writeVersion();

		pw.println("</Creator>");
		pw.println("    </Activity>");
		pw.println("  </Activities>");
	}

	public void writeFooter() {
		if (pw == null) {
			return;
		}
		pw.println("  <Author xsi:type=\"Application_t\">");

		// We put the version in the name because there isn't a better place for
		// it. The TCX schema tightly defined the Version tag, so we can't put
		// it
		// there. They've similarly constrained the PartNumber tag, so it can't
		// go
		// there either. pw.format("<Name>My Tracks %s by Google</Name>\n",

		pw.println("<Build>");

		writeVersion();

		pw.format("<Type>%s</Type>\n", TCX_TYPE_RELEASE);
		pw.println("</Build>");
		pw.format("<LangID>%s</LangID>\n", Locale.getDefault().getLanguage());
		pw.println("<PartNumber>000-00000-00</PartNumber>");
		pw.println("</Author>");
		pw.println("</TrainingCenterDatabase>");
	}

	/*
	 * @Override public String getExtension() { return
	 * TrackFileFormat.TCX.getExtension(); }
	 */
	private void writeVersion() {
		if (pw == null) {
			return;
		}
		String version = "1.1.1";

		// Splitting the myTracks version code into VersionMajor, VersionMinor
		// and BuildMajor
		// to fit the integer type requirement for these fields in the TCX spec.
		// Putting a string like "x.x.x" into VersionMajor breaks XML
		// validation.
		// We also set the BuildMinor version to 1 if this is a development
		// build to
		// signify that this build is newer than the one associated with the
		// version code given in BuildMajor.

		String[] myTracksVersionComponents = version.split("\\.");

		pw.println("<Version>");
		pw.format("<VersionMajor>%d</VersionMajor>\n",
				Integer.valueOf(myTracksVersionComponents[0]));
		pw.format("<VersionMinor>%d</VersionMinor>\n",
				Integer.valueOf(myTracksVersionComponents[1]));

		// TCX schema says these are optional but http://connect.garmin.com only
		// accepts the TCX file when they are present.
		pw.format("<BuildMajor>%d</BuildMajor>\n",
				Integer.valueOf(myTracksVersionComponents[2]));
		pw.format("<BuildMinor>%d</BuildMinor>\n", 1);
		pw.println("</Version>");
	}

	/**
	 * 
	 * @param data
	 * @param gpsData
	 *            0 - save GPS data, 1 - drop GPS data
	 * @return
	 */
	public String save(ArrayList<Telemetry> data, int gpsData) {
		String fileName = null;
		if (data == null || data.size() == 0) {
			logger.info("No training data to save");
			return fileName;
		}

		Telemetry firstPoint = data.get(0);
		Telemetry lastPoint = data.get(data.size() - 1);

		fileName = getWorkoutName(firstPoint.getTime());
		File file = new File(UserPreferences.INSTANCE.getUserDataDirectory()
				+ WORKOUTDIR + fileName);

		try {
			// make sure parent directory exists
			File parent = file.getParentFile();
			if (!parent.exists()) {
				parent.mkdirs();
			}
			file.createNewFile();

			pw = new PrintWriter(file);

			writeHeader();
			writeStartTrack(firstPoint, lastPoint);
			writeOpenSegment();

			Telemetry last = null;
			for (Telemetry t : data) {
				if (t.getLatitude() > 90 || t.getLongitude() > 180) {
					// No GPS data to save
					writeLocation(t, 1);
				} else {
					if (gpsData == 0 && last != null
							&& last.getLatitude() == t.getLatitude()
							&& last.getLongitude() == t.getLongitude()
							&& t.getLatitude() != 0 && t.getLongitude() != 0) {
						/*
						 * We are saving GPS data, it is the same GPS Point and
						 * the data is valid (not 0,0), drop it.
						 */
						continue;
					} else {
						writeLocation(t, gpsData);
					}
				}
				last = t;
			}

			writeCloseSegment();
			writeEndTrack();
			writeFooter();
			pw.flush();
		} catch (FileNotFoundException e1) {
			logger.error(e1.getLocalizedMessage() + " "
					+ file.getAbsolutePath());
		} catch (IOException e1) {
			logger.error(e1.getLocalizedMessage() + " "
					+ file.getAbsolutePath());
		} finally {
			close();
		}

		return fileName;
	}

	public static String getWorkoutName(long time) {
		Calendar date = (new GregorianCalendar());
		date.setTimeInMillis(time);
		int season = date.get(Calendar.YEAR);

		String workoutName = fileTSFormatter.format(new Date(time));

		return season + "/" + workoutName + ".tcx";
	}
}
