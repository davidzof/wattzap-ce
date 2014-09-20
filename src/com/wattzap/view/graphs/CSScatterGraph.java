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
import java.awt.Shape;

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
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.ShapeUtilities;

/* 
 * Mean Maximal Power Graph
 * 
 * @author David George (c) Copyright 2014
 * @date 18 April 2014
 */
public class CSScatterGraph extends JPanel {
	ValueMarker marker = null;
	XYPlot plot;
	private ChartPanel chartPanel = null;

	private static Logger logger = LogManager.getLogger("CSSScatterGraph");

	public CSScatterGraph(XYSeries series) {
		super();

		XYDataset xyDataset = new XYSeriesCollection(series);

		JFreeChart chart = ChartFactory.createScatterPlot("", // chart title
				"Power", // x axis label
				"Cadence", // y axis label
				xyDataset, PlotOrientation.VERTICAL, false, // include legend
				true, // tooltips
				false // urls
				);

		chart.setBackgroundPaint(Color.darkGray);

		plot = chart.getXYPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinePaint(Color.lightGray);
		plot.setRangeGridlinePaint(Color.lightGray);

		//Shape cross = ShapeUtilities.createDiamond(0.5f);
		Shape cross = ShapeUtilities.createDiagonalCross(0.5f,0.5f);
		XYItemRenderer renderer = plot.getRenderer();
		renderer.setPaint(new Color(252, 141, 89));
		renderer.setSeriesShape(0, cross);
		
		ValueAxis domainAxis = plot.getDomainAxis();
		domainAxis.setTickLabelPaint(Color.white);
		domainAxis.setLabelPaint(Color.white);
		
		ValueAxis rangeAxis =  plot.getRangeAxis();
		rangeAxis.setTickLabelPaint(Color.white);
		rangeAxis.setLabelPaint(Color.white);

		chartPanel = new ChartPanel(chart);
		chartPanel.setSize(100, 800);

		setLayout(new BorderLayout());
		add(chartPanel, BorderLayout.CENTER);
		setBackground(Color.black);
		chartPanel.revalidate();
		setVisible(true);
	}

	private static final long serialVersionUID = 1L;
}