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
package com.wattzap.view.training;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.*;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.sun.tools.visualvm.charts.ChartFactory;
import com.sun.tools.visualvm.charts.SimpleXYChartDescriptor;
import com.sun.tools.visualvm.charts.SimpleXYChartSupport;
import com.wattzap.controller.MessageBus;
import com.wattzap.controller.MessageCallback;
import com.wattzap.controller.Messages;
import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.Telemetry;
import com.wattzap.model.dto.TrainingData;
import com.wattzap.model.dto.TrainingItem;

/**
 * (c) 2013 David George / TrainingLoops.com
 * 
 * Displays training data. Shows target power/hr/cadence based on training
 * programme with real time data coming from sensors.
 * 
 * @author David George
 * @date 1 September 2013
 */
public class TrainingDisplay extends JPanel implements MessageCallback {
	private static final long serialVersionUID = 1L;
	private SimpleXYChartSupport support = null;
	int cadence;
	int heartRate = 0;
	long aggregateTime = 0;
	long startTime;
	long time;
	Iterator<TrainingItem> training;
	TrainingData tData;
	TrainingItem current;
	private ArrayList<Telemetry> data;
	int numElements;
	JComponent chart = null;
	ObjectOutputStream oos = null;
	boolean antEnabled = true;

	private static final long MILLISECSMINUTE = 60000;

	private final UserPreferences userPrefs = UserPreferences.INSTANCE;

	private static Logger logger = LogManager.getLogger("Training Display");

	public TrainingDisplay(Dimension screenSize) {
		setPreferredSize(new Dimension(screenSize.width / 2, 400));
		setLayout(new BorderLayout());

		MessageBus.INSTANCE.register(Messages.SPEED, this);
		MessageBus.INSTANCE.register(Messages.CADENCE, this);
		MessageBus.INSTANCE.register(Messages.HEARTRATE, this);
		MessageBus.INSTANCE.register(Messages.START, this);
		MessageBus.INSTANCE.register(Messages.STARTPOS, this);
		MessageBus.INSTANCE.register(Messages.STOP, this);
		MessageBus.INSTANCE.register(Messages.TRAINING, this);
		MessageBus.INSTANCE.register(Messages.CLOSE, this);
		antEnabled = userPrefs.isAntEnabled();
	}

	private void createModels(TrainingData tData) {
		if (chart != null) {
			remove(chart);
			chart = null;
		}

		SimpleXYChartDescriptor descriptor = SimpleXYChartDescriptor.decimal(0,
				200, 300, 1d, true, 600);

		Color darkOrange = new Color(246, 46, 00);
		descriptor.addItem(userPrefs.getString("power"), darkOrange,
				1.0f, Color.red, null, null);
		numElements = 1;

		if (antEnabled) {
			Color green = new Color(28, 237, 00);
			descriptor.addItem(userPrefs.getString("heartrate"),
					green, 1.0f, Color.green, null, null);
			descriptor.addItem(userPrefs.getString("cadence"),
					Color.blue, 1.0f, Color.blue, null, null);
			numElements += 2;
		}

		if (tData != null) {
			if (tData.isPwr()) {
				Color lightOrange = new Color(255, 47, 19);
				descriptor.addItem("Target Power", lightOrange, 2.5f,
						lightOrange, null, null);
				numElements++;
			}

			if (antEnabled) {
				if (tData.isHr()) {
					Color darkGreen = new Color(0, 110, 8);
					descriptor.addItem("Target Heartrate", darkGreen, 2.5f,
							darkGreen, null, null);
					numElements++;
				}

				if (tData.isCdc()) {
					Color lightBlue = new Color(64, 96, 255);
					descriptor.addItem("Target Cadence", lightBlue, 2.5f,
							lightBlue, null, null);
					numElements++;
				}
			}
			descriptor
					.setDetailsItems(new String[] { "<html><font size='+2'><b>Info" });
		}

		support = ChartFactory.createSimpleXYChart(descriptor);
		chart = support.getChart();
		add(chart, BorderLayout.CENTER);
		chart.setVisible(true);
		chart.revalidate();
	}

	private void update(Telemetry telemetry) {
		if (time == telemetry.getTime()) {
			// no change
			return;
		}
		time = telemetry.getTime();

		if (startTime == 0) {
			startTime = time; // start time
		}

		long[] values = new long[numElements];
		values[0] = telemetry.getPower();
		if (antEnabled) {
			values[1] = telemetry.getHeartRate();
			values[2] = telemetry.getCadence();
		}

		// training
		if (current != null && antEnabled) {
			long ct = current.getTime();
			if (ct > 0) {
				// time based training
				if (aggregateTime + (time - startTime) > current.getTime()) {
					if (training.hasNext()) {
						current = training.next();

						MessageBus.INSTANCE
								.send(Messages.TRAININGITEM, current);
						// Sound beep on training change
						notifyNewTrainingInterval();
					}
				}
			} else {
				// distance based training
				if (tData.isNext(telemetry.getDistanceMeters())) {
					
					current = tData.getNext(telemetry.getDistanceMeters());
					MessageBus.INSTANCE.send(Messages.TRAININGITEM, current);
					// Sound beep on training change
					notifyNewTrainingInterval();

				}
			}

			if (tData != null) {
				int index = 3;
				if (tData.isPwr()) {
					values[index++] = current.getPower();
				}
				if (tData.isHr()) {
					values[index++] = current.getHr();
				}
				if (tData.isCdc()) {
					values[index] = current.getCadence();
				}

				String[] details = { current.getDescription()
						+ current.getPowerMsg() + current.getHRMsg()
						+ current.getCadenceMsg() + "</b></font></html>" };
				support.updateDetails(details);
			}
		}

		// use telemetry time
		support.addValues(time, values);

		add(telemetry);
	}

	private void notifyNewTrainingInterval() {
		// Sound beep on training change
		Toolkit.getDefaultToolkit().beep();
		// Display new training interva informations
		String newTrainingIntervalInformations = "<html><font size='+8'><b>"+
				current.getTimeInSeconds() + " " + current.getDescription() + "<br></b></font>"
				+ "<html><font size='+6'><b>"+current.getPowerMsg() + current.getHRMsg()
				+ current.getCadenceMsg() + "</b></font></html>";
		final JOptionPane optionPane = new JOptionPane(newTrainingIntervalInformations, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);

		final JDialog dialog = new JDialog();
		dialog.setTitle(userPrefs.messages.getString("new_interval"));
		//dialog.setModal(true);
		dialog.setLocationRelativeTo(null);
		dialog.setContentPane(optionPane);

		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.pack();

		//create timer to dispose of dialog after 5 seconds
		Timer timer = new Timer(5000, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				dialog.dispose();
			}
		});
		timer.setRepeats(false);//the timer should only go off once

		//start timer to close JDialog as dialog modal we must start the timer before its visible
		timer.start();

		dialog.setVisible(true);
	}

	/*
	 * Save every one point for every second TODO: move this to data acquisition
	 * so we don't even send these points
	 * 
	 * @param t
	 */
	private void add(Telemetry t) {
		if (data == null) {
			// not yet initialized
			return;
		}
		int index = data.size();
		if (index == 0) {
			// empty, first time through
			data.add(t);
		} else {
			Telemetry tn = data.get(index - 1);
			if (t.getTime() > tn.getTime() + 1000) {
				data.add(t);
				try {
					oos.writeObject(t);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					logger.error("Can't write telemetry data to journal "
							+ e.getLocalizedMessage());
				}
			}
		}
	}

	public ArrayList<Telemetry> getData() {
		return data;
	}

	public void loadJournal() {
		ObjectInputStream objectInputStream = null;
		data = new ArrayList<Telemetry>();
		Telemetry t = null;
		try {
			FileInputStream streamIn = new FileInputStream(userPrefs.getWD()
					+ "/journal.ser");
			objectInputStream = new ObjectInputStream(streamIn);

			while ((t = (Telemetry) objectInputStream.readObject()) != null) {
				data.add(t);
			}// while

		} catch (EOFException ex) {
			logger.info("Journal file read " + data.size() + " records");
		} catch (Exception e) {
			// data = null;
			logger.error("Cannot read journal file " + e.getLocalizedMessage()
					+ " at position " + data.size());
		} finally {
			JOptionPane.showMessageDialog(this, "Recovered " + data.size()
					+ " records", "Info", JOptionPane.INFORMATION_MESSAGE);
			if (t != null) {
				MessageBus.INSTANCE.send(Messages.STARTPOS, t.getDistanceKM());
			}
			if (objectInputStream != null) {
				try {
					objectInputStream.close();
				} catch (IOException e) {
					logger.error("Cannot close journal file "
							+ e.getLocalizedMessage());
				}
			}
		}

		// Now rewrite journal file (end might be corrupt)
		ObjectOutputStream objectOutputStream = null;
		// existing data, append to journal file
		try {
			FileOutputStream fout = new FileOutputStream(userPrefs.getWD()
					+ "/journal.ser", true);

			objectOutputStream = new ObjectOutputStream(fout);
			for (Telemetry telemetry : data) {
				oos.writeObject(telemetry);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Can't write telemetry data to journal "
					+ e.getLocalizedMessage());
		} finally {
			try {
				if (objectOutputStream != null) {
					objectOutputStream.close();
				}
			} catch (IOException e) {
				logger.error("Cannot close journal file "
						+ e.getLocalizedMessage());
			}
		}
	}

	@Override
	public void callback(Messages message, Object o) {
		
		switch (message) {
		case SPEED:
			if (numElements > 0) {
				// TODO: this is a race hazard, this method can be called before
				// setup, hence this test.

				// get a clone
				Telemetry t = new Telemetry((Telemetry) o);
				// recover last heart rate data
				t.setHeartRate(heartRate);
				t.setCadence(cadence);
				
				update(t);
			}
			break;
			
		case CADENCE:
			cadence = (Integer) o;
			break;

		case HEARTRATE:
			heartRate = (Integer) o;
			break;
			
		case STOP:
			if (data != null && !data.isEmpty()) {
				Telemetry lastPoint = data.get(data.size() - 1);
				long split = lastPoint.getTime() - startTime;
				int minutes = userPrefs.getEvalTime();
				minutes -= (split / MILLISECSMINUTE);
				userPrefs.setEvalTime(minutes);
				aggregateTime += split;
			}
			break;
			
		case START:
			if (chart == null) {
				createModels(null);
			}
			try {
				if (oos == null) {
					// oos is closed
					if (data == null) {
						// new training, truncate the journal file
						data = new ArrayList<Telemetry>();
						FileOutputStream fout = new FileOutputStream(
								userPrefs.getWD() + "/journal.ser", false);

						oos = new ObjectOutputStream(fout);
					} else {
						// existing data, append to journal file
						FileOutputStream fout = new FileOutputStream(
								userPrefs.getWD() + "/journal.ser", true);

						oos = new ObjectOutputStream(fout);
					}
				}
			} catch (Exception e) {
				logger.error("Can't create journal file "
						+ e.getLocalizedMessage());
			}

			startTime = 0;
			MessageBus.INSTANCE.send(Messages.TRAININGITEM, current);

			break;
		case STARTPOS:
			double distance = (Double) o;
			if (current != null && current.getTime() == 0 && tData != null) {
				current = tData.getNext(distance*1000);
				// MessageBus.INSTANCE.send(Messages.TRAININGITEM, current);
				String[] details = { current.getDescription()
						+ current.getPowerMsg() + current.getHRMsg()
						+ current.getCadenceMsg() + "</b></font></html>" };
				support.updateDetails(details);
			}

			break;
		case TRAINING:
			tData = (TrainingData) o;

			training = tData.getTraining().iterator();
			if (training.hasNext()) {
				current = training.next();

				MessageBus.INSTANCE.send(Messages.TRAININGITEM, current);
			}

			createModels(tData);
			aggregateTime = 0;
			break;
		case CLOSE:
			current = null;
			if (chart != null) {
				remove(chart);
				chart = null;
			}
			tData = null;
			if (oos != null) {
				try {
					oos.close();
					oos = null;
				} catch (IOException e) {
					logger.error("Can't close journal file "
							+ e.getLocalizedMessage());
				}
			}
			data = null;
			break;
		}
	}
}