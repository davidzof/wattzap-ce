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

import java.io.FileReader;
import java.util.ArrayList;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.wattzap.model.dto.Telemetry;
import com.wattzap.model.dto.WorkoutData;
import com.wattzap.view.training.TrainingAnalysis;

public class GpxImporterTest {
	@Test
	public void FitLogImport() {
		try {
			LogManager.getRootLogger().setLevel(Level.INFO);
			XMLReader xr = XMLReaderFactory.createXMLReader();
			GpxImporter handler = new GpxImporter();
			xr.setContentHandler(handler);
			xr.setErrorHandler(handler);

			String file = "Croix-de-Fer-10may2014.gpx";
			FileReader r = new FileReader(file);
			xr.parse(new InputSource(r));

			ArrayList<Telemetry> gpxData = handler.data;

			FitlogImporter flHandler = new FitlogImporter();
			xr.setContentHandler(flHandler);
			xr.setErrorHandler(flHandler);
			// System.exit(1);
			file = "Croix-de-Fer-10may2014.fitlog";
			r = new FileReader(file);
			xr.parse(new InputSource(r));

			ArrayList<Telemetry> fitData = flHandler.data;
			long first = gpxData.get(0).getTime();
			int count = 0;
			for (Telemetry t : gpxData) {
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

			WorkoutData d = TrainingAnalysis.analyze(gpxData);
			d.setFtp(260);
			System.out.println(d);
			TcxWriter writer = new TcxWriter();
			writer.save(gpxData, 0);
		} catch (Exception e) {

		}
	}
}
