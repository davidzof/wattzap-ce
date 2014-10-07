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
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/* 
 * Mean Maximal Power Graph
 * 
 * @author David George (c) Copyright 2014
 * @date 18 April 2014
 */
public class MMPGraph extends JPanel {
	ValueMarker marker = null;
	XYPlot plot;
	private ChartPanel chartPanel = null;

	private static Logger logger = LogManager.getLogger("MMPGraph");

	public MMPGraph(XYSeries series) {
		super();

		NumberAxis yAxis = new NumberAxis("Power (watts)");
		yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		double maxY = series.getMaxY();
		yAxis.setRange(0, maxY + 20);
		yAxis.setTickLabelPaint(Color.white);
		yAxis.setLabelPaint(Color.white);
		
		
		LogAxis xAxis = new LogAxis("Time");
		xAxis.setTickLabelPaint(Color.white);
		xAxis.setBase(4);
		xAxis.setAutoRange(false);
	
		
		xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		xAxis.setRange(1, series.getMaxX()+500);
		xAxis.setNumberFormatOverride(new NumberFormat() {

			@Override
			public StringBuffer format(double number, StringBuffer toAppendTo,
					FieldPosition pos) {
				
				long millis = (long) number * 1000;

				if (millis >= 60000) {
					return new StringBuffer(String.format(
							"%d m%d s",
							TimeUnit.MILLISECONDS.toMinutes((long) millis),
							TimeUnit.MILLISECONDS.toSeconds((long) millis)
									- TimeUnit.MINUTES
											.toSeconds(TimeUnit.MILLISECONDS
													.toMinutes((long) millis))));
				} else {
					return new StringBuffer(String.format(
							"%d s",
							
							TimeUnit.MILLISECONDS.toSeconds((long) millis)
									- TimeUnit.MINUTES
											.toSeconds(TimeUnit.MILLISECONDS
													.toMinutes((long) millis))));
				}
			}

			@Override
			public StringBuffer format(long number, StringBuffer toAppendTo,
					FieldPosition pos) {
				return new StringBuffer(String.format("%s", number));
			}

			@Override
			public Number parse(String source, ParsePosition parsePosition) {
				return null;
			}
		});
	


		XYPlot plot = new XYPlot(new XYSeriesCollection(series), xAxis, yAxis,
				new XYLineAndShapeRenderer(true, false));


		JFreeChart chart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT,
				plot, false);	

		chart.setBackgroundPaint(Color.gray);
		plot = chart.getXYPlot();
		plot.setBackgroundPaint(Color.darkGray);
		/*plot.setDomainGridlinePaint(Color.lightGray);
		plot.setRangeGridlinePaint(Color.lightGray);*/
		
		ValueAxis domainAxis = plot.getDomainAxis();
		domainAxis.setTickLabelPaint(Color.white);
		domainAxis.setLabelPaint(Color.white);
		
		chartPanel = new ChartPanel(chart);
		chartPanel.setSize(100, 800);
		chartPanel.setFillZoomRectangle(true);
		chartPanel.setMouseWheelEnabled(true);
		chartPanel.setBackground(Color.gray);
		
		setLayout(new BorderLayout());
		add(chartPanel, BorderLayout.CENTER);
		setBackground(Color.black);
		chartPanel.revalidate();
		setVisible(true);
	}

	private static final long serialVersionUID = 1L;
}
