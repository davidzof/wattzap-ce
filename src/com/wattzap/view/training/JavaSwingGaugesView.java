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
import com.wattzap.utils.TimeFormatterUtility;
import eu.hansolo.steelseries.gauges.Radial;
import eu.hansolo.steelseries.tools.LcdColor;
import eu.hansolo.steelseries.tools.Section;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartPanel;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimePeriodValuesCollection;

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

    private static JLabel clockTotal;
    private static JLabel clockRemainingInInterval;

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
    private Telemetry currentTelemetry;

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

        clockTotal = buildClock();
        clockRemainingInInterval = buildClock();
        panelNorth.add(clockTotal);
        labelInfoInterval.setForeground(Color.white);
        labelInfoInterval.setFont(new Font("Dialog", Font.BOLD, 28));
        labelInfoInterval.setMaximumSize(new Dimension(1920, 75));
        labelInfoInterval.setHorizontalTextPosition(SwingConstants.CENTER);
        panelNorth.add(labelInfoInterval);
        panelNorth.add(clockRemainingInInterval);

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

    private static JLabel buildClock() {
        JLabel clock = new JLabel();
        clock.setPreferredSize(new Dimension(GAUGE_MINIMUM_SIZE, 75));
        clock.setMinimumSize(new Dimension(GAUGE_MINIMUM_SIZE, 75));
        clock.setSize(new Dimension(GAUGE_MINIMUM_SIZE, 75));
        clock.setForeground(Color.white);
        clock.setFont(new Font("Dialog", Font.BOLD, 28));
        return clock;
    }

    private static void buildControlPanel() {
        stopButton = new JButton(
                UserPreferences.INSTANCE.messages.getString("stop"));
        stopButton.addActionListener(e -> MessageBus.INSTANCE.send(Messages.STOP, null));
        startButton = new JButton(
                UserPreferences.INSTANCE.messages.getString("start"));
        startButton.addActionListener(e -> MessageBus.INSTANCE.send(Messages.START, null));
    }

    @Override
    public void callback(Messages message, Object o) {
        System.out.println("\nMessage " + message + ", object = " + o);
        SwingUtilities.invokeLater(() -> {
            switch (message) {
                case HEARTRATE:
                    if (currentTelemetry!=null) {
                        currentTelemetry.setHeartRate((int) o);
                        onTelemetry(currentTelemetry);
                    }
                    break;
                case CADENCE:
                    if (currentTelemetry!=null) {
                        currentTelemetry.setCadence((int) o);
                        onTelemetry(currentTelemetry);
                    }
                    break;
                case SPEED:
                    Telemetry t = (Telemetry) o;
                    // update with last received cadence and heart rate values, if any
                    if (currentTelemetry!=null) {
                        t.setHeartRate(currentTelemetry.getHeartRate());
                        t.setCadence(currentTelemetry.getCadence());
                    }
                    onTelemetry(t);
                    break;
                case TRAININGITEM:
                    if (current != null) accumulatedTrainingTime = current.getTime();
                    System.out.println("Accumulated training time = " + accumulatedTrainingTime / 1000 + " seconds");
                    current = (TrainingItem) o;
                    if (current != null) onInterval();
                    break;
                case TRAINING:
                    trainingData = (TrainingData) o;
                    accumulatedTrainingTime = 0;
                    if (trainingData != null) onNewTraining(trainingData);
            }
        });
    }


    private void onNewTraining(TrainingData trainingData) {
        noTelemetryYet = true;
        // create chart for a bird eye view of the training,
        // they also will be created when training really start for an accurate time view
        createCharts(trainingData);
    }

    private void createCharts(TrainingData trainingData) {
        Map<Long, TrainingRangeView> cadenceLimits = trainingData.getCadenceLimits();
        Map<Long, TrainingRangeView> powerRangeLimits = trainingData.getPowerRangeLimits();
        Map<Long, TrainingRangeView> heartRateRangeLimits = trainingData.getHeartRateRangeLimits();
        panelSouth.removeAll();
        cadenceChartPanel = ChartPanelBuilder.createChartPanelWithSeries(cadenceLimits, "Cadence max", "Cadence min", "Cadence", 0, 140, 100);
        panelSouth.add(cadenceChartPanel);
        powerChartPanel = ChartPanelBuilder.createChartPanelWithSeries(powerRangeLimits, "Power min", "Power max", "Power", 0, UserPreferences.INSTANCE.getMaxPower() * 1.5, UserPreferences.INSTANCE.getMaxPower());
        panelSouth.add(powerChartPanel);
        heartRateChartPanel = ChartPanelBuilder.createChartPanelWithSeries(heartRateRangeLimits, "HR min", "HR max", "Heart Rate", 0, 220, UserPreferences.INSTANCE.getMaxHR());
        panelSouth.add(heartRateChartPanel);
        frame.pack();
    }

    /**
     * Set training limits with a cyan area in the corresponding gauge
     */
    private void onInterval() {
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

    private void onTelemetry(Telemetry t) {
        currentTelemetry = t;
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
        clockTotal.setText(TimeFormatterUtility.timeToString((t.getTime() - trainingStartedTime)));
        if (current != null) {
            clockRemainingInInterval.setText(TimeFormatterUtility.timeToString((current.getTime() - accumulatedTrainingTime - (t.getTime() - intervalStartedTime)) + 1000));
        }
    }

    private void updateGraphs(Telemetry t) {
        if (cadenceChartPanel!=null) {
            TimePeriodValuesCollection datasetCadence = (TimePeriodValuesCollection) cadenceChartPanel.getChart().getXYPlot().getDataset(0);
            datasetCadence.getSeries(0).add(new Second(new Date(t.getTime())), t.getCadence());
        }
        if (powerChartPanel!=null) {
            TimePeriodValuesCollection datasetPower = (TimePeriodValuesCollection) powerChartPanel.getChart().getXYPlot().getDataset(0);
            datasetPower.getSeries(0).add(new Second(new Date(t.getTime())), t.getPower());
        }
        if (heartRateChartPanel!=null) {
            TimePeriodValuesCollection datasetHeartRate = (TimePeriodValuesCollection) heartRateChartPanel.getChart().getXYPlot().getDataset(0);
            datasetHeartRate.getSeries(0).add(new Second(new Date(t.getTime())), t.getHeartRate());
        }
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
            if (cadenceInRange) {
                cadenceGauge.setLcdColor(LcdColor.GREEN_LCD);
                cadenceGauge.setLcdInfoString("");
            }
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
            if (powerInRange) {
                powerGauge.setLcdColor(LcdColor.GREEN_LCD);
                powerGauge.setLcdInfoString("");
            }
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
            if (heartRateInRange) {
                heartRateGauge.setLcdColor(LcdColor.GREEN_LCD);
                heartRateGauge.setLcdInfoString("");
            }
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
