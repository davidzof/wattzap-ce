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
package com.wattzap.view.graphs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GradientPaint;

import javax.swing.JPanel;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;

/* 
 Power Distribution Chart
 * 
 * @author David George (c) Copyright 2014
 * @date 21 April 2014
 */
public class DistributionGraph extends JPanel {
	private ChartPanel chartPanel = null;

	private static Logger logger = LogManager.getLogger("Profile");

	public DistributionGraph(CategoryDataset dataset, String domainLabel) {
		super();

		// create the chart...
		JFreeChart chart = ChartFactory.createBarChart("", 
				domainLabel, // domain axis label
				"Time %", // range axis label
				dataset, // data
				PlotOrientation.VERTICAL, // orientation
				false, // include legend
				true, // tooltips?
				false // URLs?
				);


		chart.setBackgroundPaint(Color.white);

		// get a reference to the plot for further customisation...
		CategoryPlot plot = (CategoryPlot) chart.getPlot();


		// set the range axis to display integers only...
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		// disable bar outlines...
		BarRenderer renderer = (BarRenderer) plot.getRenderer();
		renderer.setDrawBarOutline(false);

		// set up gradient paints for series...
		GradientPaint gp0 = new GradientPaint(0.0f, 0.0f, Color.gray, 0.0f,
				0.0f, new Color(0, 0, 64));
		renderer.setSeriesPaint(0, gp0);

		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions
				.createUpRotationLabelPositions(Math.PI / 6.0));
		// OPTIONAL CUSTOMISATION COMPLETED.

		chartPanel = new ChartPanel(chart);

		chartPanel.setSize(100, 800);

		chartPanel.setFillZoomRectangle(true);
		chartPanel.setMouseWheelEnabled(true);
		setLayout(new BorderLayout());
		add(chartPanel, BorderLayout.CENTER);
		setBackground(Color.black);
		chartPanel.revalidate();
		setVisible(true);
	}

	private static final long serialVersionUID = 1L;
}
