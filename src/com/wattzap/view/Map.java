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
package com.wattzap.view;

import javax.swing.JOptionPane;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.gpxcreator.gpxpanel.GPXFile;
import com.gpxcreator.gpxpanel.GPXPanel;
import com.wattzap.controller.MessageBus;
import com.wattzap.controller.MessageCallback;
import com.wattzap.controller.Messages;
import com.wattzap.model.RouteReader;
import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.Telemetry;

/* 
 * Displays a map of the course and moves cross-hairs depending on position.
 * 
 * @author David George (c) Copyright 2013
 * @date 19 June 2013
 */
public class Map extends GPXPanel implements MessageCallback {
	private static final long serialVersionUID = 1L;
	private MainFrame frame;
	private static long count = 0;
	private int displayPeriod = 50;
	GPXFile gpxFile;

	private static Logger logger = LogManager.getLogger("Map");

	public Map(MainFrame frame) {
		super();

		// Alternative Source
		// check to see if tiles exist and use offline
		// see:
		// http://paulusschoutsen.nl/blog/2012/08/java-component-jmapviewer-with-offline-openstreetmap-support/
		// http://switch2osm.org/serving-tiles/
		// http://wiki.openstreetmap.org/wiki/JTileDownloader#Screenshots
		// this.setTileSource(tileSource)

		this.frame = frame;
		setVisible(false);

		MessageBus.INSTANCE.register(Messages.SPEEDCADENCE, this);
		MessageBus.INSTANCE.register(Messages.CLOSE, this);
		MessageBus.INSTANCE.register(Messages.GPXLOAD, this);
	}

	@Override
	public void callback(Messages message, Object o) {

		switch (message) {
		case SPEEDCADENCE:

			Telemetry t = (Telemetry) o;

			if (count++ % displayPeriod == 0) {
				if (zoom == 13) {
					zoom = 15;
					displayPeriod = 50;
				} else {
					zoom = 13;
					displayPeriod = 20;
				}
			}

			setCrosshairLat(t.getLatitude());
			setCrosshairLon(t.getLongitude());
			// int zoom = this.getZoom();
			setDisplayPositionByLatLon(t.getLatitude(), t.getLongitude(), zoom);
			setShowCrosshair(true);
			repaint();
			break;
		case CLOSE:
			if (this.isVisible()) {
				frame.remove(this);
				if (gpxFile != null) {
					this.removeGPXFile(gpxFile);
				}
				setVisible(false);
				//frame.invalidate();
				frame.validate();
				// frame.revalidate(); JDK 1.7 ONLY
			}
			break;
		case GPXLOAD:
			// code to see if we are registered
			if (!UserPreferences.INSTANCE.isRegistered()
					&& (UserPreferences.INSTANCE.getEvalTime()) <= 0) {
				logger.info("Out of time "
						+ UserPreferences.INSTANCE.getEvalTime());
				JOptionPane.showMessageDialog(this,
						UserPreferences.INSTANCE.messages
								.getString("trial_expired"),
						UserPreferences.INSTANCE.messages.getString("warning"),
						JOptionPane.WARNING_MESSAGE);
				UserPreferences.INSTANCE.shutDown();
				System.exit(0);
			}

			count = 0;
			frame.remove(this);
			RouteReader routeData = (RouteReader) o;

			if (gpxFile != null) {
				// remove any previously loaded GPX file
				this.removeGPXFile(gpxFile);
			}
			gpxFile = routeData.getGpxFile();

			// TODO - change load message: gpxload, rlvload?
			if (gpxFile != null) {
				double centerLon = gpxFile.getMinLon()
						+ (gpxFile.getMaxLon() - gpxFile.getMinLon()) / 2;
				double centerLat = gpxFile.getMinLat()
						+ (gpxFile.getMaxLat() - gpxFile.getMinLat()) / 2;
				setDisplayPositionByLatLon(centerLat, centerLon, 12);

				this.addGPXFile(gpxFile);
				// setSize(400, 400);

				frame.add(this, "cell 0 0");
				setVisible(true);
			}
			break;
		}
	}
}
