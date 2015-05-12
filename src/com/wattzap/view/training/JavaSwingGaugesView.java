package com.wattzap.view.training;

import com.wattzap.controller.MessageBus;
import com.wattzap.controller.MessageCallback;
import com.wattzap.controller.Messages;
import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.Telemetry;
import com.wattzap.model.dto.TrainingItem;
import com.wattzap.utils.DataInjector;
import eu.hansolo.steelseries.extras.StopWatch;
import eu.hansolo.steelseries.gauges.Radial;
import eu.hansolo.steelseries.tools.BackgroundColor;
import eu.hansolo.steelseries.tools.ForegroundType;
import eu.hansolo.steelseries.tools.Section;
import eu.hansolo.steelseries.tools.TicklabelOrientation;

import javax.swing.*;
import java.awt.*;

/**
 * Created by nicolas on 08/05/2015.
 */
public class JavaSwingGaugesView implements MessageCallback {

    private static final double MIN_RPM = 0;
    public static final int MAX_HR = 220;
    public static final int MIN_HR = 30;
    public static final int MAX_RPM = 180;
    public static final int MIN_POWER = 0;
    public static final int GAUGE_PREFERRED_SIZE = 400;
    private static double MAX_POWER; // ftp * 2

    private static Radial powerGauge;
    private static Radial cadenceGauge;
    private static Radial heartRateGauge;

    private static StopWatch stopWatch;

    private TrainingItem current;
    private static JButton stopButton;
    private static JButton startButton;

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
        // This method is invoked on the EDT thread
        JFrame frame = new JFrame("Gauges");
        JPanel panel = new JPanel();
        frame.add(panel);
        panel.setLayout(new FlowLayout());
        createGauges();
        panel.add(stopWatch);
        panel.add(cadenceGauge);
        panel.add(powerGauge);
        panel.add(heartRateGauge);
        panel.add(startButton);
        panel.add(stopButton);
        frame.setSize(3 * GAUGE_PREFERRED_SIZE + 100, GAUGE_PREFERRED_SIZE + 100);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }


    private static void createGauges() {
        initStopWatch();
        initCadenceGauge();
        initPowerGauge();
        initHeartRateGauge();
        initControlPanel();
    }

    private static void initStopWatch() {
        stopWatch = new StopWatch();
        stopWatch.setPreferredSize(new Dimension(GAUGE_PREFERRED_SIZE, GAUGE_PREFERRED_SIZE));
        stopWatch.setValueAnimated(10);
        stopWatch.setBackgroundColor(BackgroundColor.CARBON);
        stopWatch.setForegroundType(ForegroundType.FG_TYPE4);
        stopWatch.setLedVisible(false);
    }

    private static void initControlPanel() {
        stopButton = new JButton(
                UserPreferences.INSTANCE.messages.getString("stop"));
        stopButton.addActionListener(e -> MessageBus.INSTANCE.send(Messages.STOP, null));
        startButton = new JButton(
                UserPreferences.INSTANCE.messages.getString("start"));
        startButton.addActionListener(e -> MessageBus.INSTANCE.send(Messages.START, null));

    }

    private static void initCadenceGauge() {
        cadenceGauge = new Radial();
        cadenceGauge.setPreferredSize(new Dimension(GAUGE_PREFERRED_SIZE, GAUGE_PREFERRED_SIZE));
        cadenceGauge.setTitle("Cadence");
        cadenceGauge.setUnitString("RPM");
        cadenceGauge.setMinValue(MIN_RPM);
        cadenceGauge.setMaxValue(MAX_RPM);
        cadenceGauge.setBackgroundColor(BackgroundColor.CARBON);
        cadenceGauge.setForegroundType(ForegroundType.FG_TYPE4);
        cadenceGauge.setLedVisible(false);
        cadenceGauge.setTicklabelOrientation(TicklabelOrientation.HORIZONTAL);
        cadenceGauge.setMajorTickSpacing(10);
        cadenceGauge.setMinorTickSpacing(5);
    }

    private static void initHeartRateGauge() {
        heartRateGauge = new Radial();
        heartRateGauge.setPreferredSize(new Dimension(GAUGE_PREFERRED_SIZE, GAUGE_PREFERRED_SIZE));
        heartRateGauge.setTitle("Heart Rate");
        heartRateGauge.setUnitString("BPM");
        heartRateGauge.setMinValue(MIN_HR);
        heartRateGauge.setMaxValue(MAX_HR);
        heartRateGauge.setLedVisible(false);
        heartRateGauge.setBackgroundColor(BackgroundColor.CARBON);
        heartRateGauge.setForegroundType(ForegroundType.FG_TYPE4);

        heartRateGauge.setMajorTickSpacing(10);
        heartRateGauge.setMinorTickSpacing(5);

        double fhr = UserPreferences.INSTANCE.getMaxHR();

        // TODO retrieve colors from userpreferences
        heartRateGauge.addSection(new Section(MIN_HR, fhr * 0.68, Color.blue.brighter()));  // active recovery < 68%
        heartRateGauge.addSection(new Section(fhr * 0.68, fhr * 0.83, Color.green)); // Endurance 69 - 83%
        heartRateGauge.addSection(new Section(fhr * 0.83, fhr * 0.94, Color.yellow)); // Tempo 84 - 94%
        heartRateGauge.addSection(new Section(fhr * 0.94, fhr * 1.05, Color.orange)); // Lactate Threshold 95-105%
        heartRateGauge.addSection(new Section(fhr * 1.05, MAX_HR, Color.red)); // VO2Max

        heartRateGauge.setTicklabelOrientation(TicklabelOrientation.HORIZONTAL);
        heartRateGauge.setSectionsVisible(true);
    }

    private static void initPowerGauge() {
        powerGauge = new Radial();

        powerGauge.setPreferredSize(new Dimension(GAUGE_PREFERRED_SIZE, GAUGE_PREFERRED_SIZE));
        powerGauge.setTitle("Power");
        powerGauge.setUnitString("Watts");
        powerGauge.setMinValue(MIN_POWER);
        powerGauge.setLedVisible(false);
        powerGauge.setBackgroundColor(BackgroundColor.CARBON);
        powerGauge.setForegroundType(ForegroundType.FG_TYPE4);

        double ftp = UserPreferences.INSTANCE.getMaxPower();
        MAX_POWER = ftp * 2;
        powerGauge.setMaxValue(MAX_POWER);

        // TODO retrieve colors from userpreferences
        powerGauge.addSection(new Section(MIN_POWER, ftp * 0.55, Color.blue.brighter()));
        powerGauge.addSection(new Section(ftp * 0.55, ftp * 0.75, Color.green));
        powerGauge.addSection(new Section(ftp * 0.75, ftp * 0.90, Color.yellow));
        powerGauge.addSection(new Section(ftp * 0.90, ftp * 1.05, Color.orange));
        powerGauge.addSection(new Section(ftp * 1.05, ftp * 1.2, Color.red));
        powerGauge.addSection(new Section(ftp * 1.2, ftp * 1.5, Color.magenta));
        powerGauge.addSection(new Section(ftp * 1.5, MAX_POWER, Color.darkGray));

        powerGauge.setSectionsVisible(true);
        powerGauge.setTicklabelOrientation(TicklabelOrientation.HORIZONTAL);
        powerGauge.setMajorTickSpacing(50);
        powerGauge.setMinorTickSpacing(10);
    }

    @Override
    public void callback(Messages message, Object o) {
        switch (message) {
            case SPEED:
                Telemetry t = (Telemetry) o;
                updateGaugesValues(t);
                break;

            case TRAININGITEM:
                current = (TrainingItem) o;
                if (current != null) updateGaugesTrainingZones();
                break;
        }
    }

    /**
     * Set training limits with a cyan area in the corresponding gauge
     */
    private void updateGaugesTrainingZones() {
        System.out.println(current);
        double powerLow = current.getPowerLow();
        double powerHigh = current.getPowerHigh();
        if (powerHigh < powerLow) powerHigh = MAX_POWER;
        if (powerLow > powerHigh) powerLow = MIN_POWER;
        double cadenceLow = current.getCadenceLow();
        double cadenceHigh = current.getCadenceHigh();
        if (cadenceHigh < cadenceLow) cadenceHigh = MAX_RPM;
        if (cadenceLow > cadenceHigh) cadenceLow = MIN_RPM;
        double hrLow = current.getHrLow();
        double hrHigh = current.getHrHigh();
        if (hrHigh < hrLow) hrHigh = MAX_HR;
        if (hrLow > hrHigh) hrLow = MIN_HR;

        boolean hasPowerZone = powerLow != 0 && powerHigh != 0;
        boolean hasHearRateZone = hrLow != 0 && hrHigh != 0;
        boolean hasCadenceZone = cadenceLow != 0 && cadenceHigh != 0;

        powerGauge.resetAreas();
        powerGauge.setAreasVisible(hasPowerZone);
        powerGauge.setAreas(new Section(powerLow, powerHigh, Color.cyan));

        heartRateGauge.resetAreas();
        heartRateGauge.setAreasVisible(hasHearRateZone);
        heartRateGauge.setAreas(new Section(hrLow, hrHigh, Color.cyan));

        cadenceGauge.resetAreas();
        cadenceGauge.setAreasVisible(hasCadenceZone);
        cadenceGauge.setAreas(new Section(cadenceLow, cadenceHigh, Color.cyan));
    }

    private void updateGaugesValues(Telemetry t) {
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
