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

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.Telemetry;
import com.wattzap.utils.RollingTime;

public class TrainingAnalysis extends JFrame implements ActionListener {
	private static final long serialVersionUID = -7939830514817673972L;
	private JLabel fiveSecondPower;
	private JLabel fiveSecondWKG;
	
	private JLabel oneMinutePower;
	private JLabel oneMinuteWKG;
	
	private JLabel fiveMinutePower;
	private JLabel fiveMinuteWKG;
	
	private JLabel twentyMinutePower;
	private JLabel twentyMinuteWKG;
	
	// Cadence
	// Heart Rate
	private JLabel maxHeartRate;
	private JLabel aveHeartRate;
	private JLabel minHeartRate;
	private JLabel fTHR;

	private JLabel distance;
	// Power
	private JLabel totalPower;
	private JLabel avePower;
	private JLabel maxPower;
	private JLabel qPower;
	private JLabel ftPower;
	
	SimpleDateFormat df;
	DecimalFormat decimalFormat = new DecimalFormat("#.##");

	private JLabel time;

	TrainingDisplay tData;
	
	private static Logger logger = LogManager.getLogger("Training Analysis");

	public TrainingAnalysis(TrainingDisplay tData) {
		super();
		
		setTitle("Analysis");
		ImageIcon img = new ImageIcon("icons/preferences.jpg");
		setIconImage(img.getImage());

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
		
		fiveSecondWKG = new JLabel();
		fiveSecondWKG.setFont(font1);
		add(fiveSecondPower);
		add(fiveSecondWKG, "wrap");

		JLabel oneMinutePowerLabel = new JLabel();
		oneMinutePowerLabel.setFont(font1);
		oneMinutePowerLabel.setText("1 Minute Power");
		add(oneMinutePowerLabel);

		oneMinutePower = new JLabel();
		oneMinutePower.setFont(font1);
		oneMinuteWKG = new JLabel();
		oneMinuteWKG.setFont(font1);
		add(oneMinutePower);
		add(oneMinuteWKG, "wrap");

		JLabel fiveMinutePowerLabel = new JLabel();
		fiveMinutePowerLabel.setFont(font1);
		fiveMinutePowerLabel.setText("Five Minute Power");
		add(fiveMinutePowerLabel);

		fiveMinutePower = new JLabel();
		fiveMinutePower.setFont(font1);
		fiveMinuteWKG = new JLabel();
		fiveMinuteWKG.setFont(font1);
		add(fiveMinutePower);
		add(fiveMinuteWKG, "wrap");

		JLabel twentyMinutePowerLabel = new JLabel();
		twentyMinutePowerLabel.setFont(font1);
		twentyMinutePowerLabel.setText("Twenty Minute Power");
		add(twentyMinutePowerLabel);

		twentyMinutePower = new JLabel();
		twentyMinutePower.setFont(font1);
		twentyMinuteWKG = new JLabel();
		twentyMinuteWKG.setFont(font1);
		add(twentyMinutePower);
		add(twentyMinuteWKG, "wrap");

		JLabel maxHeartRateLabel = new JLabel();
		maxHeartRateLabel.setFont(font1);
		maxHeartRateLabel.setText("Max HeartRate");
		add(maxHeartRateLabel);

		maxHeartRate = new JLabel();
		maxHeartRate.setFont(font1);
		add(maxHeartRate, "wrap");
		
		JLabel aveHeartRateLabel = new JLabel();
		aveHeartRateLabel.setFont(font1);
		aveHeartRateLabel.setText("Average HeartRate");
		add(aveHeartRateLabel);

		aveHeartRate = new JLabel();
		aveHeartRate.setFont(font1);
		add(aveHeartRate, "wrap");
		
		JLabel fTHRLabel = new JLabel();
		fTHRLabel.setFont(font1);
		fTHRLabel.setText("Functional Threshold HeartRate");
		add(fTHRLabel);

		fTHR = new JLabel();
		fTHR.setFont(font1);
		add(fTHR, "wrap");

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
		powerLabel.setText("Power");
		add(powerLabel);

		totalPower = new JLabel();
		totalPower.setFont(font1);
		add(totalPower, "wrap");

		JLabel aveLabel = new JLabel();
		aveLabel.setFont(font1);
		aveLabel.setText("Average Power");
		add(aveLabel);

		avePower = new JLabel();
		avePower.setFont(font1);
		add(avePower, "wrap");

		JLabel maxLabel = new JLabel();
		maxLabel.setFont(font1);
		maxLabel.setText("Maximum Power");
		add(maxLabel);

		maxPower = new JLabel();
		maxPower.setFont(font1);
		add(maxPower, "wrap");
		
		JLabel qLabel = new JLabel();
		qLabel.setFont(font1);
		qLabel.setText("Quadratic Power");
		add(qLabel);

		qPower = new JLabel();
		qPower.setFont(font1);
		add(qPower, "wrap");
		
		JLabel ftpLabel = new JLabel();
		ftpLabel.setFont(font1);
		ftpLabel.setText("Functional Threshold Power");
		add(ftpLabel);

		ftPower = new JLabel();
		ftPower.setFont(font1);
		add(ftPower, "wrap");

		
		Dimension d = new Dimension(500, 400);
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
		
		distance.setText(decimalFormat.format(lastPoint.getDistance()) + " km");
		int maxHR = 0;
		int aveHR = 0;
		int minHR = 220;
		int ftHR = 0;
		double tPower = 0;

		int power = 0;
		RollingTime rollingTime = new RollingTime(5);
		// five second power
		Telemetry last = null;
		
		int maxPwr = 0;
		double qPwr = 0;
		for (Telemetry t : data) {
			// 5 second power
			rollingTime.add(t.getPower(), t.getTime() / 1000);
			int avePwr = rollingTime.getAverage();
			if (avePwr > power) {
				power = avePwr;
			}
			
			if (maxPwr < t.getPower()) {
				maxPwr = t.getPower();
			}
			
			qPwr += t.getPower() * t.getPower();
			if (t.getHeartRate() > maxHR) {
				maxHR = t.getHeartRate();
			}
			if (t.getHeartRate() < minHR) {
				minHR = t.getHeartRate();
			}
			if (last != null) {
				/*
				 * if data is recovered we need to take into account the time gap, so we check to see if T > T' by more than 2 seconds and then we adjust last time
				 */
				tPower += t.getPower() * (t.getTime() - last.getTime());
				aveHR += t.getHeartRate() * (t.getTime() - last.getTime());
			}
			last = t;
		}
		qPwr /= data.size();
		qPwr = Math.sqrt(qPwr);
		qPower.setText(String.format("%.0f", qPwr) + " Watts");
		
		
		double weight = UserPreferences.INSTANCE.getWeight();
		fiveSecondPower.setText(power + " Watts");
		fiveSecondWKG.setText(String.format("%.2f", power/weight) + " W/kg");
		maxHeartRate.setText(maxHR + " bpm");
		aveHeartRate.setText(aveHR/len + " bpm");
		
		avePower.setText(String.format("%.0f", tPower / len) + " Watts");
		maxPower.setText(maxPwr + " Watts");
		totalPower.setText(String.format("%.0f", tPower / (3600000)) + " Watts");
		
		len = len / 1000; // convert to seconds
		
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
			oneMinuteWKG.setText(String.format("%.2f", power/weight) + " W/kg");
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
			fiveMinuteWKG.setText(String.format("%.2f", power/weight) + " W/kg");
		}
		
		if (len > 1200) {
			// 20 minute second power
			power = 0;
			rollingTime = new RollingTime(1200);
			for (Telemetry t : data) {
				rollingTime.add(t.getPower(), t.getTime() / 1000);
				int avePwr = rollingTime.getAverage();
				if (avePwr > power) {
					power = avePwr;
				}
			}
			twentyMinutePower.setText(power + " Watts");
			twentyMinuteWKG.setText(String.format("%.2f", power/weight) + " W/kg");
			ftPower.setText(String.format("%.2f", power / 1.05) + " Watts");
			
			// 20 minute HR
			ftHR = 0;
			rollingTime = new RollingTime(1200);
			for (Telemetry t : data) {
				rollingTime.add(t.getHeartRate(), t.getTime() / 1000);
				aveHR = rollingTime.getAverage();
				if (aveHR > ftHR) {
					ftHR = aveHR;
				}
			}
			fTHR.setText(ftHR + " bpm");
		}

	

		setVisible(true);

	}
}
