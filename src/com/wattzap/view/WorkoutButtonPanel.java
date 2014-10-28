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
package com.wattzap.view;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.wattzap.model.UserPreferences;

/**
 * (c) 2014 Wattzap.com
 * 
 * Bottom panel for Workouts View containing buttons for Reanalyzing and Deleting
 * selected workouts as well as Quit button.
 * 
 * @see Workouts.java
 * 
 * @author David George
 * @date 17 April 2014
 */
public class WorkoutButtonPanel extends JPanel implements ActionListener {
	Workouts workouts;

	private final static String delete = "DEL";
	private final static String analyze = "ANAL";
	private final static String quit = "QUIT";

	public WorkoutButtonPanel(Workouts workouts) {
		this.workouts = workouts;

		JButton deleteButton = new JButton(
				UserPreferences.INSTANCE.messages.getString("delete"));
		deleteButton.setActionCommand(delete);
		
		JButton reloadButton = new JButton(
				UserPreferences.INSTANCE.messages.getString("load"));
		reloadButton.setActionCommand(analyze);
		JButton quitButton = new JButton(
				UserPreferences.INSTANCE.messages.getString("quit"));
		quitButton.setActionCommand(quit);
		
		reloadButton.addActionListener(this);
		deleteButton.addActionListener(this);
		quitButton.addActionListener(this);
		
		setBackground(Color.LIGHT_GRAY);
		add(reloadButton);
		add(deleteButton);
		add(quitButton);
	}

	@Override
	/**
	 * Call in response to button click on Workouts Bottom Panel
	 */
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (delete.equals(command)) {
			workouts.delete();
		} else if (quit.equals(command)) {
			workouts.quit();
		} else {
			workouts.reload();
		}

	}

	private static final long serialVersionUID = 1L;
}
