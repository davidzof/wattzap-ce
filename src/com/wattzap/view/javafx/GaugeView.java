package com.wattzap.view.javafx;

import com.wattzap.controller.MessageBus;
import com.wattzap.controller.MessageCallback;
import com.wattzap.controller.Messages;
import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.Telemetry;
import eu.hansolo.enzo.common.Section;
import eu.hansolo.enzo.gauge.Gauge;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nicolas on 08/05/2015.
 */
public class GaugeView implements MessageCallback {


    private static Gauge powerGauge;
    private long time;
    private long startTime;

    public GaugeView() {
        // register message bus events
        MessageBus.INSTANCE.register(Messages.SPEED, this);
        MessageBus.INSTANCE.register(Messages.CADENCE, this);
        MessageBus.INSTANCE.register(Messages.HEARTRATE, this);
        MessageBus.INSTANCE.register(Messages.START, this);
        MessageBus.INSTANCE.register(Messages.STARTPOS, this);
        MessageBus.INSTANCE.register(Messages.STOP, this);
        MessageBus.INSTANCE.register(Messages.TRAINING, this);
        MessageBus.INSTANCE.register(Messages.CLOSE, this);
    }

    public void show() {
        SwingUtilities.invokeLater(() -> GaugeView.initAndShowGUI());
    }

    private static void initAndShowGUI() {
        // This method is invoked on the EDT thread
        JFrame frame = new JFrame("Swing and JavaFX");
        final JFXPanel fxPanel = new JFXPanel();
        frame.add(fxPanel);
        frame.setSize(800, 600);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Platform.runLater(() -> initFX(fxPanel));


    }

    private static void initFX(JFXPanel fxPanel) {
        // This method is invoked on the JavaFX thread
        Scene scene = createScene();
        fxPanel.setScene(scene);

        // set training limits

        List<Section> trainingLimits = new ArrayList<>();
        trainingLimits.add(new Section(210 * 0.91, 210 * 1.05));
        powerGauge.setAreas(trainingLimits);
        powerGauge.setAreaFill0(Paint.valueOf("CYAN"));

        powerGauge.setValue(198);
    }

    private static Scene createScene() {

        initPowerGauge();
        Scene scene = new Scene(new StackPane(powerGauge), 400, 400);

        return (scene);
    }

    private static void initPowerGauge() {
        powerGauge = new Gauge();
        powerGauge.setTitle("Power");
        powerGauge.setUnit("Watts");
        powerGauge.setMinValue(0);

        double ftp = UserPreferences.INSTANCE.getMaxPower();
        powerGauge.setMaxValue(ftp * 2);

        List<Section> powerZones = new ArrayList<>();
        powerZones.add(new Section(0, ftp * 0.55));
        powerZones.add(new Section(ftp * 0.56, ftp * 0.75));
        powerZones.add(new Section(ftp * 0.76, ftp * 0.90));
        powerZones.add(new Section(ftp * 0.91, ftp * 1.05));
        powerZones.add(new Section(ftp * 1.06, ftp * 1.2));
        powerZones.add(new Section(ftp * 1.2, ftp * 1.5));
        powerZones.add(new Section(ftp * 1.50, ftp * 2));
        powerGauge.setSections(powerZones);

        // TODO retrieve colors from userpreferences
        powerGauge.setSectionFill0(Paint.valueOf("BLUE"));
        powerGauge.setSectionFill1(Paint.valueOf("GREEN"));
        powerGauge.setSectionFill2(Paint.valueOf("ORANGE"));
        powerGauge.setSectionFill3(Paint.valueOf("RED"));
        powerGauge.setSectionFill4(Paint.valueOf("BROWN"));
        powerGauge.setSectionFill5(Paint.valueOf("SADDLEBROWN"));
        powerGauge.setSectionFill6(Paint.valueOf("DARKGRAY"));

        powerGauge.setMajorTickSpace(50);
        powerGauge.setMinorTickSpace(10);
    }

    @Override
    public void callback(Messages message, Object o) {
        switch (message) {
            case SPEED:

                Telemetry t = (Telemetry) o;
                if (time == t.getTime()) {
                    // no change
                    return;
                }
                time = t.getTime();
                if (startTime == 0) {
                    startTime = time; // start time
                }
                powerGauge.setValue(t.getPower());
                break;
        }
    }



}
