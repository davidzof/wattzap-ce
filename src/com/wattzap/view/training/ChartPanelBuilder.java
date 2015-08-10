package com.wattzap.view.training;

import com.wattzap.model.dto.TrainingRangeView;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.time.TimePeriodValues;
import org.jfree.data.time.TimePeriodValuesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;

import java.awt.*;
import java.util.Date;
import java.util.Map;

public final class ChartPanelBuilder {


    public static ChartPanel createChartPanelWithSeries(Map<Long, TrainingRangeView> limits, String titleSerieHigh, String titleSerieLow, String title, double minY, double maxY, int physioLimit) {
        TimePeriodValues yintervalseriesHigh = new TimePeriodValues(titleSerieHigh);
        TimePeriodValues yintervalseriesLow = new TimePeriodValues(titleSerieLow);
        TimePeriodValues yintervalPhysiologicalLimit = new TimePeriodValues("phy. limit");
        TimePeriodValues yDatas = new TimePeriodValues(title);
        Second start = new Second(new Date());
        Second objPrev = start;
        Second objNext = start;
        for (Long timeInSeconds : limits.keySet()) {
            long low = limits.get(timeInSeconds).getLow();
            long high = limits.get(timeInSeconds).getHigh();
            Date nextIntervalStart = new Date(start.getStart().getTime() + (timeInSeconds * 1000));
            objNext = new Second(nextIntervalStart);
            yintervalseriesHigh.add(new SimpleTimePeriod(objPrev.getStart(), objNext.getEnd()), high);
            yintervalseriesLow.add(new SimpleTimePeriod(objPrev.getStart(), objNext.getEnd()), low);
            objPrev = objNext;
        }
        TimePeriodValuesCollection series = new TimePeriodValuesCollection();
        series.addSeries(yintervalseriesLow);
        series.addSeries(yintervalseriesHigh);

        // physiological limit : same from start to end
        TimePeriodValuesCollection series2 = new TimePeriodValuesCollection();
        yintervalPhysiologicalLimit.add(new SimpleTimePeriod(start.getStart(), start.getEnd()), physioLimit);
        yintervalPhysiologicalLimit.add(new SimpleTimePeriod(objNext.getStart(), objNext.getEnd()), physioLimit);
        series2.addSeries(yDatas);
        series2.addSeries(yintervalPhysiologicalLimit);
        return createChartPanel(series, series2, title, minY, maxY);
    }

    public static ChartPanel createChartPanel(XYDataset xydataset, XYDataset xydataset2, String title, double minY, double maxY) {

        final DateAxis domainAxis = new DateAxis("Time");
        final ValueAxis rangeAxis = new NumberAxis("Value");
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setUpperBound(maxY);
        rangeAxis.setLowerBound(minY);

        // serie 0-0 (bar)  : realtime telemetry value
        // serie 0-1 (bar)  : physiological limit (ftp, fhr, ...)
        // serie 1-0 (line) : interval low limit
        // serie 1-1 (line) : interval high limit

        XYBarRenderer barRenderer = new XYBarRenderer();

//        Color primaryColor1 = Color.cyan.darker().darker();
//        Color secondaryColor1 = Color.cyan;
//        Color primaryColor2 = Color.blue.darker();
//        Color secondaryColor2 = Color.blue.brighter();
//        GradientPaint gpHorizontal = new GradientPaint(5, 5, primaryColor1, 5,
//                10, secondaryColor1, true);
//        GradientPaint gpHorizontal2 = new GradientPaint(5, 5, primaryColor2, 5,
//                10, secondaryColor2, true);

        barRenderer.setSeriesStroke(0, new BasicStroke(1F, 1, 1));
//        Color colorMin = new Color(0, 102, 102);
        Color colorMin = Color.darkGray;
        Color colorMax = new Color(0, 204, 204);
        barRenderer.setSeriesPaint(0, colorMin);
        barRenderer.setSeriesStroke(1, new BasicStroke(1F, 1, 1));
        barRenderer.setSeriesPaint(1, colorMax);

        barRenderer.setGradientPaintTransformer(null);
        barRenderer.setBarPainter(new StandardXYBarPainter());


        barRenderer.setShadowVisible(false);
        barRenderer.setDrawBarOutline(false);

        XYLineAndShapeRenderer lineRenderer = new XYLineAndShapeRenderer();
        lineRenderer.setSeriesStroke(0, new BasicStroke(2F, 1, 1));
        lineRenderer.setSeriesPaint(0, Color.yellow);
        lineRenderer.setSeriesStroke(1, new BasicStroke(1F, 1, 1));
        lineRenderer.setSeriesPaint(1, Color.gray);
        lineRenderer.setSeriesLinesVisible(0, true);
        lineRenderer.setSeriesShapesVisible(0, false);
        lineRenderer.setSeriesLinesVisible(1, true);
        lineRenderer.setSeriesShapesVisible(1, false);

        XYPlot xyplot = new XYPlot(xydataset2, domainAxis, rangeAxis, lineRenderer);
        xyplot.setDataset(1, xydataset);
        xyplot.setRenderer(1, barRenderer);

        JFreeChart chart = new JFreeChart(title, xyplot);
        chart.setBackgroundPaint(Color.black);
        xyplot.setInsets(new RectangleInsets(5D, 5D, 5D, 20D));
        xyplot.setBackgroundPaint(Color.darkGray);
        xyplot.setAxisOffset(new RectangleInsets(5D, 5D, 5D, 5D));
        xyplot.setDomainGridlinesVisible(false);
        xyplot.setRangeGridlinesVisible(false);

        ChartPanel chartPanel = new ChartPanel(chart) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(250, 300);
            }
        };
        return chartPanel;
    }
}