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

public class TcxImporterTest {
	@Test
	public void TcxImport() {
		try {
			XMLReader xr = XMLReaderFactory.createXMLReader();
			TcxImporter handler = new TcxImporter();
			xr.setContentHandler(handler);
			xr.setErrorHandler(handler);

			String file = "2014Apr28-223459.tcx";
			FileReader r = new FileReader(file);
			xr.parse(new InputSource(r));

			ArrayList<Telemetry> data = handler.data;
			Telemetry last = data.get(data.size() - 1);
			if (last.getDistance() == 0) {
				last.setDistance(handler.distance);// hack if no distance data
			}
			for (Telemetry t : data) {
				System.out.println(t.getPower());
			}
			WorkoutData d = TrainingAnalysis.analyze(data);
			d.setFtp(220); // TODO !!!!
			d.setTcxFile(file);
			System.out.println(d);
		} catch (Exception e) {

		}
	}
}
