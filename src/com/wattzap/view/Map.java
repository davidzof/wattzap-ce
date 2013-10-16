package com.wattzap.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.gpxcreator.gpxpanel.GPXFile;
import com.gpxcreator.gpxpanel.GPXPanel;
import com.wattzap.model.GPXData;
import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.Telemetry;

public class Map extends GPXPanel implements ChangeListener, ActionListener {
	private static final long serialVersionUID = 1L;
	private MainFrame frame;
	private static long count = 0;

	private static Logger logger = LogManager.getLogger(Map.class.getName());

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

		// code to see if we are registered
		if (!UserPreferences.INSTANCE.isRegistered()
				&& (UserPreferences.INSTANCE.getEvalTime()) <= 0) {
			logger.info("Out of time " + UserPreferences.INSTANCE.getEvalTime());
			UserPreferences.INSTANCE.shutDown();
			System.exit(0);
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		Telemetry t = (Telemetry) e.getSource();
		int len = 50;

		if (count++ % len == 0) {
			if (zoom == 13) {
				zoom = 15;
				len = 50;
			} else {
				zoom = 13;
				len = 20;
			}
		}

		setCrosshairLat(t.getLatitude());
		setCrosshairLon(t.getLongitude());
		// int zoom = this.getZoom();
		setDisplayPositionByLatLon(t.getLatitude(), t.getLongitude(), zoom);
		setShowCrosshair(true);
		repaint();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		logger.debug("Action " + command);
		if ("gpxload".equals(command)) {
			count = 0;
			frame.remove(this);
			GPXData gpxData = (GPXData) e.getSource();
			GPXFile gpxFile = gpxData.getGpxFile();

			double centerLon = gpxFile.getMinLon()
					+ (gpxFile.getMaxLon() - gpxFile.getMinLon()) / 2;
			double centerLat = gpxFile.getMinLat()
					+ (gpxFile.getMaxLat() - gpxFile.getMinLat()) / 2;
			setDisplayPositionByLatLon(centerLat, centerLon, 12);

			addGPXFile(gpxFile);
			// setSize(400, 400);

			frame.add(this, "cell 0 0");
			setVisible(true);
		} else {
			if (this.isVisible()) {
				frame.remove(this);
				setVisible(false);
				frame.revalidate();
			}
		}
	}
}
