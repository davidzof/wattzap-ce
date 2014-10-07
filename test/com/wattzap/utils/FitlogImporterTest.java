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

import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.wattzap.model.dto.Telemetry;
import com.wattzap.model.dto.WorkoutData;
import com.wattzap.view.training.TrainingAnalysis;

/**
 * Importer for FITLOG format (.fitlog format which is an open XML standard
 * devised by the creators of Sportstracks)
 * 
 * @author david (c) 1st May 2014 David George/Wattzap.com
 */
public class FitlogImporterTest {
	@Test
	public void FitLogImport() {
		try {
			XMLReader xr = XMLReaderFactory.createXMLReader();
			FitlogImporter handler = new FitlogImporter();
			xr.setContentHandler(handler);
			xr.setErrorHandler(handler);

			String file = "Mouilles.fitlog";
			FileReader r = new FileReader(file);
			xr.parse(new InputSource(r));

			ArrayList<Telemetry> data = handler.data;
			WorkoutData d = TrainingAnalysis.analyze(data);
			d.setFtp(260);
			System.out.println(d);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
