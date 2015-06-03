package com.wattzap.utils;

import com.wattzap.controller.MessageBus;
import com.wattzap.controller.Messages;
import com.wattzap.model.dto.Telemetry;
import com.wattzap.model.dto.TrainingItem;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Date;

/**
 * Created by nicolas on 12/05/2015.
 */
public class DataInjector extends JFrame {
    private JLabel labelTime = new JLabel("Time");
    private JTextField time = new JTextField(5);
    private JLabel labelCadence = new JLabel("Cadence");
    private JTextField cadence = new JTextField(5);
    private JLabel labelPower = new JLabel("Power");
    private JTextField power = new JTextField(5);
    private JLabel labelHeartRate = new JLabel("Heart rate");
    private JTextField heartRate = new JTextField(5);
    private JButton sendButton = new JButton("Send Telemetry");

    private JLabel labelCadenceZone = new JLabel("Cadence Zone");
    private JLabel labelPowerZone = new JLabel("Power Zone");
    private JLabel labelHRZone = new JLabel("HR Zone");
    private JTextField cadenceZone = new JTextField(10);
    private JTextField powerZone = new JTextField(10);
    private JTextField hrZone = new JTextField(10);
    private JButton sendTrainingButton = new JButton("Send Training zones");

    public DataInjector() {
        JPanel panel = new JPanel();
        this.getContentPane().add(panel);

        panel.setLayout(new MigLayout());
        panel.add(new JLabel("Telemetry:"), "wrap");
        panel.add(labelTime);
        time.setText("0");
        panel.add(time, "wrap");
        panel.add(labelCadence);
        cadence.setText("89");
        panel.add(cadence);
        panel.add(labelPower);
        power.setText("234");
        panel.add(power);
        panel.add(labelHeartRate);
        heartRate.setText("123");
        panel.add(heartRate);
        panel.add(sendButton, "wrap");

        panel.add(new JLabel("Training zones:"), "wrap");
        panel.add(labelCadenceZone);
        panel.add(cadenceZone);
        panel.add(labelPowerZone);
        powerZone.setText("3");
        panel.add(powerZone);
        panel.add(labelHRZone);
        hrZone.setText("4");
        panel.add(hrZone);
        panel.add(sendTrainingButton);

        this.pack();
        this.setVisible(true);

        sendButton.addActionListener(e -> {
            Telemetry t = new Telemetry();
            if (!("".equals(time.getText())))
                t.setTime(new Date().getTime() + Integer.parseInt(time.getText().trim())*1000);
            if (!("".equals(cadence.getText())))
                t.setCadence(Integer.parseInt(cadence.getText().trim()));
            if (!("".equals(power.getText())))
                t.setPower(Integer.parseInt(power.getText().trim()));
            if (!("".equals(heartRate.getText())))
                t.setHeartRate(Integer.parseInt(heartRate.getText().trim()));
            MessageBus.INSTANCE.send(Messages.SPEED, t);
        });

        sendTrainingButton.addActionListener(e -> {
            TrainingItem ti =  new TrainingItem();
            if (!("".equals(cadenceZone.getText())))
                ti.setCadence(cadenceZone.getText().trim());
            if (!("".equals(powerZone.getText())))
                ti.setPower(powerZone.getText().trim());
            if (!("".equals(hrZone.getText())))
                ti.setHr(hrZone.getText().trim());
            MessageBus.INSTANCE.send(Messages.TRAININGITEM, ti);
        });
    }

}
