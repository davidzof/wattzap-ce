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

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import com.wattzap.model.dto.Telemetry;

/**
 * Decathlon et al Fitlog importer
 * 
 * @author David George
 * @date 2nd May 2014
 */
public class FitlogImporter extends DefaultHandler {
	ArrayList<Telemetry> data;
	Telemetry point;
	Telemetry last = null;
	long adjust = 0;

	public FitlogImporter() {
		super();
		data = new ArrayList<Telemetry>();
	}

	public void startElement(String uri, String name, String qName,
			Attributes atts) {

		if ("pt".equalsIgnoreCase(name)) {
			point = new Telemetry();

			String hr = atts.getValue("hr");
			if (hr != null) {
				point.setHeartRate(Integer.parseInt(hr));
			}
			String time = atts.getValue("tm");
			if (time != null) {
				point.setTime(Integer.parseInt(time) * 1000);
			}
			String cadence = atts.getValue("cadence");
			if (cadence != null) {
				point.setCadence(Integer.parseInt(cadence));
			}
			String distance = atts.getValue("dist");
			if (distance != null) {
				point.setDistanceMeters(Double.parseDouble(distance));
			}
		}
	}

	public void endElement(String uri, String name, String qName) {
		if ("pt".equalsIgnoreCase(name)) {
			if (last != null) {
				double distance = point.getDistanceMeters() - last.getDistanceMeters();
				if (distance == 0) {
					// no distance change, we've stopped, drop point
					adjust += ((point.getTime() - adjust) - last.getTime());
					return;// drop value
				}
			}
			point.setTime(point.getTime() - adjust);
			data.add(point);
			last = point;
		}
	}
}
