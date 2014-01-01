package com.wattzap.view;

import java.awt.Color;
import java.awt.Font;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.wattzap.controller.MessageBus;
import com.wattzap.controller.MessageCallback;
import com.wattzap.controller.Messages;
import com.wattzap.model.RLVReader;
import com.wattzap.model.RouteReader;
import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.Telemetry;
import com.wattzap.model.dto.TrainingItem;
import com.wattzap.model.power.Power;

/* 
 * @author David George (c) Copyright 2013
 * @date 19 June 2013
 */
public class Odometer extends JPanel implements MessageCallback {
	private static final long serialVersionUID = -7939830514817673972L;
	private JLabel speedLabel;
	private JLabel distanceLabel;
	private JLabel elevationLabel;
	private JLabel slopeLabel;
	private JLabel cadenceLabel;
	private JLabel hrLabel;
	private JLabel powerLabel;
	private JLabel resistanceLabel;
	private JLabel slopeText;
	int type = RLVReader.SLOPE;
	TrainingItem current;
	private static Logger logger = LogManager.getLogger("Odometer");

	Power power;
	Color skyBlue = new Color(0, 154, 237);
	Color textColor = new Color(240, 244, 112);
	DateFormat timeFormat;
	long startTime = 0;

	public Odometer() {
		super();

		timeFormat = new SimpleDateFormat("HH:mm:ss");
		timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

		power = UserPreferences.INSTANCE.getPowerProfile();
		setBackground(Color.BLACK);

		int style1 = Font.CENTER_BASELINE;
		Font font1 = new Font("Arial", style1, 13);

		int style = Font.BOLD | Font.ITALIC;
		Font font = new Font("Arial", style, 30);
		MigLayout layout = new MigLayout("fillx", "[center]", "[][shrink 0]");
		this.setLayout(layout);

		JLabel speedText = new JLabel();
		speedText.setFont(font1);
		speedText.setText(UserPreferences.INSTANCE.messages.getString("speed")
				+ " (km/h)");

		speedText.setForeground(textColor);
		add(speedText);

		JLabel distText = new JLabel();
		distText.setFont(font1);
		distText.setText("Distance (km)");
		distText.setForeground(textColor);
		add(distText);

		JLabel cadText = new JLabel();
		cadText.setFont(font1);
		cadText.setText("Cadence");
		cadText.setForeground(textColor);
		add(cadText);

		JLabel hrText = new JLabel();
		hrText.setFont(font1);
		hrText.setText("Heart Rate");
		hrText.setForeground(textColor);
		add(hrText);

		JLabel pwrText = new JLabel();
		pwrText.setFont(font1);
		pwrText.setText("Power");
		pwrText.setForeground(textColor);
		add(pwrText);

		slopeText = new JLabel();
		slopeText.setFont(font1);
		slopeText.setText("Slope %");
		slopeText.setForeground(textColor);
		add(slopeText);

		JLabel levelText = new JLabel();
		levelText.setFont(font1);
		levelText.setText("Altitude");
		levelText.setForeground(textColor);
		add(levelText);

		JLabel resistanceText = new JLabel();
		resistanceText.setFont(font1);
		resistanceText.setText("Stopwatch");
		resistanceText.setForeground(textColor);
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

		slopeLabel = new JLabel();
		slopeLabel.setFont(font);
		slopeLabel.setText("0.0");
		slopeLabel.setForeground(Color.WHITE);
		add(slopeLabel);

		elevationLabel = new JLabel();
		elevationLabel.setFont(font);
		elevationLabel.setForeground(Color.WHITE);
		elevationLabel.setText("0");
		add(elevationLabel);

		resistanceLabel = new JLabel();
		resistanceLabel.setFont(font);
		resistanceLabel.setForeground(Color.WHITE);
		add(resistanceLabel);
		resistanceLabel.setText("00:00:00");

		// code to see if we are registered
		if (!UserPreferences.INSTANCE.isRegistered()
				&& (UserPreferences.INSTANCE.getEvalTime()) <= 0) {
			logger.info("Out of time " + UserPreferences.INSTANCE.getEvalTime());
			UserPreferences.INSTANCE.shutDown();
			System.exit(0);
		}

		MessageBus.INSTANCE.register(Messages.TRAININGITEM, this);
		MessageBus.INSTANCE.register(Messages.SPEEDCADENCE, this);
		MessageBus.INSTANCE.register(Messages.GPXLOAD, this);
		MessageBus.INSTANCE.register(Messages.CLOSE, this);
	}

	@Override
	public void callback(Messages message, Object o) {
		switch (message) {
		case SPEEDCADENCE:
			Telemetry t = (Telemetry) o;

			if (startTime == 0) {
				startTime = t.getTime();
			}

			elevationLabel.setText(String.format("%.0f", t.getElevation()));
			slopeLabel.setText(String.format("%.1f", t.getGradient()));
			speedLabel.setText(String.format("%.1f", t.getSpeed()));
			if (current != null) {
				int i = current.isPowerInRange(t.getPower());
				if (i < 0) {
					powerLabel.setForeground(skyBlue);
					powerLabel.setText("" + t.getPower());
				} else if (i > 0) {
					powerLabel.setForeground(Color.RED);
					powerLabel.setText("" + t.getPower());
				} else {
					powerLabel.setForeground(Color.WHITE);
					powerLabel.setText("" + t.getPower());

				}
				i = current.isCadenceInRange(t.getCadence());
				if (i < 0) {
					cadenceLabel.setForeground(skyBlue);
					cadenceLabel.setText("" + t.getCadence());
				} else if (i > 0) {
					cadenceLabel.setForeground(Color.RED);
					cadenceLabel.setText("" + t.getCadence());
				} else {
					cadenceLabel.setForeground(Color.WHITE);
					cadenceLabel.setText("" + t.getCadence());
				}
				i = current.isHRInRange(t.getCadence());
				if (i < 0) {
					hrLabel.setForeground(skyBlue);
					hrLabel.setText("" + t.getHeartRate());
				} else if (i > 0) {
					hrLabel.setForeground(Color.RED);
					hrLabel.setText("" + t.getHeartRate());
				} else {
					hrLabel.setForeground(Color.WHITE);
					hrLabel.setText("" + t.getHeartRate());
				}
			} else {
				powerLabel.setText("" + t.getPower());
				cadenceLabel.setText("" + t.getCadence());
				hrLabel.setText(Integer.toString(t.getHeartRate()));
			}
			distanceLabel.setText(String.format("%.3f", t.getDistance()));
			resistanceLabel.setText(timeFormat.format(new Date(t.getTime()
					- startTime)));
			break;

		case TRAININGITEM:
			current = (TrainingItem) o;
			System.out.println("odo " + current);
			break;
		case GPXLOAD:
			RouteReader routeData = (RouteReader) o;
			type = routeData.routeType();
			switch (type) {
			case RLVReader.POWER:
				slopeText.setText("Target Power");
				break;
			case RLVReader.SLOPE:
				slopeText.setText("Slope %");
				break;
			}
			double totalDistance = routeData.getDistanceMeters();
			break;
		case CLOSE:
			startTime = 0;
			speedLabel.setText("0.0");
			powerLabel.setText("0");
			cadenceLabel.setText("0");
			hrLabel.setText("0");
			distanceLabel.setText("0.0");
			break;
		}
	}
}
