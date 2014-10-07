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
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.Telemetry;
import com.wattzap.model.dto.WorkoutData;
import com.wattzap.view.training.TrainingAnalysis;

/**
 * Imports Training Activities into the Wattzap database
 * 
 * @author David George
 * @date 2nd May 2014
 */
public class ActivityReader  {
	String workoutDir;
	List<String> importedFiles = new ArrayList<String>();
	private Logger logger = LogManager.getLogger("GPSFileVisitor");


	public List<String> getImportedFileList() {
		return importedFiles;
	}

	public void readActivity(String fileName) {
			try {
				ArrayList<Telemetry> telemetry = readTelemetry(fileName);
				if (telemetry != null) {

					String workoutName = TcxWriter.getWorkoutName(telemetry
							.get(0).getTime());
					WorkoutData workout = UserPreferences.INSTANCE
							.getWorkout(workoutName);
					int dataSource = telemetry.get(0).getResistance();

					if (workout != null) {
						logger.info("File already in database "
								+ workout.getTcxFile());
					} else {
						workout = TrainingAnalysis.analyze(telemetry);
						workout.setFtp(UserPreferences.INSTANCE.getMaxPower());
						workout.setTcxFile(workoutName);
						workout.setSource(dataSource);

						TcxWriter writer = new TcxWriter();
						importedFiles.add(writer.save(telemetry, 0));

						UserPreferences.INSTANCE.addWorkout(workout);
					}

				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				/*
				 * C:\Documents and Settings\david\Application
				 * Data\Wattzap\Imports\Col-des-Ayes-8may2014.fitlog
				 * org.xml.sax.SAXParseException; lineNumber: 1; columnNumber:
				 * 1; Content is not allowed in prolog. at
				 * com.sun.org.apache.xerces
				 * .internal.util.ErrorHandlerWrapper.createSAXParseException
				 * (ErrorHandlerWrapper.java:198) at
				 * com.sun.org.apache.xerces.internal
				 * .util.ErrorHandlerWrapper.fatalError
				 * (ErrorHandlerWrapper.java:177) at
				 * com.sun.org.apache.xerces.internal
				 * .impl.XMLErrorReporter.reportError(XMLErrorReporter.java:441)
				 */
				e.printStackTrace();
			}
	}

	public static ArrayList<Telemetry> readTelemetry(String fileName)
			throws SAXException, IOException {
		ArrayList<Telemetry> data = null;
		if (fileName.endsWith(".tcx")) {
			XMLReader xr = XMLReaderFactory.createXMLReader();

			TcxImporter handler = new TcxImporter();
			xr.setContentHandler(handler);
			xr.setErrorHandler(handler);

			FileReader r = new FileReader(fileName);

			xr.parse(new InputSource(r));

			data = handler.data;
			r.close();
			Telemetry last = data.get(data.size() - 1);
			last.setDistance(handler.distance);
			return data;
		} else if (fileName.endsWith(".fit")) {
			FitImporter handler = new FitImporter(fileName);
			return handler.data;
		} else if (fileName.endsWith(".gpx")) {
			XMLReader xr = XMLReaderFactory.createXMLReader();
			GpxImporter handler = new GpxImporter();
			xr.setContentHandler(handler);
			xr.setErrorHandler(handler);

			FileReader r = new FileReader(fileName);
			xr.parse(new InputSource(r));

			data = handler.data;
			r.close();
			FitlogImporter flHandler = new FitlogImporter();
			xr.setContentHandler(flHandler);
			xr.setErrorHandler(flHandler);

			// Merge Fitlog if it exists
			fileName = fileName.substring(0, fileName.length() - 3); // trim gpx
			File f = new File(fileName + "fitlog");
			if (f.exists()) {
				r = new FileReader(f);
				xr.parse(new InputSource(r));

				ArrayList<Telemetry> fitData = flHandler.data;
				r.close();
				long first = data.get(0).getTime();
				int count = 0;
				for (Telemetry t : data) {
					if (count == fitData.size()) {
						break; // not enough data?
					}
					long time = t.getTime() - first;
					Telemetry fl = fitData.get(count);
					if (time < fl.getTime()) {
						t.setCadence(fl.getCadence());
						t.setHeartRate(fl.getHeartRate());
					} else {
						t.setCadence(fl.getCadence());
						t.setHeartRate(fl.getHeartRate());
						count++;
					}
				}
				data.get(0).setResistance(WorkoutData.FITLOG);
			}
		}

		return data;
	}
}
