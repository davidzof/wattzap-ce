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
 * (c) 2014  Wattzap.com
 * 
 * @author David George
 * @date 17 April 2014
 */
public class WorkoutButtonPanel extends JPanel implements ActionListener {
	Workouts workouts;

	public WorkoutButtonPanel(Workouts workouts) {
		this.workouts = workouts;

		JButton stopButton = new JButton(
				UserPreferences.INSTANCE.messages.getString("delete"));
		stopButton.setActionCommand("del");
		JButton startButton = new JButton(
				UserPreferences.INSTANCE.messages.getString("load"));
		startButton.setActionCommand("load");

		startButton.addActionListener(this);
		stopButton.addActionListener(this);

		setBackground(Color.LIGHT_GRAY);
		add(startButton);
		add(stopButton);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if ("load".equals(command)) {
			workouts.load();
		} else {
			workouts.delete();
		}

	}
}
