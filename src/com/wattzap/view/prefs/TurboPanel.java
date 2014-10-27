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
package com.wattzap.view.prefs;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.wattzap.model.UserPreferences;
import com.wattzap.model.power.Power;
import com.wattzap.model.power.PowerProfiles;

/* 
 * Home trainer selection and preferences
 * 
 * You can select from a range of Home Trainers and their characteristics (resistance levels for variable resistance trainers).
 * 
 * @author David George (c) Copyright 2013
 * @date 19 June 2013
 */
public class TurboPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private final static Font font1 = new Font("Arial", Font.CENTER_BASELINE,
			12);
	private UserPreferences userPrefs = UserPreferences.INSTANCE;
	private JCheckBox virtualPower;
	private JComboBox resistanceLevels;
	private JComboBox trainerList = new JComboBox();
	private final JLabel resistanceLabel = new JLabel();

	public boolean isVirtualPower() {
		return virtualPower.isSelected();
	}

	public int getResistanceLevel() {
		return resistanceLevels.getSelectedIndex();
	}

	/**
	 * Display turbo panel.
	 */
	public TurboPanel() {
		super();
		// Create the radio buttons.
		PowerProfiles pp = PowerProfiles.INSTANCE;
		List<Power> profiles = pp.getProfiles();

		MigLayout layout = new MigLayout();
		setLayout(layout);
		JLabel label2 = new JLabel();
		label2.setText(userPrefs.messages.getString("trainer"));
		label2.setFont(font1);
		add(label2, "wrap");

		String trainerDescription = userPrefs.getPowerProfile().description();
		int index = 0;
		Power selectedProfile = null;
		for (Power p : profiles) {
			trainerList.addItem(p.description());
			if (p.description().equals(trainerDescription)) {
				trainerList.setSelectedIndex(index);
				selectedProfile = p;
			}
			index++;

		}
		trainerList.addActionListener(this);
		add(trainerList, "wrap");

		virtualPower = new JCheckBox("SimulSpeed");
		virtualPower.setSelected(userPrefs.isVirtualPower());
		add(virtualPower, "wrap");

		resistanceLabel.setText(userPrefs.messages.getString("resistance"));
		resistanceLabel.setFont(font1);
		add(resistanceLabel, "wrap");
		resistanceLabel.setVisible(false);
		displayResistance(selectedProfile);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// trainer selected, update resistance levels
		String d = getProfileDescription();
		PowerProfiles pp = PowerProfiles.INSTANCE;
		Power p = pp.getProfile(d);
		System.out.println(p);
		if (resistanceLevels != null) {
			remove(resistanceLevels);
			resistanceLabel.setVisible(false);
			resistanceLevels = null;
		}
		displayResistance(p);
	}

	public String getProfileDescription() {
		return (String) trainerList.getSelectedItem();
	}

	/**
	 * Show the resistance drop down list if the trainer is a variable
	 * resistance type
	 * 
	 * @param p - Power profile of the selected trainer
	 */
	private void displayResistance(Power p) {
		if (p != null && p.getResitanceLevels() > 1) {
			resistanceLabel.setVisible(true);
			resistanceLevels = new JComboBox();

			if (!userPrefs.isAntEnabled()) {
				// special variable resistance level when no ANT device
				resistanceLevels.addItem("auto");
			}

			for (int i = 1; i <= p.getResitanceLevels(); i++) {
				resistanceLevels.addItem("" + i);
			}
			if (p.description().equals(
					userPrefs.getPowerProfile().description())) {
				// previously selected trainer, set selected resistance level
				resistanceLevels.setSelectedIndex(userPrefs.getResistance());
			}

			add(resistanceLevels);
		}
		validate();
		repaint();
	}

}
