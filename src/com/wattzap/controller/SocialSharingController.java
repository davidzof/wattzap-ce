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
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.Telemetry;
import com.wattzap.model.social.SelfLoopsAPI;
import com.wattzap.utils.TcxWriter;
import com.wattzap.view.Workouts;
import com.wattzap.view.training.TrainingDisplay;

/**
 * Controller supporting social sharing operations
 * 
 * (c) 2015 David George / Wattzap.com
 * 
 * @author David George
 * @date 9th May 2015
 */
public class SocialSharingController implements ActionListener {
	private static Logger logger = LogManager
			.getLogger("Social Sharing Controller");
	private final static UserPreferences userPrefs = UserPreferences.INSTANCE;
	private final JFrame mainFrame;
	private final TrainingDisplay trainingDisplay;
	public final static String selfLoopsUpload = "SLU";

	Workouts workouts = null;

	public SocialSharingController(TrainingDisplay trainingDisplay, JFrame frame) {
		this.trainingDisplay = trainingDisplay;

		mainFrame = frame;
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (selfLoopsUpload.equals(command)) {
			System.out.println("Self Loops UPload");
			ArrayList<Telemetry> data = trainingDisplay.getData();
			if (data == null || data.size() == 0) {
				JOptionPane.showMessageDialog(mainFrame,
						userPrefs.messages.getString("noDataUpload"),
						userPrefs.messages.getString("warning"),
						JOptionPane.WARNING_MESSAGE);
				logger.warn("No data to save");
				return;
			}
			TcxWriter writer = new TcxWriter();
			String fileName = writer.save(data, 0);
			
			try {
				SelfLoopsAPI.uploadActivity(userPrefs.getSLUser(),
						userPrefs.getSLPass(), UserPreferences.INSTANCE.getUserDataDirectory()
						+ TcxWriter.WORKOUTDIR + fileName,
						"Uploaded by http://www.wattzap.com/");
				JOptionPane.showMessageDialog(mainFrame,
						userPrefs.messages.getString("uploadTo") + " SelfLoops.com",
						userPrefs.messages.getString("uploadWk"),
						JOptionPane.INFORMATION_MESSAGE);
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(
						mainFrame,
						userPrefs.messages.getString("uploadError")
								+ " " + e1.getLocalizedMessage(),
						userPrefs.messages.getString("error"),
						JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}
}