package com.wattzap.view.training;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.miginfocom.swing.MigLayout;

import com.wattzap.Main;
import com.wattzap.model.dto.Telemetry;
import com.wattzap.utils.RollingTime;

public class TrainingAnalysis extends JFrame implements ActionListener {
	private static final long serialVersionUID = -7939830514817673972L;
	private JLabel fiveSecondPower;
	private JLabel oneMinutePower;
	private JLabel fiveMinutePower;
	private JLabel twentyMinutePower;
	private JLabel funcThresholdPower;
	// Heart Rate
	private JLabel maxHeartRate;
	private JLabel aveHeartRate;
	private JLabel minHeartRate;

	private JLabel distance;
	private JLabel totalPower;

	SimpleDateFormat df;
	DecimalFormat decimalFormat = new DecimalFormat("#.##");

	private JLabel time;

	TrainingDisplay tData;
	
	private static Logger logger = LogManager.getLogger(TrainingAnalysis.class.getName());

	public TrainingAnalysis(TrainingDisplay tData) {
		super();

		df = new SimpleDateFormat("H'h' m'm' s's'");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));

		MigLayout layout = new MigLayout();
		this.setLayout(layout);
		this.tData = tData;
		setBackground(Color.LIGHT_GRAY);

		int style1 = Font.CENTER_BASELINE;
		Font font1 = new Font("Arial", style1, 13);

		JLabel fiveSecondPowerLabel = new JLabel();
		fiveSecondPowerLabel.setFont(font1);
		fiveSecondPowerLabel.setText("5 Second Power");
		add(fiveSecondPowerLabel);

		fiveSecondPower = new JLabel();
		fiveSecondPower.setFont(font1);
		add(fiveSecondPower, "wrap");

		JLabel oneMinutePowerLabel = new JLabel();
		oneMinutePowerLabel.setFont(font1);
		oneMinutePowerLabel.setText("1 Minute Power");
		add(oneMinutePowerLabel);

		oneMinutePower = new JLabel();
		oneMinutePower.setFont(font1);
		add(oneMinutePower, "wrap");

		JLabel fiveMinutePowerLabel = new JLabel();
		fiveMinutePowerLabel.setFont(font1);
		fiveMinutePowerLabel.setText("Five Minute Power");
		add(fiveMinutePowerLabel);

		fiveMinutePower = new JLabel();
		fiveMinutePower.setFont(font1);
		add(fiveMinutePower, "wrap");

		JLabel twentyMinutePowerLabel = new JLabel();
		twentyMinutePowerLabel.setFont(font1);
		twentyMinutePowerLabel.setText("Twenty Minute Power");
		add(twentyMinutePowerLabel);

		twentyMinutePower = new JLabel();
		twentyMinutePower.setFont(font1);
		add(twentyMinutePower, "wrap");

		JLabel maxHeartRateLabel = new JLabel();
		maxHeartRateLabel.setFont(font1);
		maxHeartRateLabel.setText("Max HeartRate");
		add(maxHeartRateLabel);

		maxHeartRate = new JLabel();
		maxHeartRate.setFont(font1);
		add(maxHeartRate, "wrap");

		JLabel timeLabel = new JLabel();
		timeLabel.setFont(font1);
		timeLabel.setText("Time");
		add(timeLabel);

		time = new JLabel();
		time.setFont(font1);
		add(time, "wrap");

		JLabel distanceLabel = new JLabel();
		distanceLabel.setFont(font1);
		distanceLabel.setText("Distance");
		add(distanceLabel);

		distance = new JLabel();
		distance.setFont(font1);
		add(distance, "wrap");

		JLabel powerLabel = new JLabel();
		powerLabel.setFont(font1);
		powerLabel.setText("Average Power");
		add(powerLabel);

		totalPower = new JLabel();
		totalPower.setFont(font1);
		add(totalPower, "wrap");

		Dimension d = new Dimension(400, 300);
		this.setSize(d);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ArrayList<Telemetry> data = tData.getData();
		if (data == null || data.size() == 0) {
			logger.info("No training data to analyze");
			return;
		}
		Telemetry firstPoint = data.get(0);
		Telemetry lastPoint = data.get(data.size() - 1);
		long len = (lastPoint.getTime() - firstPoint.getTime());
		time.setText(df.format(len) + " ");
		len = len / 1000; // convert to seconds

		distance.setText(decimalFormat.format(lastPoint.getDistance()) + " km");
		int maxHR = 0;
		int aveHR = 0;
		int minHR = 220;
		double tPower = 0;

		int power = 0;
		RollingTime rollingTime = new RollingTime(5);
		// five second power
		Telemetry last = null;
		for (Telemetry t : data) {
			rollingTime.add(t.getPower(), t.getTime() / 1000);
			int avePwr = rollingTime.getAverage();
			if (avePwr > power) {
				power = avePwr;
			}

			if (t.getHeartRate() > maxHR) {
				maxHR = t.getHeartRate();
			}
			if (t.getHeartRate() < minHR) {
				minHR = t.getHeartRate();
			}
			if (last != null) {
				
				tPower += t.getPower() * (t.getTime() - last.getTime());
				System.out.println("delta " + (t.getTime() - last.getTime()) + " power " + t.getPower() + " tPower " + tPower);
			}
			last = t;
		}
		fiveSecondPower.setText(power + " Watts");
		maxHeartRate.setText(maxHR + " bpm");
		

		System.out.println(" total watts " + tPower / (3600000));
		totalPower.setText(tPower / (len * 1000) + " Watts");

		if (len > 60) {
			// 1 minute second power
			power = 0;
			rollingTime = new RollingTime(60);
			for (Telemetry t : data) {
				rollingTime.add(t.getPower(), t.getTime() / 1000);
				int avePwr = rollingTime.getAverage();
				if (avePwr > power) {
					power = avePwr;
				}
			}
			oneMinutePower.setText(power + " Watts");
		}

		if (len > 300) {
			// 5 minute second power
			power = 0;
			rollingTime = new RollingTime(300);
			for (Telemetry t : data) {
				rollingTime.add(t.getPower(), t.getTime() / 1000);
				int avePwr = rollingTime.getAverage();
				if (avePwr > power) {
					power = avePwr;
				}
			}
			fiveMinutePower.setText(power + " Watts");
		}

		/*
		 * 
		 * Distance: 176.41 km Energy: 5193 kJ TSS: 349 (0.79) NP: 311 VI: 1.21
		 * Pw:HR 0.18% EF: 2.22 Gain: 4059 m Loss: - 3010 m Grade:0.6 % VAM:719
		 * W/Kg:3.8 Min Avg Max Power (Watts): 0 256 936 Speed (km/h): 0 31.3
		 * 83.5 Pace (min/km): 99:99 01:55 00:43 HR (bpm): 72 140 175 Cadence
		 * (rpm): 0 82 138 Elev (m): 678 1134 1996 Temp (C): <17 25 37
		 */
		/*
		 * 2 sec 874 W 5 sec 830 W 10 sec 692 W 12 sec 656 W 20 sec 621 W 30 sec
		 * 571 W 1:00 min 496 W 2:00 min 466 W 5:00 min 456 W 6:00 min 451 W
		 * 10:00 min 439 W 12:00 min 433 W 20:00 min 405 W 30:00 min 367 W 01:00
		 * h 334 W 01:30 h 316 W 02:00 h 303 W 03:00 h 278 W
		 */

		setVisible(true);

	}
}
