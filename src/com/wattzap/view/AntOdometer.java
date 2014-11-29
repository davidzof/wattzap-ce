/* This file is part of Wattzap Community Edition.
 *
 * Wattzap Community Edtion is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Wattzap Community Edition is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Wattzap.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wattzap.view;

import java.awt.Color;
import java.awt.Font;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.wattzap.controller.MessageBus;
import com.wattzap.controller.MessageCallback;
import com.wattzap.controller.Messages;
import com.wattzap.model.RouteReader;
import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.Telemetry;
import com.wattzap.model.dto.TrainingItem;

/**
 * Used when ANT+ Stick is connected and configured
 * 
 * @author David George (c) Copyright 2014
 * @date 5 February 2014
 */
public class AntOdometer extends JPanel implements MessageCallback {
	private static final long serialVersionUID = -7939830514817673972L;
	private JLabel speedText;
	private JLabel distText;
	private JLabel slopeText;
	private JLabel levelText;

	private JLabel speedLabel;
	private JLabel distanceLabel;
	private JLabel elevationLabel;
	private JLabel slopeLabel;
	private JLabel cadenceLabel;
	private JLabel hrLabel;
	private JLabel powerLabel;
	private JLabel chronoLabel;

	private int type = RouteReader.SLOPE;
	private TrainingItem current;
	private static Logger logger = LogManager.getLogger("Odometer");

	private final Color skyBlue = new Color(0, 154, 237);
	private final Color textColor = new Color(240, 244, 112);
	private DateFormat timeFormat;
	private long startTime = 0;
	private double totalDistance = 0;

	private final UserPreferences userPrefs = UserPreferences.INSTANCE;

	public AntOdometer() {
		super();

		timeFormat = new SimpleDateFormat("HH:mm:ss");
		timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		setBackground(Color.BLACK);

		int style1 = Font.CENTER_BASELINE;
		Font font1 = new Font("Arial", style1, 13);

		int style = Font.BOLD | Font.ITALIC;
		Font font = new Font("Arial", style, 30);
		MigLayout layout = new MigLayout("fillx", "[center]", "[][shrink 0]");
		this.setLayout(layout);

		speedText = new JLabel();
		speedText.setFont(font1);
		speedText.setForeground(textColor);
		add(speedText);

		distText = new JLabel();
		distText.setFont(font1);
		distText.setForeground(textColor);
		add(distText);

		JLabel pwrText = new JLabel();
		pwrText.setFont(font1);
		pwrText.setText(userPrefs.messages.getString("power"));
		pwrText.setForeground(textColor);
		add(pwrText);

		JLabel hrText = new JLabel();
		hrText.setFont(font1);
		hrText.setText(userPrefs.messages.getString("heartrate"));
		hrText.setForeground(textColor);
		add(hrText);

		JLabel cadText = new JLabel();
		cadText.setFont(font1);
		cadText.setText(userPrefs.messages.getString("cadence"));
		cadText.setForeground(textColor);
		add(cadText);

		slopeText = new JLabel();
		slopeText.setFont(font1);
		slopeText.setText(userPrefs.messages.getString("slope") + " %");
		slopeText.setForeground(textColor);
		add(slopeText);

		levelText = new JLabel();
		levelText.setFont(font1);
		levelText.setText(userPrefs.messages.getString("altitude"));
		levelText.setForeground(textColor);
		add(levelText);

		JLabel resistanceText = new JLabel();
		resistanceText.setFont(font1);
		resistanceText.setText(userPrefs.messages.getString("stopwatch"));
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

		powerLabel = new JLabel();
		powerLabel.setFont(font);
		powerLabel.setText("0");
		powerLabel.setForeground(Color.WHITE);
		add(powerLabel);

		hrLabel = new JLabel();
		hrLabel.setFont(font);
		hrLabel.setText("0");
		hrLabel.setForeground(Color.WHITE);
		add(hrLabel);

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

		elevationLabel = new JLabel();
		elevationLabel.setFont(font);
		elevationLabel.setForeground(Color.WHITE);
		elevationLabel.setText("0");
		add(elevationLabel);

		chronoLabel = new JLabel();
		chronoLabel.setFont(font);
		chronoLabel.setForeground(Color.WHITE);
		add(chronoLabel);
		chronoLabel.setText("00:00:00");

		initLabels(userPrefs.isMetric());
		MessageBus.INSTANCE.register(Messages.TRAININGITEM, this);
		MessageBus.INSTANCE.register(Messages.SPEED, this);
		MessageBus.INSTANCE.register(Messages.CADENCE, this);
		MessageBus.INSTANCE.register(Messages.HEARTRATE, this);
		MessageBus.INSTANCE.register(Messages.GPXLOAD, this);
		MessageBus.INSTANCE.register(Messages.START, this);
	}

	private void initLabels(boolean metric) {
		if (metric) {
			speedText
					.setText(userPrefs.messages.getString("speed") + " (km/h)");
			distText.setText(userPrefs.messages.getString("distance") + " (km)");
		} else {
			speedText.setText(userPrefs.messages.getString("speed") + " (mph)");
			distText.setText(userPrefs.messages.getString("distance")
					+ " (miles)");

		}
	}

	@Override
	public void callback(Messages message, Object o) {

		switch (message) {
		case SPEED:
			Telemetry t = (Telemetry) o;

			if (startTime == 0) {
				startTime = t.getTime();
			}

			boolean metric = userPrefs.isMetric();
			if (userPrefs.isMetric()) {
				speedLabel.setText(String.format("%.1f", t.getSpeedKMH()));
				distanceLabel.setText(String.format("%.3f", t.getDistanceKM()));
			} else {
				speedLabel.setText(String.format("%.1f", t.getSpeedMPH()));
				distanceLabel.setText(String.format("%.3f",
						t.getDistanceMiles()));
			}

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

			} else {
				powerLabel.setText("" + t.getPower());
			}

			chronoLabel.setText(timeFormat.format(new Date(t.getTime()
					- startTime)));
			switch (type) {
			case RouteReader.POWER:
				if (userPrefs.isMetric()) {
					// remaining distance
					elevationLabel.setText(String.format("%.3f",
							(totalDistance - t.getDistanceMeters()) / 1000));
				} else {
					// FIXME isn't totalDistance in meters? Check this
					elevationLabel.setText(String.format("%.3f",
							(totalDistance - t.getDistanceMiles()) / 1000));

				}
				break;
			case RouteReader.SLOPE:
				elevationLabel.setText(String.format("%.0f", t.getElevation()));
				slopeLabel.setText(String.format("%.1f", t.getGradient()));
				break;
			}

			break;

		case CADENCE:
			int cadence = (Integer) o;
			if (current != null) {

				int i = current.isCadenceInRange(cadence);
				if (i < 0) {
					cadenceLabel.setForeground(skyBlue);
					cadenceLabel.setText("" + cadence);
				} else if (i > 0) {
					cadenceLabel.setForeground(Color.RED);
					cadenceLabel.setText("" + cadence);
				} else {
					cadenceLabel.setForeground(Color.WHITE);
					cadenceLabel.setText("" + cadence);
				}
			} else {
				cadenceLabel.setText("" + cadence);
			}

			break;

		case HEARTRATE:
			int heartRate = (Integer) o;
			if (current != null) {
				int i = current.isHRInRange(heartRate);
				if (i < 0) {
					hrLabel.setForeground(skyBlue);
					hrLabel.setText("" + heartRate);
				} else if (i > 0) {
					hrLabel.setForeground(Color.RED);
					hrLabel.setText("" + heartRate);
				} else {
					hrLabel.setForeground(Color.WHITE);
					hrLabel.setText("" + heartRate);
				}
			} else {
				hrLabel.setText(Integer.toString(heartRate));
			}
			break;
		case TRAININGITEM:
			current = (TrainingItem) o;
			if (current != null) {
				slopeLabel.setText("" + current.getPower());
			}
			break;
		case GPXLOAD:
			current = null;
			// code to see if we are registered
			if (!userPrefs.isRegistered() && (userPrefs.getEvalTime()) <= 0) {
				logger.info("Out of time " + userPrefs.getEvalTime());
				JOptionPane.showMessageDialog(this,
						userPrefs.messages.getString("trial_expired"),
						userPrefs.messages.getString("warning"),
						JOptionPane.WARNING_MESSAGE);
				userPrefs.shutDown();
				System.exit(0);
			}

			RouteReader routeData = (RouteReader) o;
			type = routeData.routeType();
			switch (type) {
			case RouteReader.POWER:
				slopeText.setText(userPrefs.messages.getString("target_power"));
				levelText
						.setText(userPrefs.messages.getString("distance_left"));
				break;
			case RouteReader.SLOPE:
				slopeText.setText(userPrefs.messages.getString("slope") + " %");
				levelText.setText(userPrefs.messages.getString("altitude"));

				break;
			}

			totalDistance = routeData.getDistanceMeters();

			startTime = 0;
			speedLabel.setText("0.0");
			powerLabel.setText("0");
			cadenceLabel.setText("0");
			hrLabel.setText("0");
			distanceLabel.setText("0.0");
			break;
		case START:
			powerLabel.setForeground(Color.WHITE);
			initLabels(userPrefs.isMetric());
		}
	}
}
