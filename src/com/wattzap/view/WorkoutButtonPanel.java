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

import com.wattzap.controller.MessageBus;
import com.wattzap.controller.MessageCallback;
import com.wattzap.controller.Messages;
import com.wattzap.model.UserPreferences;

/**
 * (c) 2014 Wattzap.com
 * 
 * Bottom panel for Workouts View containing buttons for Reanalyzing and
 * Deleting selected workouts as well as Quit button.
 * 
 * @see Workouts.java
 * 
 * @author David George
 * @date 17 April 2014
 */
public class WorkoutButtonPanel extends JPanel implements ActionListener,
		MessageCallback {
	Workouts workouts;

	private final static String delete = "DEL";
	private final static String analyze = "ANAL";
	private final static String quit = "QUIT";
	private final JButton deleteButton;
	private final JButton reloadButton;
	private final JButton quitButton;

	public WorkoutButtonPanel(Workouts workouts) {
		this.workouts = workouts;

		deleteButton = new JButton();
		deleteButton.setActionCommand(delete);
		reloadButton = new JButton();
		reloadButton.setActionCommand(analyze);
		quitButton = new JButton();
		quitButton.setActionCommand(quit);

		reloadButton.addActionListener(this);
		deleteButton.addActionListener(this);
		quitButton.addActionListener(this);

		doText();
		setBackground(Color.LIGHT_GRAY);
		add(reloadButton);
		add(deleteButton);
		add(quitButton);
		
		MessageBus.INSTANCE.register(Messages.LOCALE, this);
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
			workouts.reanalyze();
		}

	}

	/*
	 * Setup button text, makes it easy to update if locale is changed
	 */
	private void doText() {
		deleteButton.setText(UserPreferences.INSTANCE.messages
				.getString("delete"));
		reloadButton.setText(UserPreferences.INSTANCE.messages
				.getString("reanal"));
		quitButton.setText(UserPreferences.INSTANCE.messages.getString("quit"));
	}

	/**
	 * Change text language if we get a LOCALE message
	 */
	@Override
	public void callback(Messages message, Object o) {
		switch (message) {
		case LOCALE:
			doText();
			break;
		}
	}

	private static final long serialVersionUID = 1L;
}
