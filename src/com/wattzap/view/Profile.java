package com.wattzap.view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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

import com.wattzap.model.GPXData;
import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.Telemetry;

public class Profile extends JPanel implements ChangeListener, ActionListener {
	ValueMarker marker = null;
	XYPlot plot;
	private ChartPanel chartPanel = null;
	
	private static Logger logger = LogManager
			.getLogger("Profile");

	public Profile(Dimension d, MainFrame frame) {
		super();

		// this.setPreferredSize(d);
		
		// code to see if we are registered
		if (!UserPreferences.INSTANCE.isRegistered() && (UserPreferences.INSTANCE.getEvalTime()) <= 0) {
			logger.info("Out of time " + UserPreferences.INSTANCE.getEvalTime());
			UserPreferences.INSTANCE.shutDown();
			System.exit(0);
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (plot == null) {
			// nothing to see here
			// TODO get plot to unregister itself
			return;
		}

		Telemetry t = (Telemetry) e.getSource();

		if (marker != null) {
			plot.removeDomainMarker(marker);
		}
		marker = new ValueMarker(t.getDistance());

		marker.setPaint(Color.blue);
		BasicStroke stroke = new BasicStroke(2);
		marker.setStroke(stroke);
		plot.addDomainMarker(marker);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		logger.debug(command);
		if ("gpxload".equals(command)) {
			if (chartPanel != null) {
				remove(chartPanel);
			}
			GPXData gpxData = (GPXData) e.getSource();
			logger.debug("Load " + gpxData.getFilename());
			XYDataset xyDataset = new XYSeriesCollection(gpxData.getSeries());

			// create the chart...
			final JFreeChart chart = ChartFactory.createXYAreaChart(
					gpxData.getFilename(), // chart
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
			plot.setForegroundAlpha(0.85f);

			plot.setBackgroundPaint(Color.white);
			plot.setDomainGridlinePaint(Color.lightGray);
			plot.setRangeGridlinePaint(Color.lightGray);

			ValueAxis rangeAxis = plot.getRangeAxis();
			rangeAxis.setTickLabelPaint(Color.white);
			rangeAxis.setLabelPaint(Color.white);
			ValueAxis domainAxis = plot.getDomainAxis();
			domainAxis.setTickLabelPaint(Color.white);
			domainAxis.setLabelPaint(Color.white);

			double minY = gpxData.getSeries().getMinY();
			double maxY = gpxData.getSeries().getMaxY();
			rangeAxis.setRange(minY - 100.0, maxY + 100.0);

			chartPanel = new ChartPanel(chart);

			chartPanel.setSize(100, 800);

			setLayout(new BorderLayout());
			add(chartPanel, BorderLayout.CENTER);
			setBackground(Color.black);
			chartPanel.revalidate();
			setVisible(true);

		} else if ("Close".equals(command)) {
			if (this.isVisible()) {
				remove(chartPanel);
				setVisible(false);
				revalidate();
			}
		}
	}

}
