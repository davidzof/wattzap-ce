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
import eu.hansolo.steelseries.tools.LcdColor;
import eu.hansolo.steelseries.tools.Section;
import net.miginfocom.swing.MigLayout;
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

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.Map;

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

    private static ChartPanel cadenceChartPanel;
    private static ChartPanel powerChartPanel;
    private static ChartPanel heartRateChartPanel;
    private static JPanel panelSouth;
    private static JFrame frame;
    private boolean noTelemetryYet;

    private static JLabel labelInfoInterval = new JLabel();
    private long accumulatedTrainingTime = 0;

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

        // build panels
        JPanel panelNorth = new JPanel();
        JPanel centerPanel = new JPanel();
        panelSouth = new JPanel();

        panelNorth.setBackground(Color.black);
        panelNorth.setLayout(new MigLayout());
        panelNorth.setMaximumSize(new Dimension(1920, 75));

        mainPanel.add(panelNorth, BorderLayout.NORTH);
        mainPanel.add(centerPanel);
        mainPanel.add(panelSouth, BorderLayout.SOUTH);

        panelNorth.add(startButton);
        panelNorth.add(stopButton);


        clockTotal = buildClockTotal();
        clockRemainingInInterval = buildClockRemaining();
        panelNorth.add(clockTotal);
        panelNorth.add(clockRemainingInInterval);
        labelInfoInterval.setForeground(Color.white);
        labelInfoInterval.setFont(new Font("Dialog", Font.BOLD, 28));
        labelInfoInterval.setMaximumSize(new Dimension(1920, 75));
        labelInfoInterval.setHorizontalTextPosition(SwingConstants.CENTER);
        panelNorth.add(labelInfoInterval);

        centerPanel.setBackground(Color.black);
        centerPanel.setLayout(new GridLayout(1, 3, 20, 20));

        centerPanel.add(cadenceGauge);
        centerPanel.add(powerGauge);
        centerPanel.add(heartRateGauge);

        panelSouth.setBackground(Color.black);
        panelSouth.setLayout(new GridLayout(1, 3, 20, 20));

        frame.setSize(2 * GAUGE_PREFERRED_SIZE + 100, GAUGE_PREFERRED_SIZE + 100);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static DisplaySingle buildClockRemaining() {
        DisplaySingle clockRemainingInInterval = new DisplaySingle();
        clockRemainingInInterval.setPreferredSize(new Dimension(GAUGE_MINIMUM_SIZE, 75));
        clockRemainingInInterval.setMinimumSize(new Dimension(GAUGE_MINIMUM_SIZE, 75));
        clockRemainingInInterval.setLcdUnitString("seconds");
        clockRemainingInInterval.setLcdInfoString("Remaining in interval");
        clockRemainingInInterval.setLcdDecimals(0);
        clockRemainingInInterval.setLcdColor(LcdColor.STANDARD_LCD);
        return clockRemainingInInterval;
    }


    private ChartPanel createChartPanelWithSeries(Map<Long, TrainingRangeView> limits, String titleSerieHigh, String titleSerieLow, String title, double minY, double maxY, int physioLimit) {
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

    private static ChartPanel createChartPanel(XYDataset xydataset, XYDataset xydataset2, String title, double minY, double maxY) {

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

    // <editor-fold defaultstate="collapsed" desc="build clock total">
    private static DisplaySingle buildClockTotal() {
        DisplaySingle clockTotal = new DisplaySingle();
        clockTotal.setPreferredSize(new Dimension(GAUGE_MINIMUM_SIZE, 75));
        clockTotal.setMinimumSize(new Dimension(GAUGE_MINIMUM_SIZE, 75));
        clockTotal.setLcdUnitString("seconds");
        clockTotal.setLcdInfoString("Time elapsed total"); // TODO translate
        clockTotal.setLcdDecimals(0);
        clockTotal.setLcdValue(0);
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
                    onNewTelemetry(t);
                    break;
                case TRAININGITEM:
                    if (current != null) accumulatedTrainingTime = current.getTime();
                    System.out.println("Accumulated training time = " + accumulatedTrainingTime / 1000 + " seconds");
                    current = (TrainingItem) o;
                    if (current != null) onNewInterval();
                    break;
                case TRAINING:
                    trainingData = (TrainingData) o;
                    accumulatedTrainingTime = 0;
                    if (trainingData != null) onNewTraining(trainingData);
            }
        });
    }

    // </editor-fold>

    private void onNewTraining(TrainingData trainingData) {
        noTelemetryYet = true;
        // create chart for a bird eye view of the training
        // thy will be re-created when training really start for an accurate time view
        createCharts(trainingData);
    }

    private void createCharts(TrainingData trainingData) {
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
        System.out.println("current interval " + current);
        labelInfoInterval.setText(current.getDescription());
        frame.pack();
        intervalStartedTime = 0;
        System.out.println("Interval started time = " + new Date(intervalStartedTime).toString());
        System.out.println("Interval duration = " + current.getTime() / 1000 + " seconds");

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

    private void onNewTelemetry(Telemetry t) {
        if (noTelemetryYet && trainingData != null) {
            noTelemetryYet = false;
            trainingStartedTime = new Date().getTime();
            System.out.println("Training started time = " + new Date(trainingStartedTime).toString());
            // recreate charts...
            System.out.println("First telemetry, recreate charts...");
            createCharts(trainingData);
        }
        if (current != null && intervalStartedTime == 0) intervalStartedTime = t.getTime();
        System.out.println("telemetry.getTime()=" + t.getTime() + ", ~=" + new Date(t.getTime()).toString());
        updateClocks(t);
        updateGauges(t);
        updateLcdColors(t);
        updateGraphs(t);
    }

    private void updateClocks(Telemetry t) {
        clockTotal.setLcdValue((t.getTime() - trainingStartedTime) / 1000);
        if (current != null) {
            clockRemainingInInterval.setLcdValue((current.getTime() - accumulatedTrainingTime - (t.getTime() - intervalStartedTime)) / 1000 + 1);
        }
    }

    private void updateGraphs(Telemetry t) {
        TimePeriodValuesCollection datasetCadence = (TimePeriodValuesCollection) cadenceChartPanel.getChart().getXYPlot().getDataset(0);
        datasetCadence.getSeries(0).add(new Second(new Date(t.getTime())), t.getCadence());
        TimePeriodValuesCollection datasetPower = (TimePeriodValuesCollection) powerChartPanel.getChart().getXYPlot().getDataset(0);
        datasetPower.getSeries(0).add(new Second(new Date(t.getTime())), t.getPower());
        TimePeriodValuesCollection datasetHeartRate = (TimePeriodValuesCollection) heartRateChartPanel.getChart().getXYPlot().getDataset(0);
        datasetHeartRate.getSeries(0).add(new Second(new Date(t.getTime())), t.getHeartRate());
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


        }
    }


    private void updateGauges(Telemetry t) {
        powerGauge.setValueAnimated(t.getPower());
        heartRateGauge.setValueAnimated(t.getHeartRate());
        cadenceGauge.setValueAnimated(t.getCadence());
    }

    public static void main(String[] args) {
        JavaSwingGaugesView view = new JavaSwingGaugesView();
        view.show();

        // for testing purposes
        new DataInjector();

    }


}
