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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import net.miginfocom.swing.MigLayout;

import com.wattzap.model.UserPreferences;
import com.wattzap.model.power.Power;
import com.wattzap.model.power.PowerProfiles;

/* 
 * @author David George (c) Copyright 2013
 * @date 19 June 2013
 */
public class TurboPanel extends JPanel implements ActionListener {
	private List<JRadioButton> trainerProfiles;
	private UserPreferences userPrefs = UserPreferences.INSTANCE;
	private JCheckBox virtualPower;
	private JComboBox resistanceLevels;

	public boolean isVirtualPower() {
		return virtualPower.isSelected();
	}

	public int getResistanceLevel() {
		return resistanceLevels.getSelectedIndex();
	}

	public TurboPanel() {
		super();
		// Create the radio buttons.
		PowerProfiles pp = PowerProfiles.INSTANCE;
		List<Power> profiles = pp.getProfiles();
		trainerProfiles = new ArrayList<JRadioButton>();

		MigLayout layout = new MigLayout();
		setLayout(layout);
		JLabel label2 = new JLabel();
		label2.setText("Select your Profile");
		add(label2, "wrap");

		ButtonGroup group = new ButtonGroup();
		String trainerDescription = userPrefs.getPowerProfile().description();
		Power selectedProfile = null;
		for (Power p : profiles) {
			JRadioButton button = new JRadioButton(p.description());

			if (p.description().equals(trainerDescription)) {
				button.setEnabled(true);
			}

			trainerProfiles.add(button);
			button.setActionCommand("trainer");
			// button.setSelected(true);
			group.add(button);
			add(button, "wrap");
			button.addActionListener(this);
			if (p.description().equals(trainerDescription)) {
				button.setSelected(true);
				selectedProfile = p;
			}

		}// for
		virtualPower = new JCheckBox("SimulSpeed");
		virtualPower.setSelected(userPrefs.isVirtualPower());
		add(virtualPower, "wrap");
		displayResistance(selectedProfile);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		// trainer selected
		String d = getProfileDescription();
		PowerProfiles pp = PowerProfiles.INSTANCE;
		Power p = pp.getProfile(d);
		if (resistanceLevels != null) {
			remove(resistanceLevels);
			resistanceLevels = null;
		}
		displayResistance(p);

	}

	public String getProfileDescription() {
		for (JRadioButton button : trainerProfiles) {
			if (button.isSelected()) {
				return button.getText();
			}
		}
		return null;
	}

	private void displayResistance(Power p) {
		if (p != null && p.getResitanceLevels() > 1) {
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
				resistanceLevels.setSelectedIndex(userPrefs.getResistance());
			}
			add(resistanceLevels);
		}
		validate();
		repaint();
	}

}
