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

import javax.swing.JFileChooser;
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
	public final static String selfLoopsUpload = "SLU";

	Workouts workouts = null;

	public SocialSharingController(JFrame frame) {

		mainFrame = frame;
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (selfLoopsUpload.equals(command)) {
			System.out.println("Self Loops UPload");

		}
	}
}