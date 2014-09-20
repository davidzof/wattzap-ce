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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Mean Maximal Power Graph
 * 
 * @author David George (c) Copyright 2014
 * @date 18 April 2014
 */
public class SCHRGraph extends JPanel {
	ValueMarker marker = null;
	XYPlot plot;
	private ChartPanel chartPanel = null;
	// a few colors
	private final static Color straw = new Color(255, 255, 191);// straw
	private final static Color cornflower = new Color(145, 191, 219);// cornflower
	private final static Color orange = new Color(252, 141, 89);// orange

	private static Logger logger = LogManager.getLogger("Profile");

	public SCHRGraph(XYSeries powerSeries, XYSeries cadenceSeries,
			XYSeries heartRateSeries) {
		super();

		final NumberAxis domainAxis = new NumberAxis("Time (h:m:s)");

		domainAxis.setVerticalTickLabels(true);
		domainAxis.setTickLabelPaint(Color.black);
		domainAxis.setAutoRange(true);

		domainAxis.setNumberFormatOverride(new NumberFormat() {
			@Override
			public StringBuffer format(double millis, StringBuffer toAppendTo,
					FieldPosition pos) {
				if (millis >= 3600000) {
					// hours, minutes and seconds
					return new StringBuffer(

					String.format(
							"%d:%d:%d",
							TimeUnit.MILLISECONDS.toHours((long) millis),
							TimeUnit.MILLISECONDS.toMinutes((long) millis
									- TimeUnit.MILLISECONDS
											.toHours((long) millis) * 3600000),
							TimeUnit.MILLISECONDS.toSeconds((long) millis)
									- TimeUnit.MINUTES
											.toSeconds(TimeUnit.MILLISECONDS
													.toMinutes((long) millis))));
				} else if (millis >= 60000) {
					// minutes and seconds
					return new StringBuffer(String.format(
							"%d:%d",
							TimeUnit.MILLISECONDS.toMinutes((long) millis),
							TimeUnit.MILLISECONDS.toSeconds((long) millis)
									- TimeUnit.MINUTES
											.toSeconds(TimeUnit.MILLISECONDS
													.toMinutes((long) millis))));
				} else {
					return new StringBuffer(String.format(
							"%d",
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

		final ValueAxis powerAxis = new NumberAxis("Power (watts)");
		powerAxis.setRange(0, powerSeries.getMaxY());

		// create plot ...
		final IntervalXYDataset powerData = new XYSeriesCollection(powerSeries);
		final XYItemRenderer powerRenderer = new StandardXYItemRenderer() {
			Stroke regularStroke = new BasicStroke(0.7f);
			Color color;

			public void setColor(Color color) {
				this.color = color;
			}

			@Override
			public Stroke getItemStroke(int row, int column) {
				return regularStroke;
			}

			@Override
			public Paint getItemPaint(int row, int column) {
				return orange;
			}
		};
		powerRenderer.setSeriesPaint(0, orange);
		final XYPlot plot = new XYPlot(powerData, domainAxis, powerAxis,
				powerRenderer);

		// add a second dataset and renderer...
		final IntervalXYDataset cadenceData = new XYSeriesCollection(
				cadenceSeries);
		final XYItemRenderer cadenceRenderer = new StandardXYItemRenderer() {
			Stroke regularStroke = new BasicStroke(0.7f);
			Color color;

			public void setColor(Color color) {
				this.color = color;
			}

			@Override
			public Stroke getItemStroke(int row, int column) {
				return regularStroke;
			}

			@Override
			public Paint getItemPaint(int row, int column) {
				return cornflower;
			}
			
			public Shape lookupLegendShape(int series) {
				return new Rectangle(15, 15);
			}
		};

		final ValueAxis cadenceAxis = new NumberAxis("Cadence (rpm)");
		cadenceAxis.setRange(0, 200);
		

		// arguments of new XYLineAndShapeRenderer are to activate or deactivate
		// the display of points or line. Set first argument to true if you want
		// to draw lines between the points for e.g.
		plot.setDataset(1, cadenceData);
		plot.setRenderer(1, cadenceRenderer);
		plot.setRangeAxis(1, cadenceAxis);
		plot.mapDatasetToRangeAxis(1, 1);
		cadenceRenderer.setSeriesPaint(0, cornflower);

		// add a third dataset and renderer...
		final IntervalXYDataset hrData = new XYSeriesCollection(heartRateSeries);
		final XYItemRenderer hrRenderer = new StandardXYItemRenderer() {
			Stroke regularStroke = new BasicStroke(0.7f);
			Color color;

			public void setColor(Color color) {
				this.color = color;
			}

			@Override
			public Stroke getItemStroke(int row, int column) {
				return regularStroke;
			}

			@Override
			public Paint getItemPaint(int row, int column) {
				return straw;
			}

	
		};

		// arguments of new XYLineAndShapeRenderer are to activate or deactivate
		// the display of points or line. Set first argument to true if you want
		// to draw lines between the points for e.g.
		final ValueAxis heartRateAxis = new NumberAxis("Heart-Rate (bpm)");
		heartRateAxis.setRange(0, 200);
		plot.setDataset(2, hrData);
		plot.setRenderer(2, hrRenderer);
		hrRenderer.setSeriesPaint(0, straw);

		plot.setRangeAxis(2, heartRateAxis);
		plot.mapDatasetToRangeAxis(2, 2);
		plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		plot.setBackgroundPaint(Color.DARK_GRAY);
		// return a new chart containing the overlaid plot...
		JFreeChart chart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT,
				plot, true);
		
		chart.getLegend().setBackgroundPaint(Color.gray);

		chartPanel = new ChartPanel(chart);
		chartPanel.setSize(100, 1200);
		chartPanel.setFillZoomRectangle(true);
		chartPanel.setMouseWheelEnabled(true);
		chartPanel.setBackground(Color.gray);

		setLayout(new BorderLayout());
		add(chartPanel, BorderLayout.CENTER);
		// setBackground(Color.black);
		chartPanel.revalidate();
		setVisible(true);
	}

	private static final long serialVersionUID = 1L;
}
