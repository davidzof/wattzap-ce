package com.wattzap.view.training;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

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
 * Speed and Cadence ANT+ processor.
 * 
 * @author David George
 * @date 1 September 2013
 */
public class TrainingDisplay extends JPanel implements MessageCallback {
	private static final long serialVersionUID = 1L;
	private SimpleXYChartSupport support = null;
	int cadence;
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

		MessageBus.INSTANCE.register(Messages.SPEEDCADENCE, this);
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
		descriptor.addItem(userPrefs.messages.getString("power"), darkOrange,
				1.0f, Color.red, null, null);
		numElements = 1;

		if (antEnabled) {
			Color green = new Color(28, 237, 00);
			descriptor.addItem(userPrefs.messages.getString("heartrate"),
					green, 1.0f, Color.green, null, null);
			descriptor.addItem(userPrefs.messages.getString("cadence"),
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

	private void update(Telemetry t) {

		if (time == t.getTime()) {
			// no change
			return;
		}
		time = t.getTime();

		if (startTime == 0) {
			startTime = time; // start time
		}

		long[] values = new long[numElements];
		values[0] = t.getPower();
		if (antEnabled) {
			values[1] = t.getHeartRate();

			if (t.getCadence() != -1) {
				cadence = t.getCadence();
			}
			values[2] = cadence;
		}

		// training
		if (current != null && antEnabled) {

			long ct = current.getTime();
			if (ct > 0) {
				if (aggregateTime + (time - startTime) > current.getTime()) {
					if (training.hasNext()) {
						current = training.next();

						MessageBus.INSTANCE
								.send(Messages.TRAININGITEM, current);
						// Sound beep on training change
						Toolkit.getDefaultToolkit().beep();
					}
				}
			} else {
				// time based
				if (t.getDistance() > current.getDistance()) {
					if (training.hasNext()) {
						current = training.next();
						MessageBus.INSTANCE
								.send(Messages.TRAININGITEM, current);
						// Sound beep on training change
						Toolkit.getDefaultToolkit().beep();
					}
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

		add(t);
	}

	/*
	 * Save every one point for every second TODO: move this to data acquisition
	 * so we don't even send this points
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
		ObjectInputStream objectinputstream = null;
		data = new ArrayList<Telemetry>();
		Telemetry t = null;
		try {
			FileInputStream streamIn = new FileInputStream(userPrefs.getWD()
					+ "/journal.ser");
			objectinputstream = new ObjectInputStream(streamIn);

			while ((t = (Telemetry) objectinputstream.readObject()) != null) {
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
				MessageBus.INSTANCE.send(Messages.STARTPOS, t.getDistance());
			}
			if (objectinputstream != null) {
				try {
					objectinputstream.close();
				} catch (IOException e) {
					logger.error("Cannot close journal file "
							+ e.getLocalizedMessage());
				}
			}
		}
	}

	@Override
	public void callback(Messages message, Object o) {
		switch (message) {
		case SPEEDCADENCE:
			if (numElements > 0) {
				// TODO: this is a race hazard, this method can be called before
				// setup, hence this test.
				Telemetry t = (Telemetry) o;
				update(t);
			}

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
			if (data == null) {
				data = new ArrayList<Telemetry>();

				try {
					FileOutputStream fout = new FileOutputStream(
							userPrefs.getWD() + "/journal.ser", false);

					oos = new ObjectOutputStream(fout);
				} catch (Exception e) {
					logger.error("Can't create journal file "
							+ e.getLocalizedMessage());
				}

			}
			startTime = 0;
			MessageBus.INSTANCE.send(Messages.TRAININGITEM, current);

			break;
		case STARTPOS:
			double distance = (Double) o;
			if (current != null && current.getTime() == 0 && tData != null) {
				training = tData.getTraining().iterator();

				// Power Program
				TrainingItem item = current = training.next();
				while (current.getDistance() < distance) {
					if (training.hasNext()) {
						current = training.next();
					}
					item = current;
				}
				current = item;
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
			// data = new ArrayList<Telemetry>();
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