package com.wattzap.view.training;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.sun.tools.visualvm.charts.ChartFactory;
import com.sun.tools.visualvm.charts.SimpleXYChartDescriptor;
import com.sun.tools.visualvm.charts.SimpleXYChartSupport;
import com.wattzap.Main;
import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.Telemetry;
import com.wattzap.model.dto.TrainingData;
import com.wattzap.model.dto.TrainingItem;
import com.wattzap.utils.RollingTime;

public class TrainingDisplay extends JPanel implements ChangeListener,
		ActionListener {
	private static final long serialVersionUID = 1L;
	private SimpleXYChartSupport support = null;
	int cadence;
	long time;
	long startTime;
	Iterator<TrainingItem> training;
	TrainingData tData;
	TrainingItem current;
	private ArrayList<Telemetry> data;
	int numElements;

	private static Logger logger = LogManager.getLogger(TrainingDisplay.class
			.getName());

	public TrainingDisplay(Dimension screenSize) {
		setPreferredSize(new Dimension(screenSize.width / 2, 400));
	}

	private void createModels(TrainingData tData) {
		SimpleXYChartDescriptor descriptor = SimpleXYChartDescriptor.decimal(0,
				200, 300, 1d, true, 600);

		descriptor.addItem("Power", Color.red, 1.0f, Color.red, null, null);

		descriptor.addItem("Heartrate", Color.green, 1.0f, Color.green, null,
				null);
		descriptor.addItem("Cadence", Color.blue, 1.0f, Color.blue, null, null);

		numElements = 3;
		if (tData != null) {
			if (tData.isPwr()) {
				Color lightRed = new Color(255, 96, 64);
				descriptor.addItem("Target Power", lightRed, 2.5f, lightRed,
						null, null);
				numElements++;
			}

			if (tData.isHr()) {
				Color lightGreen = new Color(96, 255, 64);
				descriptor.addItem("Target Heartrate", lightGreen, 2.5f,
						lightGreen, null, null);
				numElements++;
			}

			if (tData.isCdc()) {
				Color lightBlue = new Color(64, 96, 255);
				descriptor.addItem("Target Cadence", lightBlue, 2.5f,
						lightBlue, null, null);
				numElements++;
			}

			descriptor
					.setDetailsItems(new String[] { "<html><font size='+1'><b>Info" });
		}

		support = ChartFactory.createSimpleXYChart(descriptor);
		JComponent chart = support.getChart();
		setLayout(new BorderLayout());
		add(chart, BorderLayout.CENTER);

		chart.setVisible(true);
		chart.revalidate();
	}

	/*
	 * Typically called from the speedCadenceListener
	 * 
	 * @see
	 * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
	 * )
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		Telemetry t = (Telemetry) e.getSource();
		long time = t.getTime();
		if (time == 0) {
			return; // no valid data
		}
		if (startTime == 0) {
			startTime = time; // start time
		}

		long[] values = new long[numElements];
		values[0] = t.getPower();
		values[1] = t.getHeartRate();

		if (t.getCadence() != -1) {
			cadence = t.getCadence();
		}
		values[2] = cadence;

		// training
		if (current != null) {
			if ((time - startTime) > current.getTime()) {
				if (training.hasNext()) {
					current = training.next();
				}
			}
			int index = 3;
			if (tData.isPwr()) {
				values[index++] = current.getPower();
				String[] details = { current.getDescription()
						+ " Target Power " + current.getPower()
						+ "</b></font></html>" };
				support.updateDetails(details);
			}
			if (tData.isHr()) {
				values[index++] = current.getHr();
			}
			if (tData.isCdc()) {
				values[index] = current.getCadence();
			}
		}

		// use telemetry time

		support.addValues(time, values);

		add(t);
	}

	/**
	 * Save every one point for every second TODO: move this to data acquisition
	 * so we don't even send this points
	 * 
	 * @param t
	 */
	private void add(Telemetry t) {
		int index = data.size();
		if (index == 0) {
			// empty, first time through
			data.add(t);
		} else {
			Telemetry tn = data.get(index - 1);
			if (t.getTime() > tn.getTime() + 1000) {
				data.add(t);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		logger.info(command);

		if ("trainingLoad".equals(command)) {
			tData = ((TrainingData) e.getSource());
			training = tData.getTraining().iterator();
			if (training.hasNext()) {
				current = training.next();
			}

			createModels(tData);
			time = 0;
			data = new ArrayList<Telemetry>();
		} else if ("start".equals(command)) {
			if (support == null) {
				createModels(null);
			}
			if (data == null) {
				data = new ArrayList<Telemetry>();
			}
		}
	}

	public ArrayList<Telemetry> getData() {
		return data;
	}
}