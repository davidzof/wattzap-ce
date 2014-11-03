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
import java.util.ArrayList;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.Telemetry;
import com.wattzap.model.dto.WorkoutData;
import com.wattzap.view.training.TrainingAnalysis;

public class GpxImporterTest {
	@Test
	public void ImportGPX() {
		try {
			LogManager.getRootLogger().setLevel(Level.INFO);
			XMLReader xr = XMLReaderFactory.createXMLReader();
			GpxImporter handler = new GpxImporter();
			xr.setContentHandler(handler);
			xr.setErrorHandler(handler);

			String file = "resources/test/colducoq-6jun2014.gpx";
			FileReader r = new FileReader(file);
			xr.parse(new InputSource(r));

			ArrayList<Telemetry> gpxData = handler.data;
			Telemetry last = gpxData.get(gpxData.size() - 1);
			Assert.assertEquals(last.getDistanceKM(), 32.557, 0.001);
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	@Test
	public void ImportAndWriteTCX() {
		try {
			LogManager.getRootLogger().setLevel(Level.INFO);
			XMLReader xr = XMLReaderFactory.createXMLReader();
			GpxImporter handler = new GpxImporter();
			xr.setContentHandler(handler);
			xr.setErrorHandler(handler);

			String file = "resources/test/colducoq-6jun2014.gpx";
			FileReader r = new FileReader(file);
			xr.parse(new InputSource(r));

			ArrayList<Telemetry> gpxData = handler.data;

			TcxWriter writer = new TcxWriter();
			writer.save(gpxData, 0);

			XMLReader xrTcx = XMLReaderFactory.createXMLReader();
			TcxImporter tcxHandler = new TcxImporter();
			xr.setContentHandler(tcxHandler);
			xr.setErrorHandler(tcxHandler);

			Telemetry firstPoint = gpxData.get(0);

			String fileName = UserPreferences.INSTANCE.getUserDataDirectory()
					+ "/Workouts/"
					+ TcxWriter.getWorkoutName(firstPoint.getTime());

			r = new FileReader(fileName);
			xr.parse(new InputSource(r));

			ArrayList<Telemetry> tcxData = tcxHandler.data;
			Assert.assertEquals(tcxData.size(), gpxData.size());
			// go through point by point
			for (int i = 0; i < tcxData.size(); i++) {
				Telemetry gpxPoint = gpxData.get(i);
				Telemetry tcxPoint = tcxData.get(i);
				Assert.assertEquals(tcxPoint.getSpeedKMH(), gpxPoint.getSpeedKMH(), 0.1);
			}

		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	
	//@Test
	public void ImportGPXMergeFitLog() {
		try {
			LogManager.getRootLogger().setLevel(Level.INFO);
			XMLReader xr = XMLReaderFactory.createXMLReader();
			GpxImporter handler = new GpxImporter();
			xr.setContentHandler(handler);
			xr.setErrorHandler(handler);

			String file = "resources/test/colducoq-6jun2014.gpx";
			FileReader r = new FileReader(file);
			xr.parse(new InputSource(r));

			ArrayList<Telemetry> gpxData = handler.data;

			FitlogImporter flHandler = new FitlogImporter();
			xr.setContentHandler(flHandler);
			xr.setErrorHandler(flHandler);
			file = "resources/test/colducoq-6jun2014.fitlog";
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
			d.setFtp(250);
			System.out.println(d);

		} catch (Exception e) {
			e.printStackTrace();

		}
	}
}
