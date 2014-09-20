package com.wattzap.view.training;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.Telemetry;
import com.wattzap.model.dto.WorkoutData;

/**
 * (c) 2014 David George / Wattzap.com
 * 
 * @author David George
 * @date 1 January 2014
 */
public class TrainingAnalysis extends JFrame {
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
	private JLabel ft1Power;
	private JLabel ft20Power;

	private JLabel load;
	private JLabel stress;

	SimpleDateFormat df;
	DecimalFormat decimalFormat = new DecimalFormat("#.##");

	private JLabel time;

	private static Logger logger = LogManager.getLogger("Training Analysis");

	private final UserPreferences userPrefs = UserPreferences.INSTANCE;

	public TrainingAnalysis() {
		super();

		setTitle("Analysis");
		ImageIcon img = new ImageIcon("icons/preferences.jpg");
		setIconImage(img.getImage());

		df = new SimpleDateFormat("H'h' m'm' s's'");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));

		MigLayout layout = new MigLayout();
		this.setLayout(layout);
		setBackground(Color.LIGHT_GRAY);

		int style1 = Font.CENTER_BASELINE;
		Font font1 = new Font("Arial", style1, 13);

		JLabel fiveSecondPowerLabel = new JLabel();
		fiveSecondPowerLabel.setFont(font1);
		fiveSecondPowerLabel.setText(userPrefs.messages.getString("5secpow"));
		add(fiveSecondPowerLabel);

		fiveSecondPower = new JLabel();
		fiveSecondPower.setFont(font1);

		fiveSecondWKG = new JLabel();
		fiveSecondWKG.setFont(font1);
		add(fiveSecondPower);
		add(fiveSecondWKG, "wrap");

		JLabel oneMinutePowerLabel = new JLabel();
		oneMinutePowerLabel.setFont(font1);
		oneMinutePowerLabel.setText(userPrefs.messages.getString("1minpow"));
		add(oneMinutePowerLabel);

		oneMinutePower = new JLabel();
		oneMinutePower.setFont(font1);
		oneMinuteWKG = new JLabel();
		oneMinuteWKG.setFont(font1);
		add(oneMinutePower);
		add(oneMinuteWKG, "wrap");

		JLabel fiveMinutePowerLabel = new JLabel();
		fiveMinutePowerLabel.setFont(font1);
		fiveMinutePowerLabel.setText(userPrefs.messages.getString("5minpow"));
		add(fiveMinutePowerLabel);

		fiveMinutePower = new JLabel();
		fiveMinutePower.setFont(font1);
		fiveMinuteWKG = new JLabel();
		fiveMinuteWKG.setFont(font1);
		add(fiveMinutePower);
		add(fiveMinuteWKG, "wrap");

		JLabel twentyMinutePowerLabel = new JLabel();
		twentyMinutePowerLabel.setFont(font1);
		twentyMinutePowerLabel
				.setText(userPrefs.messages.getString("20minpow"));
		add(twentyMinutePowerLabel);

		twentyMinutePower = new JLabel();
		twentyMinutePower.setFont(font1);
		twentyMinuteWKG = new JLabel();
		twentyMinuteWKG.setFont(font1);
		add(twentyMinutePower);
		add(twentyMinuteWKG, "wrap");

		if (userPrefs.isAntEnabled()) {
			JLabel maxHeartRateLabel = new JLabel();
			maxHeartRateLabel.setFont(font1);
			maxHeartRateLabel.setText(userPrefs.messages.getString("maxhr"));
			add(maxHeartRateLabel);

			maxHeartRate = new JLabel();
			maxHeartRate.setFont(font1);
			add(maxHeartRate, "wrap");

			JLabel aveHeartRateLabel = new JLabel();
			aveHeartRateLabel.setFont(font1);
			aveHeartRateLabel.setText(userPrefs.messages.getString("avehr"));
			add(aveHeartRateLabel);

			aveHeartRate = new JLabel();
			aveHeartRate.setFont(font1);
			add(aveHeartRate, "wrap");

			JLabel fTHRLabel = new JLabel();
			fTHRLabel.setFont(font1);
			fTHRLabel.setText(userPrefs.messages.getString("fthr"));
			add(fTHRLabel);

			fTHR = new JLabel();
			fTHR.setFont(font1);
			add(fTHR, "wrap");
		}

		JLabel timeLabel = new JLabel();
		timeLabel.setFont(font1);
		timeLabel.setText("Time");
		add(timeLabel);

		time = new JLabel();
		time.setFont(font1);
		add(time, "wrap");

		JLabel distanceLabel = new JLabel();
		distanceLabel.setFont(font1);
		distanceLabel.setText(userPrefs.messages.getString("distance"));
		add(distanceLabel);

		distance = new JLabel();
		distance.setFont(font1);
		add(distance, "wrap");

		JLabel powerLabel = new JLabel();
		powerLabel.setFont(font1);
		powerLabel.setText(userPrefs.messages.getString("power"));
		add(powerLabel);

		totalPower = new JLabel();
		totalPower.setFont(font1);
		add(totalPower, "wrap");

		JLabel aveLabel = new JLabel();
		aveLabel.setFont(font1);
		aveLabel.setText(userPrefs.messages.getString("avepow"));
		add(aveLabel);

		avePower = new JLabel();
		avePower.setFont(font1);
		add(avePower, "wrap");

		JLabel maxLabel = new JLabel();
		maxLabel.setFont(font1);
		maxLabel.setText(userPrefs.messages.getString("maxpow"));
		add(maxLabel);

		maxPower = new JLabel();
		maxPower.setFont(font1);
		add(maxPower, "wrap");

		JLabel qLabel = new JLabel();
		qLabel.setFont(font1);
		qLabel.setText(userPrefs.messages.getString("qpow"));
		add(qLabel);

		qPower = new JLabel();
		qPower.setFont(font1);
		add(qPower, "wrap");

		JLabel ftpLabel = new JLabel();
		ftpLabel.setFont(font1);
		ftpLabel.setText(userPrefs.messages.getString("cftp"));
		add(ftpLabel);

		ftPower = new JLabel();
		ftPower.setFont(font1);
		add(ftPower, "wrap");

		JLabel ftp1Label = new JLabel();
		ftp1Label.setFont(font1);
		ftp1Label.setText(userPrefs.messages.getString("1minftp"));
		add(ftp1Label);

		ft1Power = new JLabel();
		ft1Power.setFont(font1);
		add(ft1Power, "wrap");

		JLabel ftp2Label = new JLabel();
		ftp2Label.setFont(font1);
		ftp2Label.setText(userPrefs.messages.getString("20minftp"));
		add(ftp2Label);

		ft20Power = new JLabel();
		ft20Power.setFont(font1);
		add(ft20Power, "wrap");

		JLabel loadLabel = new JLabel();
		loadLabel.setFont(font1);
		loadLabel.setText(userPrefs.messages.getString("load"));
		add(loadLabel);

		load = new JLabel();
		load.setFont(font1);
		add(load, "wrap");

		JLabel stressLabel = new JLabel();
		stressLabel.setFont(font1);
		stressLabel.setText(userPrefs.messages.getString("stress"));
		add(stressLabel);

		stress = new JLabel();
		stress.setFont(font1);
		add(stress, "wrap");

		Dimension d = new Dimension(500, 400);

		this.setSize(d);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	public void show(WorkoutData workoutData) {
		if (workoutData == null) {
			return;
		}
		time.setText(df.format(workoutData.getTime()) + " ");
		distance.setText(decimalFormat.format(workoutData.getDistance())
				+ " km");
		qPower.setText(workoutData.getQuadraticPower() + " Watts");

		double weight = workoutData.getWeight();
		fiveSecondPower.setText(workoutData.getFiveSecondPwr() + " Watts");
		fiveSecondWKG.setText(String.format("%.2f",
				workoutData.getFiveSecondPwr() / weight)
				+ " W/kg");

		avePower.setText(workoutData.getAvePower() + " Watts");
		maxPower.setText(workoutData.getMaxPower() + " Watts");
		totalPower.setText(workoutData.getTotalPower() + " Watts");

		oneMinutePower.setText(workoutData.getOneMinutePwr() + " Watts");
		oneMinuteWKG.setText(String.format("%.2f",
				workoutData.getOneMinutePwr() / weight)
				+ " W/kg");
		fiveMinutePower.setText(workoutData.getFiveMinutePwr() + " Watts");
		fiveMinuteWKG.setText(String.format("%.2f",
				workoutData.getFiveMinutePwr() / weight)
				+ " W/kg");

		twentyMinutePower.setText(workoutData.getTwentyMinutePwr() + " Watts");
		twentyMinuteWKG.setText(String.format("%.2f",
				workoutData.getTwentyMinutePwr() / weight)
				+ " W/kg");
		ftPower.setText(UserPreferences.INSTANCE.getMaxPower() + " Watts");
		ft1Power.setText(String.format("%.2f",
				workoutData.getOneMinutePwr() * 0.75) + " Watts");
		ft20Power.setText(String.format("%.2f",
				workoutData.getTwentyMinutePwr() * 0.95)
				+ " Watts");

		if (userPrefs.isAntEnabled()) {
			fTHR.setText(workoutData.getFtHR() + " bpm");
			maxHeartRate.setText(workoutData.getMaxHR() + " bpm");
			aveHeartRate.setText(workoutData.getAveHR() + " bpm");

		}
		load.setText(String.format("%.2f", workoutData.getIntensity() * 100));
		stress.setText("" + workoutData.getStress());

		setVisible(true);
	}

	public static WorkoutData analyze(ArrayList<Telemetry> data) {
		WorkoutData workoutData = new WorkoutData();

		if (data == null || data.size() == 0) {
			logger.info("No training data to analyze");
			return null;
		}
		Telemetry firstPoint = data.get(0);
		Telemetry lastPoint = data.get(data.size() - 1);
		long len = (lastPoint.getTime() - firstPoint.getTime());
		workoutData.setTime(len);
		workoutData.setDate(firstPoint.getTime());
		workoutData.setDistance(lastPoint.getDistance());

		int maxCad = 0;
		long aveCad = 0;
		int maxHR = 0;
		long aveHR = 0;
		int minHR = 220;
		int ftHR = 0;
		double tPower = 0;

		// five second power
		Telemetry last = null;

		int maxPwr = 0;
		double qPwr = 0;

		ftHR = 0;
		TreeMap<Integer, Long> pow = new TreeMap<Integer, Long>();
		Telemetry first = null;
		for (Telemetry t : data) {
			if (first == null) {
				// first time through
				first = t;
			} else {
				if (pow.containsKey(t.getPower())) {
					long time = pow.get(t.getPower());
					pow.put(t.getPower(), time
							+ (t.getTime() - first.getTime()));
				} else {
					pow.put(t.getPower(), t.getTime() - first.getTime());
				}

				first = t;
			}

			if (maxPwr < t.getPower()) {
				maxPwr = t.getPower();
			}

			qPwr += t.getPower() * t.getPower();
			if (t.getHeartRate() > maxHR) {
				maxHR = t.getHeartRate();
			}
			if (t.getCadence() > maxCad) {
				maxCad = t.getCadence();
			}
			if (t.getHeartRate() != -1 && t.getHeartRate() < minHR) {
				minHR = t.getHeartRate();
			}
			if (last != null) {
				/*
				 * if data is recovered we need to take into account the time
				 * gap, so we check to see if T > T' by more than 2 seconds and
				 * then we adjust last time
				 */
				tPower += t.getPower() * (t.getTime() - last.getTime());
				if (t.getHeartRate() > 0) {
					aveHR += t.getHeartRate() * (t.getTime() - last.getTime());
				}
				aveCad += t.getCadence() * (t.getTime() - last.getTime());
			}
			last = t;
		}// for

		int fiveSecPwr = 0;
		int oneMinPwr = 0;
		int fiveMinPwr = 0;
		int twentyMinPwr = 0;
		long tot = 0;
		for (Map.Entry<Integer, Long> entry : pow.descendingMap().entrySet()) {
			int key = entry.getKey();
			long value = entry.getValue();
			tot += value;
			if (tot >= 5000 && fiveSecPwr == 0) {
				fiveSecPwr = key;
			}
			if (tot >= 60000 && oneMinPwr == 0) {
				oneMinPwr = key;
			}
			if (tot >= 300000 && fiveMinPwr == 0) {
				fiveMinPwr = key;
			}
			if (tot >= 1200000 && twentyMinPwr == 0) {
				twentyMinPwr = key;
			}
		}// for
		workoutData.setFiveSecondPwr(fiveSecPwr);
		workoutData.setFiveMinutePwr(fiveMinPwr);
		workoutData.setOneMinutePwr(oneMinPwr);
		workoutData.setTwentyMinutePwr(twentyMinPwr);

		workoutData.setFtHR(ftHR);
		qPwr /= data.size();
		qPwr = Math.sqrt(qPwr);
		workoutData.setQuadraticPower((int) qPwr);

		workoutData.setWeight(UserPreferences.INSTANCE.getWeight());

		workoutData.setMaxHR(maxHR);
		workoutData.setMinHR(minHR);
		workoutData.setMaxCadence(maxCad);
		workoutData.setAveCadence((int) (aveCad / len));
		workoutData.setAveHR((int) (aveHR / len));

		workoutData.setAvePower((int) (tPower / len));
		workoutData.setMaxPower(maxPwr);
		workoutData.setTotalPower((int) (tPower / (3600000)));

		return workoutData;

	}
}
