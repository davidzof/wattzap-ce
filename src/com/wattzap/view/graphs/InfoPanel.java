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
package com.wattzap.view.graphs;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.WorkoutData;

/**
 * (c) 2014 Wattzap.com
 * 
 * Workout information panel. Displays all the analyzed data from the workout.
 * 
 * @author David George
 * @date 22 September 2014
 */
public class InfoPanel extends JPanel {

	private final static int style1 = Font.CENTER_BASELINE;
	private final static Font font1 = new Font("Arial", style1, 11);
	private final static UserPreferences userPrefs = UserPreferences.INSTANCE;
	private final static String COLGAP = "gapright 10";

	// Power
	private JLabel totalPower;
	private JLabel avePower;
	private JLabel maxPower;

	// Heart Rate
	private JLabel ftHeartRate;

	public InfoPanel() {
		MigLayout layout = new MigLayout(
			);
		this.setLayout(layout);
		setBackground(Color.LIGHT_GRAY);

		JLabel ftpLabel = new JLabel();
		ftpLabel.setFont(font1);
		ftpLabel.setText(userPrefs.messages.getString("fthr"));
		add(ftpLabel);

		ftHeartRate = new JLabel();
		ftHeartRate.setFont(font1);
		ftHeartRate.setForeground(Color.DARK_GRAY);
		add(ftHeartRate, COLGAP);

		JLabel powerLabel = new JLabel();
		powerLabel.setFont(font1);
		powerLabel.setText(userPrefs.messages.getString("power"));
		add(powerLabel);

		totalPower = new JLabel();
		totalPower.setFont(font1);
		totalPower.setForeground(Color.DARK_GRAY);
		add(totalPower, COLGAP);

		JLabel aveLabel = new JLabel();
		aveLabel.setFont(font1);
		aveLabel.setText(userPrefs.messages.getString("avepow"));
		add(aveLabel);

		avePower = new JLabel();
		avePower.setFont(font1);
		avePower.setForeground(Color.DARK_GRAY);
		add(avePower, COLGAP);

		JLabel maxLabel = new JLabel();
		maxLabel.setFont(font1);
		maxLabel.setText(userPrefs.messages.getString("maxpow"));
		add(maxLabel);

		maxPower = new JLabel();
		maxPower.setFont(font1);
		maxPower.setForeground(Color.DARK_GRAY);
		add(maxPower);
	}

	public void update(WorkoutData data) {
		if (data == null) {
			return;
		}

		avePower.setText(data.getAvePower() + " Watts");
		maxPower.setText(data.getMaxPower() + " Watts");
		totalPower.setText(data.getTotalPower() + " Watts");

		ftHeartRate.setText(data.getFtHR() + " bpm");

	}
	
	private static final long serialVersionUID = 1L;
}
