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
package com.wattzap.view.graphs;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.WorkoutData;

/**
 * (c) 2014 Wattzap.com
 * 
 * Workout information panel. Displays all the analyzed data from the workout.
 * 
 * @author David George
 * @date 22 September 2014
 */
public class InfoPanel extends JPanel {

	private final static int style1 = Font.CENTER_BASELINE;
	private final static Font font1 = new Font("Arial", style1, 11);
	private final static UserPreferences userPrefs = UserPreferences.INSTANCE;
	private final static String COLGAP = "gapright 10";

	// Cadence
	private JLabel maxCadence;
	private JLabel aveCadence;

	// Heart Rate
	private JLabel ftHeartRate;
	private JLabel maxHeartRate;
	private JLabel aveHeartRate;

	private JLabel distance;

	// Power
	private JLabel totalPower;
	private JLabel avePower;
	private JLabel maxPower;
	private JLabel qPower;
	private JLabel ftPower;
	private JLabel ft1Power;
	private JLabel ft20Power;

	private JLabel fiveSecondPower;
	private JLabel fiveSecondWKG;

	private JLabel oneMinutePower;
	private JLabel oneMinuteWKG;

	private JLabel fiveMinutePower;
	private JLabel fiveMinuteWKG;

	private JLabel twentyMinutePower;
	private JLabel twentyMinuteWKG;

	private JLabel load;
	private JLabel stress;

	private JLabel time;

	public InfoPanel() {
		MigLayout layout = new MigLayout();
		this.setLayout(layout);
		setBackground(Color.LIGHT_GRAY);

		// time
		JLabel timeLabel = new JLabel();
		timeLabel.setFont(font1);
		timeLabel.setText("Time");
		add(timeLabel);

		time = new JLabel();
		time.setFont(font1);
		time.setForeground(Color.DARK_GRAY);
		add(time, COLGAP);

		// distance
		JLabel distanceLabel = new JLabel();
		distanceLabel.setFont(font1);
		distanceLabel.setText(userPrefs.getString("distance"));
		add(distanceLabel);

		distance = new JLabel();
		distance.setFont(font1);
		distance.setForeground(Color.DARK_GRAY);
		add(distance, "wrap");

		// Max Heart Rate
		JLabel maxHeartRateLabel = new JLabel();
		maxHeartRateLabel.setFont(font1);
		maxHeartRateLabel.setText(userPrefs.getString("maxhr"));
		add(maxHeartRateLabel);

		maxHeartRate = new JLabel();
		maxHeartRate.setFont(font1);
		maxHeartRate.setForeground(Color.DARK_GRAY);
		add(maxHeartRate, COLGAP);

		// Average Heartrate
		JLabel aveHeartRateLabel = new JLabel();
		aveHeartRateLabel.setFont(font1);
		aveHeartRateLabel.setText(userPrefs.getString("avehr"));
		add(aveHeartRateLabel);

		aveHeartRate = new JLabel();
		aveHeartRate.setFont(font1);
		aveHeartRate.setForeground(Color.DARK_GRAY);
		add(aveHeartRate, COLGAP);

		// Functional Threshold Heartrate
		JLabel fthrLabel = new JLabel();
		fthrLabel.setFont(font1);
		fthrLabel.setText(userPrefs.getString("fthr"));
		add(fthrLabel);

		ftHeartRate = new JLabel();
		ftHeartRate.setFont(font1);
		ftHeartRate.setForeground(Color.DARK_GRAY);
		add(ftHeartRate, COLGAP);

		// Average Cadence
		JLabel aveCadenceLabel = new JLabel();
		aveCadenceLabel.setFont(font1);
		aveCadenceLabel.setText(userPrefs.getString("avecad"));
		add(aveCadenceLabel);

		aveCadence = new JLabel();
		aveCadence.setFont(font1);
		aveCadence.setForeground(Color.DARK_GRAY);
		add(aveCadence, COLGAP);

		JLabel maxCadenceLabel = new JLabel();
		maxCadenceLabel.setFont(font1);
		maxCadenceLabel.setText(userPrefs.getString("maxcad"));
		add(maxCadenceLabel);

		maxCadence = new JLabel();
		maxCadence.setFont(font1);
		maxCadence.setForeground(Color.DARK_GRAY);
		add(maxCadence, "wrap");

		// Total Power
		JLabel powerLabel = new JLabel();
		powerLabel.setFont(font1);
		powerLabel.setText(userPrefs.getString("power"));
		add(powerLabel);

		totalPower = new JLabel();
		totalPower.setFont(font1);
		totalPower.setForeground(Color.DARK_GRAY);
		add(totalPower, COLGAP);

		// Average Power
		JLabel aveLabel = new JLabel();
		aveLabel.setFont(font1);
		aveLabel.setText(userPrefs.getString("avepow"));
		add(aveLabel);

		avePower = new JLabel();
		avePower.setFont(font1);
		avePower.setForeground(Color.DARK_GRAY);
		add(avePower, COLGAP);

		// Max Power
		JLabel maxLabel = new JLabel();
		maxLabel.setFont(font1);
		maxLabel.setText(userPrefs.getString("maxpow"));
		add(maxLabel);

		maxPower = new JLabel();
		maxPower.setFont(font1);
		maxPower.setForeground(Color.DARK_GRAY);
		add(maxPower, COLGAP);

		// Quadratic Power
		JLabel qLabel = new JLabel();
		qLabel.setFont(font1);
		qLabel.setText(userPrefs.getString("qpow"));
		add(qLabel);

		qPower = new JLabel();
		qPower.setFont(font1);
		qPower.setForeground(Color.DARK_GRAY);
		add(qPower, "wrap");

		// Current FTP
		JLabel ftpLabel = new JLabel();
		ftpLabel.setFont(font1);
		ftpLabel.setText(userPrefs.getString("cftp"));
		add(ftpLabel);

		ftPower = new JLabel();
		ftPower.setFont(font1);
		ftPower.setForeground(Color.DARK_GRAY);
		add(ftPower, COLGAP);

		JLabel ftp1Label = new JLabel();
		ftp1Label.setFont(font1);
		ftp1Label.setText(userPrefs.getString("1minftp"));
		add(ftp1Label);

		ft1Power = new JLabel();
		ft1Power.setFont(font1);
		ft1Power.setForeground(Color.DARK_GRAY);
		add(ft1Power, COLGAP);

		JLabel ftp2Label = new JLabel();
		ftp2Label.setFont(font1);
		ftp2Label.setText(userPrefs.getString("20minftp"));
		add(ftp2Label);

		ft20Power = new JLabel();
		ft20Power.setFont(font1);
		ft20Power.setForeground(Color.DARK_GRAY);
		add(ft20Power, COLGAP);

		// Load
		JLabel loadLabel = new JLabel();
		loadLabel.setFont(font1);
		loadLabel.setText(userPrefs.getString("load"));
		add(loadLabel);

		load = new JLabel();
		load.setFont(font1);
		load.setForeground(Color.DARK_GRAY);
		add(load, COLGAP);

		JLabel stressLabel = new JLabel();
		stressLabel.setFont(font1);
		stressLabel.setText(userPrefs.getString("stress"));
		add(stressLabel);

		stress = new JLabel();
		stress.setFont(font1);
		stress.setForeground(Color.DARK_GRAY);
		add(stress, COLGAP);

	}

	public void update(WorkoutData data) {
		if (data == null) {
			return;
		}

		maxHeartRate.setText(data.getMaxHR() + " bpm");
		aveHeartRate.setText(data.getAveHR() + " bpm");
		ftHeartRate.setText(data.getFtHR() + " bpm");

		aveCadence.setText(data.getAveCadence() + " rpm");
		maxCadence.setText(data.getMaxCadence() + " rpm");

		avePower.setText(data.getAvePower() + " Watts");
		maxPower.setText(data.getMaxPower() + " Watts");
		totalPower.setText(data.getTotalPower() + " Watts");
		qPower.setText(data.getQuadraticPower() + " Watts");
		ftPower.setText(UserPreferences.INSTANCE.getMaxPower() + " Watts");
		ft1Power.setText((int) (data.getOneMinutePwr()*0.75) + " Watts");
		ft20Power.setText((int) (data.getTwentyMinutePwr()*0.95) + " Watts");

		load.setText("" + (int)(data.getIntensity()*100));
		stress.setText("" + data.getStress());

		time.setText(data.getDateAsString());
		distance.setText(String.format("%.3f", data.getDistanceMeters() / 1000)
				+ " km");
	}

	private static final long serialVersionUID = 1L;
}
