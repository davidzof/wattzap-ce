package com.wattzap.view.training;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import au.com.bytecode.opencsv.CSVReader;

import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.TrainingData;
import com.wattzap.model.dto.TrainingItem;

public class TrainingPicker extends JFileChooser implements ActionListener {
	private static final long serialVersionUID = 1L;
	JFrame frame;
	private List<ActionListener> listeners = new ArrayList<ActionListener>();

	public TrainingPicker(JFrame panel) {
		super();
		this.frame = panel;
		File cwd = new File(UserPreferences.INSTANCE.getAppData() + "/Training");
		setCurrentDirectory(cwd);

		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Training Files (.trn)", "trn");
		setFileFilter(filter);
	}

	public void actionPerformed(ActionEvent e) {
		System.out.println("1. " + e.getSource().getClass().getName());
		System.out.println(e.getActionCommand());
		System.out.println("id " + e.getID());

		int runningTime = 0;
		int retVal = showOpenDialog(frame);

		if (retVal == JFileChooser.APPROVE_OPTION) {
			File file = getSelectedFile();
			// This is where a real application would open the file.
			System.out.println("Opening: " + file.getAbsolutePath());

			CSVReader reader;
			TrainingData tData = new TrainingData();

			try {
				reader = new CSVReader(new FileReader(file));

				String[] nextLine;

				while ((nextLine = reader.readNext()) != null) {
					// nextLine[] is an array of values from the line
					TrainingItem item = new TrainingItem();
					String f1 = nextLine[0];
					if (f1.indexOf(':') != -1) {
						// minutes seconds
						int minutes = Integer.parseInt(f1.substring(0,
								f1.indexOf(':')));
						int seconds = Integer.parseInt(f1.substring(f1
								.indexOf(':') + 1));
						item.setTime((runningTime) + (minutes * 60) + seconds);
					} else {
						item.setTime((runningTime) + Integer.parseInt(f1) * 60);
					}
					runningTime += item.getTimeInSeconds();

					item.setDescription(nextLine[1]);
					if (!nextLine[2].isEmpty()) {
						int hr = Integer.parseInt(nextLine[2].trim());
						item.setHr((hr * UserPreferences.INSTANCE.getMaxHR()) / 100);
						System.out.println("time " + item.getTime() + " hr "
								+ item.getHR());
						tData.setHr(true);
					}
					if (!nextLine[3].isEmpty()) {
						String p = nextLine[3];

						if (p.indexOf('w') != -1) {
							// absolute power in watts
							int power = Integer.parseInt(p.substring(0,
									p.indexOf('w')).trim());
							item.setPower(power);
						} else {
							// percentage of max power
							int power = Integer.parseInt(p.trim());
							item.setPower((power * UserPreferences.INSTANCE
									.getMaxPower()) / 100);

						}
						tData.setPwr(true);
					}
					if (!nextLine[4].isEmpty()) {
						int cadence = Integer.parseInt(nextLine[4].trim());
						item.setCadence(cadence);
						tData.setCdc(true);
					}

					tData.addItem(item);
				}// while
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}

			notifyListeners(tData);
		} else {
			System.out.println("Open command cancelled by user.");
		}
	}

	private void notifyListeners(TrainingData tData) {
		for (ActionListener l : listeners) {
			l.actionPerformed(new ActionEvent(tData, 0, "trainingLoad"));
		}
	}

	public void addOpenListener(ActionListener l) {
		listeners.add(l);
	}
}