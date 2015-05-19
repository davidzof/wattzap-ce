package com.wattzap.view.training;

import com.wattzap.controller.MessageBus;
import com.wattzap.controller.MessageCallback;
import com.wattzap.controller.Messages;
import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.Telemetry;
import com.wattzap.model.dto.TrainingItem;
import com.wattzap.utils.DataInjector;
import eu.hansolo.steelseries.extras.StopWatch;
import eu.hansolo.steelseries.gauges.DisplaySingle;
import eu.hansolo.steelseries.gauges.Radial;
import eu.hansolo.steelseries.gauges.SparkLine;
import eu.hansolo.steelseries.tools.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

/**
 * Created by nicolas on 08/05/2015.
 */
public class JavaSwingGaugesView implements MessageCallback {

    private static final double MIN_RPM = 0;
    public static final int MAX_HR = 220;
    public static final int MIN_HR = 30;
    public static final int MAX_RPM = 180;
    public static final int MIN_POWER = 0;
    public static final int GAUGE_PREFERRED_SIZE = 800;
    private static final int GAUGE_MINIMUM_SIZE = 250;
    private static double MAX_POWER; // ftp * 2

    private static Radial powerGauge;
    private static Radial cadenceGauge;
    private static Radial heartRateGauge;

    private static DisplaySingle clockTotal;
    private static DisplaySingle clockRemainingInInterval;

    private TrainingItem current;
    private static JButton stopButton;
    private static JButton startButton;
    private static SparkLine sparkLine;

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
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.black);
        frame.add(mainPanel);
        MigLayout mainLayout = new MigLayout(
                "align center, fill");
        mainPanel.setLayout(mainLayout);

        // build north panel
        JPanel panelNorth = new JPanel();
        panelNorth.setLayout(new MigLayout());
        panelNorth.add(new JLabel("Wattzap!"));
        clockTotal = buildClockTotal();
        panelNorth.add(clockTotal);
        clockRemainingInInterval = new DisplaySingle();
        clockRemainingInInterval.setPreferredSize(new Dimension(GAUGE_MINIMUM_SIZE, 75));
        clockRemainingInInterval.setMinimumSize(new Dimension(GAUGE_MINIMUM_SIZE, 75));
        clockRemainingInInterval.setLcdUnitString("minutes");
        clockRemainingInInterval.setLcdInfoString("Time remaining in current interval");
        clockRemainingInInterval.setLcdDecimals(0);
        clockRemainingInInterval.setLcdColor(LcdColor.STANDARD_LCD);
        panelNorth.add(clockRemainingInInterval);
        panelNorth.setBackground(Color.orange);

        sparkLine = new SparkLine();
        sparkLine.setMinimumSize(new Dimension(GAUGE_PREFERRED_SIZE, 75));
        sparkLine.setFilled(true);
        sparkLine.setAverageVisible(true);
        sparkLine.setAreaFill(ColorDef.BLUE);

        panelNorth.add(sparkLine);


        mainPanel.add(panelNorth, "dock north, shrink 0");

        // build center grid
        buildCadenceGauge();
        buildPowerGauge();
        buildHeartRateGauge();
        buildControlPanel();
        buildTrainingBirdView();

        mainPanel.add(cadenceGauge);
        mainPanel.add(powerGauge);
        mainPanel.add(heartRateGauge);

        // build south panel = controls
        JPanel panelSouth = new JPanel();
        panelSouth.setLayout(new MigLayout());
        panelSouth.setBackground(Color.green);
        panelSouth.add(startButton);
        panelSouth.add(stopButton);
        mainPanel.add(panelSouth, "dock south, shrink 0");


        frame.setSize(3 * GAUGE_PREFERRED_SIZE + 100, GAUGE_PREFERRED_SIZE*2 + 100);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }


    private static void buildTrainingBirdView() {
        // TODO add bar graph with x=time and y=intensity
        // bar color will follow intensity zone
        // icon will suit the target type (heart icon, power icon, rotor icon)
    }

    // <editor-fold defaultstate="collapsed" desc="build clocks">
    private static DisplaySingle buildClockTotal() {
        DisplaySingle clockTotal = new DisplaySingle();
        clockTotal.setPreferredSize(new Dimension(GAUGE_PREFERRED_SIZE, 75));
        clockTotal.setMinimumSize(new Dimension(GAUGE_PREFERRED_SIZE, 75));
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

    // <editor-fold defaultstate="collapsed" desc="build cadence gauge">
    private static void buildCadenceGauge() {
        cadenceGauge = new Radial();
        cadenceGauge.setPreferredSize(new Dimension(GAUGE_PREFERRED_SIZE, GAUGE_PREFERRED_SIZE));
        cadenceGauge.setMinimumSize(new Dimension(GAUGE_MINIMUM_SIZE, GAUGE_MINIMUM_SIZE));
        cadenceGauge.setTitle("Cadence");
        cadenceGauge.setUnitString("RPM");
        cadenceGauge.setMinValue(MIN_RPM);
        cadenceGauge.setMaxValue(MAX_RPM);
        cadenceGauge.setBackgroundColor(BackgroundColor.CARBON);
        cadenceGauge.setForegroundType(ForegroundType.FG_TYPE4);
        cadenceGauge.setFrameDesign(FrameDesign.BLACK_METAL);
        cadenceGauge.setLedVisible(false);
        cadenceGauge.setLcdColor(LcdColor.STANDARD_LCD);
        cadenceGauge.setTicklabelOrientation(TicklabelOrientation.HORIZONTAL);
        cadenceGauge.setMajorTickSpacing(10);
        cadenceGauge.setMinorTickSpacing(5);
        cadenceGauge.addSection(new Section(50, 80, Color.blue.darker()));
        cadenceGauge.addSection(new Section(80, 100, Color.green.darker()));
        cadenceGauge.addSection(new Section(100, 130, Color.orange.darker()));
//        cadenceGauge.setTransparentSectionsEnabled(true);
//        cadenceGauge.setExpandedSectionsEnabled(true);
        cadenceGauge.setSection3DEffectVisible(true);
        cadenceGauge.setSectionsVisible(true);
        // for cadence, target 90 rpm
//        cadenceGauge.setTrackVisible(true);
//        cadenceGauge.setTrackStartColor(Color.blue);
//        cadenceGauge.setTrackSectionColor(Color.green);
//        cadenceGauge.setTrackStopColor(Color.red);
//        cadenceGauge.setTrackStart(40);
//        cadenceGauge.setTrackStop(150);
//        cadenceGauge.setTrackSection(90);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="build hear rate gauge">
    private static void buildHeartRateGauge() {
        heartRateGauge = new Radial();
        heartRateGauge.setPreferredSize(new Dimension(GAUGE_PREFERRED_SIZE, GAUGE_PREFERRED_SIZE));
        heartRateGauge.setMinimumSize(new Dimension(GAUGE_MINIMUM_SIZE, GAUGE_MINIMUM_SIZE));
        heartRateGauge.setTitle("Heart Rate");
        heartRateGauge.setUnitString("BPM");
        heartRateGauge.setMinValue(MIN_HR);
        heartRateGauge.setMaxValue(MAX_HR);
        heartRateGauge.setLedVisible(false);
        heartRateGauge.setFrameDesign(FrameDesign.BLACK_METAL);
        heartRateGauge.setBackgroundColor(BackgroundColor.CARBON);
        heartRateGauge.setForegroundType(ForegroundType.FG_TYPE4);

        heartRateGauge.setMajorTickSpacing(10);
        heartRateGauge.setMinorTickSpacing(5);

        double fhr = UserPreferences.INSTANCE.getMaxHR();

        // TODO retrieve colors from userpreferences
        heartRateGauge.addSection(new Section(MIN_HR, fhr * 0.68, Color.blue.brighter().brighter()));  // active recovery < 68%
        heartRateGauge.addSection(new Section(fhr * 0.68, fhr * 0.83, Color.green)); // Endurance 69 - 83%
        heartRateGauge.addSection(new Section(fhr * 0.83, fhr * 0.94, Color.yellow.brighter().brighter())); // Tempo 84 - 94%
        heartRateGauge.addSection(new Section(fhr * 0.94, fhr * 1.05, Color.orange.darker())); // Lactate Threshold 95-105%
        heartRateGauge.addSection(new Section(fhr * 1.05, fhr * 1.15, Color.red.darker())); // VO2Max
//        heartRateGauge.setTransparentSectionsEnabled(true);
        heartRateGauge.setLcdColor(LcdColor.STANDARD_LCD);
//        heartRateGauge.setExpandedSectionsEnabled(true);
        heartRateGauge.setSection3DEffectVisible(true);
        heartRateGauge.setTicklabelOrientation(TicklabelOrientation.HORIZONTAL);
        heartRateGauge.setSectionsVisible(true);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="build hear rate gauge">
    private static void buildPowerGauge() {
        powerGauge = new Radial();
        powerGauge.setPreferredSize(new Dimension(GAUGE_PREFERRED_SIZE, GAUGE_PREFERRED_SIZE));
        powerGauge.setMinimumSize(new Dimension(GAUGE_MINIMUM_SIZE, GAUGE_MINIMUM_SIZE));
        powerGauge.setTitle("Power");
        powerGauge.setUnitString("Watts");
        powerGauge.setMinValue(MIN_POWER);
        powerGauge.setLedVisible(false);
        powerGauge.setBackgroundColor(BackgroundColor.CARBON);
        powerGauge.setForegroundType(ForegroundType.FG_TYPE4);
        powerGauge.setLcdColor(LcdColor.STANDARD_LCD);
        powerGauge.setFrameDesign(FrameDesign.BLACK_METAL);
        double ftp = UserPreferences.INSTANCE.getMaxPower();
        MAX_POWER = ftp * 2;
        powerGauge.setMaxValue(MAX_POWER);

        // sections
        // TODO retrieve colors from user preferences
        powerGauge.addSection(new Section(MIN_POWER, ftp * 0.55, Color.blue.brighter()));
        powerGauge.addSection(new Section(ftp * 0.55, ftp * 0.75, Color.green));
        powerGauge.addSection(new Section(ftp * 0.75, ftp * 0.90, Color.yellow));
        powerGauge.addSection(new Section(ftp * 0.90, ftp * 1.05, Color.orange));
        powerGauge.addSection(new Section(ftp * 1.05, ftp * 1.2, Color.red));
        powerGauge.addSection(new Section(ftp * 1.2, ftp * 1.5, Color.magenta));
        powerGauge.addSection(new Section(ftp * 1.5, MAX_POWER, Color.darkGray));
        powerGauge.setSectionsVisible(true);
        powerGauge.setSection3DEffectVisible(true);
//        powerGauge.setTransparentSectionsEnabled(true);
//        powerGauge.setExpandedSectionsEnabled(true);

        // ticks
        powerGauge.setTicklabelOrientation(TicklabelOrientation.HORIZONTAL);
        powerGauge.setMajorTickSpacing(50);
        powerGauge.setMinorTickSpacing(10);
    }
    // </editor-fold>

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
                    if (current != null) updateGaugesTrainingZones();
                    break;
            }
        });
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

        boolean hasPowerZone = powerLow != 0 || powerHigh != 0;
        System.out.println("hasPowerZone="+hasPowerZone);
        boolean hasHRZone = hrLow != 0 || hrHigh != 0;
        System.out.println("hasHRZone="+hasHRZone);
        boolean hasCadenceZone = cadenceLow != 0 || cadenceHigh != 0;
        System.out.println("hasCadenceZone="+hasCadenceZone);

        Color areaColor = Color.cyan;

        powerGauge.resetAreas();
        powerGauge.setAreasVisible(hasPowerZone);
        powerGauge.setGlowing(hasPowerZone);
        powerGauge.setGlowColor(Color.cyan);
        powerGauge.setGlowVisible(hasPowerZone);
//        powerGauge.setTransparentAreasEnabled(true);
        powerGauge.setAreas(new Section(powerLow, powerHigh, areaColor));

        heartRateGauge.resetAreas();
        heartRateGauge.setAreasVisible(hasHRZone);
        heartRateGauge.setGlowing(hasHRZone);
        heartRateGauge.setGlowColor(Color.cyan);
        heartRateGauge.setGlowVisible(hasHRZone);
//        heartRateGauge.setTransparentAreasEnabled(true);
        heartRateGauge.setAreas(new Section(hrLow, hrHigh, areaColor));

        cadenceGauge.resetAreas();
        cadenceGauge.setAreasVisible(hasCadenceZone);
        cadenceGauge.setGlowing(hasCadenceZone);
        cadenceGauge.setGlowColor(Color.cyan);
        cadenceGauge.setGlowVisible(hasCadenceZone);
//        cadenceGauge.setTransparentAreasEnabled(true);
        cadenceGauge.setAreas(new Section(cadenceLow, cadenceHigh, areaColor));
    }

    private void updateGaugesValues(Telemetry t) {
        updateLcdColors(t);
        clockTotal.setLcdValue(t.getTime());
        powerGauge.setValueAnimated(t.getPower());
        sparkLine.addDataPoint(t.getPower());
        heartRateGauge.setValueAnimated(t.getHeartRate());
        cadenceGauge.setValueAnimated(t.getCadence());
    }

    private void updateLcdColors(Telemetry t) {
        if (current != null) {
            boolean cadenceTooLow = current.isCadenceInRange(t.getCadence()) == -1;
            boolean cadenceInRange = (current.isCadenceInRange(t.getCadence()) == 0) || (current.getCadenceLow() == 0) || (current.getCadenceHigh() == 0);
            boolean cadenceTooHigh = current.isCadenceInRange(t.getCadence()) == 1;
            if (cadenceTooLow) cadenceGauge.setLcdColor(LcdColor.BLUE_LCD);
            if (cadenceInRange) cadenceGauge.setLcdColor(LcdColor.STANDARD_GREEN_LCD);
            if (cadenceTooHigh) cadenceGauge.setLcdColor(LcdColor.ORANGE_LCD);

            boolean powerTooLow = current.isPowerInRange(t.getPower()) == -1;
            boolean powerInRange = current.isPowerInRange(t.getPower()) == 0 || (current.getPowerLow() == 0) || (current.getPowerHigh() == 0);
            boolean powerTooHigh = current.isPowerInRange(t.getPower()) == 1;
            if (powerTooLow) powerGauge.setLcdColor(LcdColor.BLUE_LCD);
            if (powerInRange) powerGauge.setLcdColor(LcdColor.STANDARD_GREEN_LCD);
            if (powerTooHigh) powerGauge.setLcdColor(LcdColor.ORANGE_LCD);

            boolean heartRateTooLow = current.isHRInRange(t.getHeartRate()) == -1;
            boolean heartRateInRange = current.isHRInRange(t.getHeartRate()) == 0 || (current.getHrLow() == 0) || (current.getHrHigh() == 0);
            boolean heartRateTooHigh = current.isHRInRange(t.getHeartRate()) == 1;
            if (heartRateTooLow) heartRateGauge.setLcdColor(LcdColor.BLUE_LCD);
            if (heartRateInRange) heartRateGauge.setLcdColor(LcdColor.STANDARD_GREEN_LCD);
            if (heartRateTooHigh) heartRateGauge.setLcdColor(LcdColor.ORANGE_LCD);

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
