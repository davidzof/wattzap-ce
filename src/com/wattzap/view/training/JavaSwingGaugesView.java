package com.wattzap.view.training;

import com.wattzap.controller.MessageBus;
import com.wattzap.controller.MessageCallback;
import com.wattzap.controller.Messages;
import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.Telemetry;
import com.wattzap.model.dto.TrainingData;
import com.wattzap.model.dto.TrainingItem;
import com.wattzap.model.dto.TrainingRangeView;
import com.wattzap.utils.DataInjector;
import eu.hansolo.steelseries.gauges.DisplaySingle;
import eu.hansolo.steelseries.gauges.Radial;
import eu.hansolo.steelseries.tools.*;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.time.*;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Created by nicolas on 08/05/2015.
 */
public class JavaSwingGaugesView implements MessageCallback {


    public static final int GAUGE_PREFERRED_SIZE = 800;
    private static final int GAUGE_MINIMUM_SIZE = 250;


    private static Radial powerGauge;
    private static Radial cadenceGauge;
    private static Radial heartRateGauge;

    private static DisplaySingle clockTotal;
    private static DisplaySingle clockRemainingInInterval;

    private TrainingData trainingData;
    private TrainingItem current;

    private static JButton stopButton;
    private static JButton startButton;

    private long intervalStartedTime; // keep the beginning of the current interval

    private long trainingStartedTime; // keep the beginning of the whole training session
    private static TimePeriodValuesCollection heartRateSeries;
    private static TimePeriodValuesCollection powerSeries;
    private static TimePeriodValuesCollection cadenceSeries;
    private static ChartPanel cadenceChartPanel;
    private static ChartPanel powerChartPanel;
    private static ChartPanel heartRateChartPanel;
    private static JPanel panelSouth;
    private static JFrame frame;

    public JavaSwingGaugesView() {
        // register message bus events
        MessageBus.INSTANCE.register(Messages.SPEED, this);
        MessageBus.INSTANCE.register(Messages.CADENCE, this);
        MessageBus.INSTANCE.register(Messages.HEARTRATE, this);
        MessageBus.INSTANCE.register(Messages.START, this);
        MessageBus.INSTANCE.register(Messages.STARTPOS, this);
        MessageBus.INSTANCE.register(Messages.STOP, this);
        MessageBus.INSTANCE.register(Messages.TRAINING, this);
        MessageBus.INSTANCE.register(Messages.TRAININGITEM, this);
        MessageBus.INSTANCE.register(Messages.CLOSE, this);
    }

    public void show() {
        SwingUtilities.invokeLater(() -> JavaSwingGaugesView.initAndShowGUI());
    }

    private static void initAndShowGUI() {

        cadenceGauge = new CadenceGaugeBuilder().build();
        heartRateGauge = new HeartRateGaugeBuilder().build();
        powerGauge = new PowerGaugeBuilder().build();
        buildControlPanel();

        frame = new JFrame("Gauges");
        frame.setBackground(Color.black);
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.black);
        frame.add(mainPanel);
        mainPanel.setLayout(new BorderLayout());

        // build north panel
        JPanel panelNorth = new JPanel();
        panelNorth.setBackground(Color.black);
        panelNorth.setLayout(new MigLayout());
        panelNorth.add(new JLabel("Wattzap!"));
        panelNorth.add(startButton);
        panelNorth.add(stopButton);
        clockTotal = buildClockTotal();
        panelNorth.add(clockTotal);
        clockRemainingInInterval = new DisplaySingle();
        clockRemainingInInterval.setPreferredSize(new Dimension(GAUGE_MINIMUM_SIZE, 75));
        clockRemainingInInterval.setMinimumSize(new Dimension(GAUGE_MINIMUM_SIZE, 75));
        clockRemainingInInterval.setLcdUnitString("seconds");
        clockRemainingInInterval.setLcdInfoString("Remaining in interval");
        clockRemainingInInterval.setLcdDecimals(0);
        clockRemainingInInterval.setLcdColor(LcdColor.STANDARD_LCD);
        panelNorth.add(clockRemainingInInterval);
        mainPanel.add(panelNorth, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(Color.black);
        centerPanel.setLayout(new GridLayout(1, 3, 20, 20));
        mainPanel.add(centerPanel);

        centerPanel.add(cadenceGauge);
        centerPanel.add(powerGauge);
        centerPanel.add(heartRateGauge);

        panelSouth = new JPanel();
        panelSouth.setBackground(Color.black);
        panelSouth.setLayout(new GridLayout(1, 3, 20, 20));
        mainPanel.add(panelSouth, BorderLayout.SOUTH);

        frame.setSize(2 * GAUGE_PREFERRED_SIZE + 100, GAUGE_PREFERRED_SIZE + 100);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }


    private ChartPanel createChartPanelWithSeries(Map<Long, TrainingRangeView> limits, String titleSerieHigh, String titleSerieLow, String title, double minY, double maxY, int physioLimit) {
        TimePeriodValues yintervalseriesHigh = new TimePeriodValues(titleSerieHigh);
        TimePeriodValues yintervalseriesLow = new TimePeriodValues(titleSerieLow);
        TimePeriodValues yintervalPhysiologicalLimit = new TimePeriodValues("phy. limit");
        Second start = new Second(new Date());
        Second objPrev = start;
        Second objNext = start;
        for (Long timeInSeconds : limits.keySet()) {
            long low = limits.get(timeInSeconds).getLow();
            long high = limits.get(timeInSeconds).getHigh();
            Date nextIntervalStart = new Date(start.getStart().getTime()+ (timeInSeconds*1000));
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
        series2.addSeries(yintervalPhysiologicalLimit);

        return createChartPanel(series, series2, title, minY, maxY);
    }

    private static ChartPanel createChartPanel(XYDataset xydataset, XYDataset xydataset2, String title, double minY, double maxY) {

        final DateAxis domainAxis = new DateAxis("Time");
        final ValueAxis rangeAxis = new NumberAxis("Value");
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setUpperBound(maxY);
        rangeAxis.setLowerBound(minY);

        XYBarRenderer barRenderer = new XYBarRenderer();
        // serie 0 : interval low limit
        // serie 1 : interval high limit
        // serie 2 : physiological limit (ftp, fhr, ...)
        // serie 3 : realtime telemetry value
        barRenderer.setSeriesStroke(0, new BasicStroke(1F, 1, 1));
        barRenderer.setSeriesPaint(0, Color.cyan.darker().darker());
        barRenderer.setSeriesStroke(1, new BasicStroke(1F, 1, 1));
        barRenderer.setSeriesPaint(1, Color.cyan.brighter());

        XYLineAndShapeRenderer line3DRenderer = new XYLineAndShapeRenderer();
        line3DRenderer.setSeriesStroke(0, new BasicStroke(1F, 1, 1));
        line3DRenderer.setSeriesPaint(0, Color.gray);
        line3DRenderer.setSeriesStroke(1, new BasicStroke(2F, 1, 1));
        line3DRenderer.setSeriesPaint(1, Color.yellow);
        line3DRenderer.setSeriesLinesVisible(0, true);
        line3DRenderer.setSeriesShapesVisible(0, false);


        barRenderer.setBarPainter(new StandardXYBarPainter());
        barRenderer.setShadowVisible(false);
        barRenderer.setDrawBarOutline(false);

        XYPlot xyplot = new XYPlot(xydataset, domainAxis, rangeAxis, barRenderer);

        xyplot.setRenderer(0, barRenderer);

        xyplot.setDataset(1, xydataset2);
        xyplot.setRenderer(1, line3DRenderer);

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
                return new Dimension(250, 250);
            }
        };
        return chartPanel;
    }
    // <editor-fold defaultstate="collapsed" desc="build clocks">
    private static DisplaySingle buildClockTotal() {
        DisplaySingle clockTotal = new DisplaySingle();
        clockTotal.setPreferredSize(new Dimension(GAUGE_MINIMUM_SIZE, 75));
        clockTotal.setMinimumSize(new Dimension(GAUGE_MINIMUM_SIZE, 75));
        clockTotal.setLcdUnitString("minutes");
        clockTotal.setLcdInfoString("Time elapsed total"); // TODO translate
        clockTotal.setLcdDecimals(0);
        clockTotal.setLcdValue(42);
        clockTotal.setLcdColor(LcdColor.STANDARD_LCD);
        return clockTotal;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="build control panel">
    private static void buildControlPanel() {
        stopButton = new JButton(
                UserPreferences.INSTANCE.messages.getString("stop"));
        stopButton.addActionListener(e -> MessageBus.INSTANCE.send(Messages.STOP, null));
        startButton = new JButton(
                UserPreferences.INSTANCE.messages.getString("start"));
        startButton.addActionListener(e -> MessageBus.INSTANCE.send(Messages.START, null));
    }



    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="callback">
    @Override
    public void callback(Messages message, Object o) {
        SwingUtilities.invokeLater(() -> {
            switch (message) {
                case SPEED:
                    Telemetry t = (Telemetry) o;
                    updateGaugesValues(t);
                    break;

                case TRAININGITEM:
                    current = (TrainingItem) o;
                    if (current != null) onNewInterval();
                    break;
                case TRAINING:
                    trainingData = (TrainingData) o;
                    if (trainingData != null) onNewTraining(trainingData);
            }
        });
    }

    // </editor-fold>

    private void onNewTraining(TrainingData trainingData) {
        trainingStartedTime = 0;
        Map<Long, TrainingRangeView> cadenceLimits = trainingData.getCadenceLimits();
        Map<Long, TrainingRangeView> powerRangeLimits = trainingData.getPowerRangeLimits();
        Map<Long, TrainingRangeView> heartRateRangeLimits = trainingData.getHeartRateRangeLimits();
        panelSouth.removeAll();
        cadenceChartPanel = createChartPanelWithSeries(cadenceLimits, "Cadence max", "Cadence min", "Cadence", 0, 140, 100);
        panelSouth.add(cadenceChartPanel);
        powerChartPanel = createChartPanelWithSeries(powerRangeLimits, "Power min", "Power max", "Power", 0, UserPreferences.INSTANCE.getMaxPower() * 1.5, UserPreferences.INSTANCE.getMaxPower());
        panelSouth.add(powerChartPanel);
        heartRateChartPanel = createChartPanelWithSeries(heartRateRangeLimits, "HR min", "HR max", "Heart Rate", 0, 220, UserPreferences.INSTANCE.getMaxHR());
        panelSouth.add(heartRateChartPanel);
        frame.pack();
    }


    /**
     * Set training limits with a cyan area in the corresponding gauge
     */
    private void onNewInterval() {
        System.out.println(current);

        TrainingRangeView cadenceRangeView = current.getCadenceRangeView();
        TrainingRangeView powerRangeView = current.getPowerRangeView();
        TrainingRangeView heartRateRangeView = current.getHeartRateRangeView();

        boolean hasCadenceZone = cadenceRangeView.hasZone();
        boolean hasPowerZone = powerRangeView.hasZone();
        boolean hasHRZone = heartRateRangeView.hasZone();

        Color areaColor = Color.cyan.darker();

        cadenceGauge.resetAreas();
        cadenceGauge.setAreasVisible(hasCadenceZone);
        cadenceGauge.setGlowing(hasCadenceZone);
        cadenceGauge.setGlowVisible(hasCadenceZone);
        cadenceGauge.setAreas(new Section(cadenceRangeView.getLow(), cadenceRangeView.getHigh(), areaColor));

        powerGauge.resetAreas();
        powerGauge.setAreasVisible(hasPowerZone);
        powerGauge.setGlowing(hasPowerZone);
        powerGauge.setGlowVisible(hasPowerZone);
        powerGauge.setAreas(new Section(powerRangeView.getLow(), powerRangeView.getHigh(), areaColor));

        heartRateGauge.resetAreas();
        heartRateGauge.setAreasVisible(hasHRZone);
        heartRateGauge.setGlowing(hasHRZone);
        heartRateGauge.setGlowVisible(hasHRZone);
        heartRateGauge.setAreas(new Section(heartRateRangeView.getLow(), heartRateRangeView.getHigh(), areaColor));
    }

    private void updateGaugesValues(Telemetry t) {
        updateLcdColors(t);
        System.out.println("telemetry.getTime()=" + t.getTime() + ", ~=" + new Date(t.getTime()).toString());
        if (current != null)
            clockRemainingInInterval.setLcdValue(intervalStartedTime + current.getTime() - t.getTime());
        clockTotal.setLcdValue(t.getTime() - trainingStartedTime);
        powerGauge.setValueAnimated(t.getPower());
        heartRateGauge.setValueAnimated(t.getHeartRate());
        cadenceGauge.setValueAnimated(t.getCadence());
    }


    private void updateLcdColors(Telemetry t) {
        if (current != null) {
            boolean cadenceTooLow = current.isCadenceInRange(t.getCadence()) == -1;
            boolean cadenceInRange = (current.isCadenceInRange(t.getCadence()) == 0) || (current.getCadenceLow() == 0) || (current.getCadenceHigh() == 0);
            boolean cadenceTooHigh = current.isCadenceInRange(t.getCadence()) == 1;
            if (cadenceTooLow) {
                cadenceGauge.setLcdColor(LcdColor.BLUE_LCD);
                cadenceGauge.setLcdInfoString("LOW");
            }
            if (cadenceInRange)
                cadenceGauge.setLcdColor(LcdColor.GREEN_LCD);
            if (cadenceTooHigh) {
                cadenceGauge.setLcdColor(LcdColor.ORANGE_LCD);
                cadenceGauge.setLcdInfoString("HIGH");
            }

            boolean powerTooLow = current.isPowerInRange(t.getPower()) == -1;
            boolean powerInRange = current.isPowerInRange(t.getPower()) == 0 || (current.getPowerLow() == 0) || (current.getPowerHigh() == 0);
            boolean powerTooHigh = current.isPowerInRange(t.getPower()) == 1;
            if (powerTooLow) {
                powerGauge.setLcdColor(LcdColor.BLUE_LCD);
                powerGauge.setLcdInfoString("LOW");
            }
            if (powerInRange)
                powerGauge.setLcdColor(LcdColor.GREEN_LCD);
            if (powerTooHigh) {
                powerGauge.setLcdColor(LcdColor.ORANGE_LCD);
                powerGauge.setLcdInfoString("HIGH");
            }

            boolean heartRateTooLow = current.isHRInRange(t.getHeartRate()) == -1;
            boolean heartRateInRange = current.isHRInRange(t.getHeartRate()) == 0 || (current.getHrLow() == 0) || (current.getHrHigh() == 0);
            boolean heartRateTooHigh = current.isHRInRange(t.getHeartRate()) == 1;
            if (heartRateTooLow) {
                heartRateGauge.setLcdColor(LcdColor.BLUE_LCD);
                heartRateGauge.setLcdInfoString("LOW");
            }
            if (heartRateInRange)
                heartRateGauge.setLcdColor(LcdColor.GREEN_LCD);
            if (heartRateTooHigh) {
                heartRateGauge.setLcdColor(LcdColor.ORANGE_LCD);
                heartRateGauge.setLcdInfoString("HIGH");
            }

            clockRemainingInInterval.setLcdValue(current.getTimeInSeconds());

        }
    }

    public static void main(String[] args) {
        JavaSwingGaugesView view = new JavaSwingGaugesView();
        view.show();

        // for testing purposes
        new DataInjector();

    }


}
