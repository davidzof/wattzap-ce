//package com.wattzap.view.javafx;
//
//import com.wattzap.controller.MessageBus;
//import com.wattzap.controller.MessageCallback;
//import com.wattzap.controller.Messages;
//import com.wattzap.model.UserPreferences;
//import com.wattzap.model.dto.Telemetry;
//import com.wattzap.model.dto.TrainingItem;
//import javafx.application.Platform;
//import javafx.embed.swing.JFXPanel;
//import javafx.geometry.Insets;
//import javafx.scene.Scene;
//import javafx.scene.layout.HBox;
//import javafx.scene.paint.Paint;
//
//import javax.swing.*;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by nicolas on 08/05/2015.
// */
//public class JavaFxGaugesView implements MessageCallback {
//
//    private static final double MIN_RPM = 0;
//    public static final int MAX_HR = 220;
//    public static final int MIN_HR = 30;
//    public static final int MAX_RPM = 180;
//    public static final int MIN_POWER = 0;
//    private static double MAX_POWER; // ftp * 2
//
//    private static Gauge powerGauge;
//    private static Gauge cadenceGauge;
//    private static Gauge heartRateGauge;
//    private TrainingItem current;
//
//    public JavaFxGaugesView() {
//        // register message bus events
//        MessageBus.INSTANCE.register(Messages.SPEED, this);
//        MessageBus.INSTANCE.register(Messages.CADENCE, this);
//        MessageBus.INSTANCE.register(Messages.HEARTRATE, this);
//        MessageBus.INSTANCE.register(Messages.START, this);
//        MessageBus.INSTANCE.register(Messages.STARTPOS, this);
//        MessageBus.INSTANCE.register(Messages.STOP, this);
//        MessageBus.INSTANCE.register(Messages.TRAINING, this);
//        MessageBus.INSTANCE.register(Messages.TRAININGITEM, this);
//        MessageBus.INSTANCE.register(Messages.CLOSE, this);
//    }
//
//    public void show() {
//        SwingUtilities.invokeLater(() -> JavaFxGaugesView.initAndShowGUI());
//    }
//
//    private static void initAndShowGUI() {
//        // This method is invoked on the EDT thread
//        JFrame frame = new JFrame("Gauges");
//        final JFXPanel fxPanel = new JFXPanel();
//        frame.add(fxPanel);
//        frame.setSize(800, 600);
//        frame.setVisible(true);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        Platform.runLater(() -> initFX(fxPanel));
//    }
//
//    private static void initFX(JFXPanel fxPanel) {
//        // This method is invoked on the JavaFX thread
//        Scene scene = createScene();
//        fxPanel.setScene(scene);
//    }
//
//    private static Scene createScene() {
//
//        initPowerGauge();
//        initHeartRateGauge();
//        initCadenceGauge();
//
//        HBox hbox = new HBox();
//        hbox.setPadding(new Insets(15, 12, 15, 12));
//        hbox.setSpacing(10);
//        hbox.getChildren().addAll(powerGauge, heartRateGauge, cadenceGauge);
//
//        Scene scene = new Scene(hbox, 800, 600);
//
//        return (scene);
//    }
//
//    private static void initCadenceGauge() {
//        cadenceGauge = new Gauge();
//        cadenceGauge.setPrefSize(800, 800);
//        cadenceGauge.setTitle("Cadence");
//        cadenceGauge.setUnit("RPM");
//        cadenceGauge.setMinValue(MIN_RPM);
//        cadenceGauge.setMaxValue(MAX_RPM);
//
//        cadenceGauge.setMajorTickSpace(10);
//        cadenceGauge.setMinorTickSpace(5);
//    }
//
//    private static void initHeartRateGauge() {
//        heartRateGauge = new Gauge();
//        heartRateGauge.setPrefSize(800, 800);
//        heartRateGauge.setTitle("Heart Rate");
//        heartRateGauge.setUnit("BPM");
//        heartRateGauge.setMinValue(MIN_HR);
//        heartRateGauge.setMaxValue(MAX_HR);
//
//        heartRateGauge.setMajorTickSpace(10);
//        heartRateGauge.setMinorTickSpace(5);
//
//        double fhr = UserPreferences.INSTANCE.getMaxHR();
//
//        List<Section> heartRateZones = new ArrayList<>();
//        heartRateZones.add(new Section(MIN_HR, (int) (fhr * 0.68)));  // active recovery < 68%
//        heartRateZones.add(new Section((int) (fhr * 0.69), (int) (fhr * 0.83))); // Endurance 69 - 83%
//        heartRateZones.add(new Section((int) (fhr * 0.84), (int) (fhr * 0.94))); // Tempo 84 - 94%
//        heartRateZones.add(new Section((int) (fhr * 0.95), (int) (fhr * 1.05))); // Lactate Threshold 95-105%
//        heartRateZones.add(new Section((int) (fhr * 1.06), MAX_HR)); // VO2Max
//        heartRateGauge.setSections(heartRateZones);
//
//        // TODO retrieve colors from userpreferences
//        heartRateGauge.setSectionFill0(Paint.valueOf("BLUE"));
//        heartRateGauge.setSectionFill1(Paint.valueOf("GREEN"));
//        heartRateGauge.setSectionFill2(Paint.valueOf("ORANGE"));
//        heartRateGauge.setSectionFill3(Paint.valueOf("RED"));
//        heartRateGauge.setSectionFill4(Paint.valueOf("BROWN"));
//    }
//
//    private static void initPowerGauge() {
//        powerGauge = new Gauge();
//
//        powerGauge.setPrefSize(800, 800);
//        powerGauge.setTitle("Power");
//        powerGauge.setUnit("Watts");
//        powerGauge.setMinValue(MIN_POWER);
//
//        double ftp = UserPreferences.INSTANCE.getMaxPower();
//        MAX_POWER = ftp * 2;
//        powerGauge.setMaxValue(MAX_POWER);
//
//        List<Section> powerZones = new ArrayList<>();
//        powerZones.add(new Section(MIN_POWER, ftp * 0.55));
//        powerZones.add(new Section(ftp * 0.56, ftp * 0.75));
//        powerZones.add(new Section(ftp * 0.76, ftp * 0.90));
//        powerZones.add(new Section(ftp * 0.91, ftp * 1.05));
//        powerZones.add(new Section(ftp * 1.06, ftp * 1.2));
//        powerZones.add(new Section(ftp * 1.2, ftp * 1.5));
//        powerZones.add(new Section(ftp * 1.50, MAX_POWER));
//        powerGauge.setSections(powerZones);
//
//        // TODO retrieve colors from userpreferences
//        powerGauge.setSectionFill0(Paint.valueOf("BLUE"));
//        powerGauge.setSectionFill1(Paint.valueOf("GREEN"));
//        powerGauge.setSectionFill2(Paint.valueOf("ORANGE"));
//        powerGauge.setSectionFill3(Paint.valueOf("RED"));
//        powerGauge.setSectionFill4(Paint.valueOf("BROWN"));
//        powerGauge.setSectionFill5(Paint.valueOf("SADDLEBROWN"));
//        powerGauge.setSectionFill6(Paint.valueOf("DARKGRAY"));
//
//        powerGauge.setMajorTickSpace(50);
//        powerGauge.setMinorTickSpace(10);
//    }
//
//    @Override
//    public void callback(Messages message, Object o) {
//        switch (message) {
//            case SPEED:
//                Telemetry t = (Telemetry) o;
//                // recover last heart rate data
////                t.setHeartRate(heartRate);
////                t.setCadence(cadence);
//                updateGaugesValues(t);
//                break;
//
//            case TRAININGITEM:
//                current = (TrainingItem) o;
//                updateGaugesTrainingZones();
//                break;
//        }
//    }
//
//    /**
//     * Set training limits with a cyan area in the corresponding gauge
//     */
//    private void updateGaugesTrainingZones() {
//        System.out.println(current);
//        double powerLow = current.getPowerLow();
//        double powerHigh = current.getPowerHigh();
//        if (powerHigh < powerLow) powerHigh = MAX_POWER;
//        if (powerLow > powerHigh) powerLow = MIN_POWER;
//        double cadenceLow = current.getCadenceLow();
//        double cadenceHigh = current.getCadenceHigh();
//        if (cadenceHigh < cadenceLow) cadenceHigh = MAX_RPM;
//        if (cadenceLow > cadenceHigh) cadenceLow = MIN_RPM;
//        double hrLow = current.getHrLow();
//        double hrHigh = current.getHrHigh();
//        if (hrHigh < hrLow) hrHigh = MAX_HR;
//        if (hrLow > hrHigh) hrLow = MIN_HR;
//
//        List<Section> powerLimits = new ArrayList<>();
//        powerLimits.add(new Section(powerLow, powerHigh));
//        powerGauge.setAreas(powerLimits);
//        powerGauge.setAreaFill0(Paint.valueOf("CYAN"));
//
//        List<Section> cadenceLimits = new ArrayList<>();
//        cadenceLimits.add(new Section(cadenceLow, cadenceHigh));
//        cadenceGauge.setAreas(cadenceLimits);
//        cadenceGauge.setAreaFill0(Paint.valueOf("CYAN"));
//
//        List<Section> heartRateLimits = new ArrayList<>();
//        heartRateLimits.add(new Section(hrLow, hrHigh));
//        heartRateGauge.setAreas(heartRateLimits);
//        heartRateGauge.setAreaFill0(Paint.valueOf("CYAN"));
//    }
//
//    private void updateGaugesValues(Telemetry t) {
//        powerGauge.setValue(t.getPower());
//        heartRateGauge.setValue(t.getHeartRate());
//        cadenceGauge.setValue(t.getCadence());
//    }
//
//
//}
