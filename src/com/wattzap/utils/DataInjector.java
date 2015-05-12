package com.wattzap.utils;

import com.wattzap.controller.MessageBus;
import com.wattzap.controller.Messages;
import com.wattzap.model.dto.Telemetry;

import javax.swing.*;
import java.awt.*;

/**
 * Created by nicolas on 12/05/2015.
 */
public class DataInjector extends JFrame {
    private JLabel labelCadence = new JLabel("Cadence");
    private JTextField cadence = new JTextField(20);
    private JLabel labelPower = new JLabel("Power");
    private JTextField power = new JTextField(20);
    private JLabel labelHeartRate = new JLabel("Heart rate");
    private JTextField heartRate = new JTextField(20);
    private JButton sendButton = new JButton("Send Telemetry");

    public DataInjector() {
        JPanel panel = new JPanel();
        this.getContentPane().add(panel);

        panel.setLayout(new FlowLayout());
        panel.add(labelCadence);
        panel.add(cadence);
        panel.add(labelPower);
        panel.add(power);
        panel.add(labelHeartRate);
        panel.add(heartRate);
        panel.add(sendButton);

        this.pack();
        this.setVisible(true);

        sendButton.addActionListener(e -> {
            Telemetry t = new Telemetry();
            t.setCadence(Integer.parseInt(cadence.getText()));
            t.setPower(Integer.parseInt(power.getText()));
            t.setHeartRate(Integer.parseInt(heartRate.getText()));
            MessageBus.INSTANCE.send(Messages.SPEED, t);
        });
    }

}
