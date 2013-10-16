package com.wattzap.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.Telemetry;
import com.wattzap.model.power.Power;

public class Odometer extends JPanel implements ChangeListener, ActionListener {
	private static final long serialVersionUID = -7939830514817673972L;
	private JLabel speedLabel;
	private JLabel distanceLabel;
	private JLabel elevationLabel;
	private JLabel slopeLabel;
	private JLabel cadenceLabel;
	private JLabel hrLabel;
	private JLabel powerLabel;
	private JLabel resistanceLabel;

	private static Logger logger = LogManager.getLogger("Odometer");
	
	Power power;

	public Odometer() {
		super();

		power = UserPreferences.INSTANCE.getPowerProfile();
		setBackground(Color.DARK_GRAY);

		int style1 = Font.CENTER_BASELINE;
		Font font1 = new Font("Arial", style1, 13);

		int style = Font.BOLD | Font.ITALIC;
		Font font = new Font("Arial", style, 30);
		MigLayout layout = new MigLayout("fillx", "rel[grow,fill]", "[]30px[]");
		this.setLayout(layout);

		JLabel speedText = new JLabel();
		speedText.setFont(font1);
		speedText.setText("Speed (km/h)");

		speedText.setForeground(Color.GREEN);
		add(speedText);

		JLabel distText = new JLabel();
		distText.setFont(font1);
		distText.setText("Distance (km)");
		distText.setForeground(Color.GREEN);
		add(distText);

		JLabel cadText = new JLabel();
		cadText.setFont(font1);
		cadText.setText("Cadence");
		cadText.setForeground(Color.GREEN);
		add(cadText);

		JLabel slopeText = new JLabel();
		slopeText.setFont(font1);
		slopeText.setText("Slope %");
		slopeText.setForeground(Color.GREEN);
		add(slopeText);

		JLabel hrText = new JLabel();
		hrText.setFont(font1);
		hrText.setText("Heart Rate");
		hrText.setForeground(Color.GREEN);
		add(hrText);

		JLabel pwrText = new JLabel();
		pwrText.setFont(font1);
		pwrText.setText("Power");
		pwrText.setForeground(Color.GREEN);
		add(pwrText);

		JLabel levelText = new JLabel();
		levelText.setFont(font1);
		levelText.setText("Altitude");
		levelText.setForeground(Color.GREEN);
		add(levelText);

		JLabel resistanceText = new JLabel();
		resistanceText.setFont(font1);
		resistanceText.setText("Resistance");
		resistanceText.setForeground(Color.GREEN);
		add(resistanceText, "Wrap");

		speedLabel = new JLabel();
		speedLabel.setFont(font);
		speedLabel.setText("0.0");
		speedLabel.setForeground(Color.WHITE);
		add(speedLabel);

		distanceLabel = new JLabel();
		distanceLabel.setFont(font);
		distanceLabel.setText("0.0");
		distanceLabel.setForeground(Color.WHITE);
		add(distanceLabel);

		cadenceLabel = new JLabel();
		cadenceLabel.setFont(font);
		cadenceLabel.setText("0");
		cadenceLabel.setForeground(Color.WHITE);

		add(cadenceLabel);

		slopeLabel = new JLabel();
		slopeLabel.setFont(font);
		slopeLabel.setText("0.0");
		slopeLabel.setForeground(Color.WHITE);
		add(slopeLabel);

		hrLabel = new JLabel();
		hrLabel.setFont(font);
		hrLabel.setText("0");
		hrLabel.setForeground(Color.WHITE);
		add(hrLabel);

		powerLabel = new JLabel();
		powerLabel.setFont(font);
		powerLabel.setText("0");
		powerLabel.setForeground(Color.WHITE);
		add(powerLabel);

		elevationLabel = new JLabel();
		elevationLabel.setFont(font);
		elevationLabel.setForeground(Color.WHITE);
		elevationLabel.setText("0");
		add(elevationLabel);

		resistanceLabel = new JLabel();
		resistanceLabel.setFont(font);
		resistanceLabel.setForeground(Color.WHITE);
		add(resistanceLabel);
		resistanceLabel.setText("" + UserPreferences.INSTANCE.getResistance());
		
		// code to see if we are registered
		if (!UserPreferences.INSTANCE.isRegistered() && (UserPreferences.INSTANCE.getEvalTime()) <= 0) {
			logger.info("Out of time " + UserPreferences.INSTANCE.getEvalTime());
			UserPreferences.INSTANCE.shutDown();
			System.exit(0);
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		Telemetry t = (Telemetry) e.getSource();

		cadenceLabel.setText(Integer.toString(t.getCadence()));
		hrLabel.setText(Integer.toString(t.getHeartRate()));
		elevationLabel.setText(String.format("%.0f", t.getElevation()));
		slopeLabel.setText(String.format("%.1f", t.getGradient()));
		speedLabel.setText(String.format("%.1f", t.getSpeed()));
		powerLabel.setText("" + t.getPower());
		distanceLabel.setText(String.format("%.3f", t.getDistance()));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		logger.info("command");
		
		
	}
}
