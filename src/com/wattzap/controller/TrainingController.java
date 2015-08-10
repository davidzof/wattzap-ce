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
package com.wattzap.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.Telemetry;
import com.wattzap.model.dto.WorkoutData;
import com.wattzap.utils.TcxWriter;
import com.wattzap.view.Workouts;
import com.wattzap.view.training.TrainingAnalysis;
import com.wattzap.view.training.TrainingDisplay;

/**
 * Controller linked to training data.
 * 
 * (c) 2014 David George / Wattzap.com
 * 
 * @author David George
 * @date 1 January 2014
 */
public class TrainingController implements ActionListener {
	private static Logger logger = LogManager.getLogger("TrainingController");
	private final static UserPreferences userPrefs = UserPreferences.INSTANCE;
	private final TrainingDisplay trainingDisplay;
	private final JFrame mainFrame;
	private final TrainingAnalysis analysis = new TrainingAnalysis();

	public final static String analyze = "A";
	public final static String save = "S";
	public final static String recover = "R";
	public final static String view = "V";
	public final static String open = "O";

	Workouts workouts = null;

	public TrainingController(TrainingDisplay trainingDisplay, JFrame frame) {
		this.trainingDisplay = trainingDisplay;
		mainFrame = frame;
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (save.equals(command)) {
			ArrayList<Telemetry> data = trainingDisplay.getData();
			if (data == null || data.size() == 0) {
				JOptionPane.showMessageDialog(mainFrame,
						userPrefs.messages.getString("noDataSave"),
						userPrefs.messages.getString("warning"),
						JOptionPane.WARNING_MESSAGE);
				logger.warn("No data to save");
				return;
			}

			int dialogButton = JOptionPane.YES_NO_OPTION;
			Telemetry zero = data.get(0);
			int gpsData = 1;
			if (zero != null && zero.getLatitude() <= 90) {
				// gpsData == 0 is Yes
				gpsData = JOptionPane.showConfirmDialog(mainFrame,
						userPrefs.messages.getString("saveGPS"),
						userPrefs.messages.getString("saveGPS"), dialogButton);
			}
			TcxWriter writer = new TcxWriter();

			String fileName = writer.save(data, gpsData);
			WorkoutData workoutData = TrainingAnalysis.analyze(data);
			workoutData.setTcxFile(fileName);
			workoutData.setFtp(UserPreferences.INSTANCE.getMaxPower());
			workoutData.setDescription(trainingDisplay.getName());
			UserPreferences.INSTANCE.addWorkout(workoutData);

			JOptionPane.showMessageDialog(
					mainFrame,
					userPrefs.messages.getString("wktSave") + ": "
							+ userPrefs.getUserDataDirectory()
							+ TcxWriter.WORKOUTDIR + fileName, "Workout Saved",
					JOptionPane.INFORMATION_MESSAGE);

			if (workouts != null) {
				workouts.updateModel();
			}
		} else if (analyze.equals(command)) {
			ArrayList<Telemetry> data = trainingDisplay.getData();
			WorkoutData wData = TrainingAnalysis.analyze(data);
			if (wData != null) {
				wData.setFtp(UserPreferences.INSTANCE.getMaxPower());
				analysis.show(wData);
			}
		} else if (recover.equals(command)) {
			// recover data
			trainingDisplay.loadJournal();

		} else if (view.equals(command)) {
			if (workouts == null) {
				workouts = new Workouts();
			} else {
				workouts.setVisible(true);
			}
		}
	}
}