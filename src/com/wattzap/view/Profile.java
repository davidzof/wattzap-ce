package com.wattzap.view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;

import com.wattzap.controller.MessageBus;
import com.wattzap.controller.MessageCallback;
import com.wattzap.controller.Messages;
import com.wattzap.model.RLVReader;
import com.wattzap.model.RouteReader;
import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.Telemetry;

/* 
 * Shows a profile of the route and moves an indicator to show rider progress on profile
 * 
 * @author David George (c) Copyright 2013
 * @date 19 June 2013
 */
public class Profile extends JPanel implements MessageCallback {
	ValueMarker marker = null;
	XYPlot plot;
	private ChartPanel chartPanel = null;

	private static Logger logger = LogManager.getLogger("Profile");

	public Profile(Dimension d) {
		super();

		// this.setPreferredSize(d);

		MessageBus.INSTANCE.register(Messages.SPEEDCADENCE, this);
		MessageBus.INSTANCE.register(Messages.STARTPOS, this);
		MessageBus.INSTANCE.register(Messages.CLOSE, this);
		MessageBus.INSTANCE.register(Messages.GPXLOAD, this);
	}

	@Override
	public void callback(Messages message, Object o) {
		double distance = 0.0;
		switch (message) {
		case SPEEDCADENCE:
			Telemetry t = (Telemetry) o;
			distance = t.getDistance();
			break;
		case STARTPOS:
			distance = (Double) o;
			break;
		case CLOSE:
			if (this.isVisible()) {
				remove(chartPanel);
				setVisible(false);
				revalidate();
			}
			
			return;
		case GPXLOAD:
			// code to see if we are registered
			if (!UserPreferences.INSTANCE.isRegistered()
					&& (UserPreferences.INSTANCE.getEvalTime()) <= 0) {
				logger.info("Out of time " + UserPreferences.INSTANCE.getEvalTime());
				JOptionPane.showMessageDialog(this, UserPreferences.INSTANCE.messages.getString("trial_expired"),
						UserPreferences.INSTANCE.messages.getString("warning"),
						JOptionPane.WARNING_MESSAGE);
				UserPreferences.INSTANCE.shutDown();
				System.exit(0);
			}
			
			RouteReader routeData = (RouteReader) o;

			if (chartPanel != null) {
				remove(chartPanel);
				if (routeData.routeType() == RLVReader.POWER) {
					setVisible(false);
					chartPanel.revalidate();
					return;
				}
			} else if (routeData.routeType() == RLVReader.POWER) {
				return;
			}

			logger.debug("Load " + routeData.getFilename());
			XYDataset xyDataset = new XYSeriesCollection(routeData.getSeries());

			// create the chart...
			final JFreeChart chart = ChartFactory.createXYAreaChart(
					routeData.getName(), // chart
					// title
					"Distance (km)", // domain axis label
					"Height (meters)", // range axis label
					xyDataset, // data
					PlotOrientation.VERTICAL, // orientation
					false, // include legend
					false, // tooltips
					false // urls
					);

			chart.setBackgroundPaint(Color.darkGray);

			plot = chart.getXYPlot();
			// plot.setForegroundAlpha(0.85f);

			plot.setBackgroundPaint(Color.white);
			plot.setDomainGridlinePaint(Color.lightGray);
			plot.setRangeGridlinePaint(Color.lightGray);

			ValueAxis rangeAxis = plot.getRangeAxis();
			rangeAxis.setTickLabelPaint(Color.white);
			rangeAxis.setLabelPaint(Color.white);
			ValueAxis domainAxis = plot.getDomainAxis();
			domainAxis.setTickLabelPaint(Color.white);
			domainAxis.setLabelPaint(Color.white);

			double minY = routeData.getSeries().getMinY();
			double maxY = routeData.getSeries().getMaxY();
			rangeAxis.setRange(minY - 100.0, maxY + 100.0);

			chartPanel = new ChartPanel(chart);

			chartPanel.setSize(100, 800);

			setLayout(new BorderLayout());
			add(chartPanel, BorderLayout.CENTER);
			setBackground(Color.black);
			chartPanel.revalidate();
			setVisible(true);
			break;
		}// switch
		if (plot == null) {
			return;
		}

		if (marker != null) {
			plot.removeDomainMarker(marker);
		}
		marker = new ValueMarker(distance);

		marker.setPaint(Color.blue);
		BasicStroke stroke = new BasicStroke(2);
		marker.setStroke(stroke);
		plot.addDomainMarker(marker);

	}

	private static final long serialVersionUID = 1L;
}
