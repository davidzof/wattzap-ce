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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import com.wattzap.model.UserPreferences;
import com.wattzap.model.power.Power;
import com.wattzap.model.power.PowerProfiles;

// TODO: Add video directory location
public class Preferences extends JFrame implements ActionListener {
	private static final long serialVersionUID = 2396482595423749504L;

	// personal data
	JTextField weight;
	JTextField bikeWeight;
	JTextField wheelSize;
	JTextField maxHR;
	JTextField maxPwr;
	JCheckBox units;
	JLabel weightLabel;
	JLabel bikeWeightLabel;

	TurboPanel trainerPanel;
	AntPanel antPanel;
	UserPreferences userPrefs = UserPreferences.INSTANCE;

	public Preferences() {
		setTitle("Preferences");
		ImageIcon img = new ImageIcon("icons/preferences.jpg");
		setIconImage(img.getImage());

		JTabbedPane jtp = new JTabbedPane();

		Container contentPane = getContentPane();
		// MigLayout layout1 = new MigLayout();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(jtp, BorderLayout.CENTER);

		contentPane.setBackground(Color.lightGray);

		// Personal Data
		JPanel userPanel = new JPanel();
		userdata(userPanel);

		// ANT+ Pairing
		antPanel = new AntPanel();

		// Trainer Profiles
		trainerPanel = new TurboPanel();

		jtp.addTab(UserPreferences.INSTANCE.messages
				.getString("personal_data"), userPanel);
		jtp.addTab("Trainer", trainerPanel);
		jtp.addTab("ANT+", antPanel);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		JButton saveButton = new JButton(UserPreferences.INSTANCE.messages
				.getString("saveclose"));
		saveButton.setPreferredSize(new Dimension(150, 30));
		saveButton.setActionCommand("save");
		saveButton.addActionListener(this);
		buttonPanel.add(saveButton);

		JButton cancelButton = new JButton(UserPreferences.INSTANCE.messages
				.getString("cancel"));
		cancelButton.setPreferredSize(new Dimension(120, 30));
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(this);
		buttonPanel.add(cancelButton);
		contentPane.add(buttonPanel, BorderLayout.SOUTH);

		pack();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		// dispose();
	}

	private void userdata(JPanel tab) {
		MigLayout layout = new MigLayout();
		tab.setLayout(layout);

		// User Weight
		weightLabel = new JLabel();
		weight = new JTextField(20);
		// Bike weight
		bikeWeightLabel = new JLabel();
		bikeWeight = new JTextField(20);
		if (userPrefs.isMetric()) {
			weightLabel.setText(UserPreferences.INSTANCE.messages
					.getString("your_weight") + " (kg) ");
			bikeWeightLabel.setText(UserPreferences.INSTANCE.messages
					.getString("bike_weight") + " (kg) ");
			weight.setText(String.format("%.1f",userPrefs.getWeight()));
			bikeWeight.setText(String.format("%.1f",userPrefs.getBikeWeight()));
		} else {
			weightLabel.setText(UserPreferences.INSTANCE.messages
					.getString("your_weight")+ " (lbs)");
			bikeWeightLabel.setText(UserPreferences.INSTANCE.messages
					.getString("bike_weight") + " (lbs)");
			weight.setText(String.format("%.0f", userPrefs.getWeight()));
			bikeWeight.setText("" + userPrefs.getBikeWeight());
		}
		tab.add(weightLabel);
		tab.add(weight, "span");
		tab.add(bikeWeightLabel);
		tab.add(bikeWeight, "span");

		JLabel wheelLabel = new JLabel();
		wheelLabel.setText(UserPreferences.INSTANCE.messages
				.getString("wheel_size") + " (mm)");
		tab.add(wheelLabel);
		wheelSize = new JTextField(20);
		wheelSize.setText("" + userPrefs.getWheelsize());
		tab.add(wheelSize, "span");

		JLabel hrLabel = new JLabel();
		hrLabel.setText("FT Heart Rate");
		tab.add(hrLabel);
		maxHR = new JTextField(20);
		maxHR.setText("" + userPrefs.getMaxHR());
		tab.add(maxHR, "span");

		JLabel pwrLabel = new JLabel();
		pwrLabel.setText(UserPreferences.INSTANCE.messages
				.getString("ftp"));
		tab.add(pwrLabel);
		maxPwr = new JTextField(20);
		maxPwr.setText("" + userPrefs.getMaxPower());
		tab.add(maxPwr, "span");

		units = new JCheckBox("Metric");
		units.setSelected(userPrefs.isMetric());
		units.setActionCommand("units");
		units.addActionListener(this);
		tab.add(units);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if ("save".equals(command)) {
			updatePreferences();
			setVisible(false); // you can't see me!
			dispose(); // Destroy the JFrame object
			return;

		} else if ("units".equals(command)) {
			if (units.isSelected()) {
				weightLabel.setText(UserPreferences.INSTANCE.messages
						.getString("your_weight") + " (kg) ");
				bikeWeightLabel.setText(UserPreferences.INSTANCE.messages
						.getString("bike_weight") + " (kg) ");
				userPrefs.setUnits(true);
				weight.setText(String.format("%.1f", userPrefs.getWeight()));
				bikeWeight.setText(String.format("%.1f", userPrefs.getBikeWeight()));
			} else {
				weightLabel.setText(UserPreferences.INSTANCE.messages
						.getString("your_weight")+ " (lbs)");
				bikeWeightLabel.setText(UserPreferences.INSTANCE.messages
						.getString("bike_weight") + " (lbs)");
				userPrefs.setUnits(false);
				weight.setText(String.format("%.1f", userPrefs.getWeight()));
				bikeWeight.setText(String.format("%.1f", userPrefs.getBikeWeight()));
			}

		} else if ("cancel".equals(command)) {
			setVisible(false); // you can't see me!
			dispose(); // Destroy the JFrame object
			return;

		}

		setVisible(true);
	}

	public void updatePreferences() {
		userPrefs.setVirtualPower(trainerPanel.isVirtualPower());

		try {
			userPrefs.setWeight(Double.parseDouble(weight.getText()));
			userPrefs.setBikeWeight(Double.parseDouble(bikeWeight.getText()));
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(this, nfe.getMessage(),
					"Weight format error: ", JOptionPane.ERROR_MESSAGE);
		}
		try {
			userPrefs.setWheelsize(Integer.parseInt(wheelSize.getText()));
		} catch (NumberFormatException nfe) {
			JOptionPane
					.showMessageDialog(
							this,
							nfe.getMessage(),
							"Wheelsize format error, should be a whole number of millimeters: ",
							JOptionPane.ERROR_MESSAGE);
		}
		try {
			userPrefs.setMaxHR(Integer.parseInt(maxHR.getText()));
		} catch (NumberFormatException nfe) {
			JOptionPane
					.showMessageDialog(
							this,
							nfe.getMessage(),
							"HR format error, should be an integer between 100-220 bpm: ",
							JOptionPane.ERROR_MESSAGE);
		}
		try {
			userPrefs.setMaxPower(Integer.parseInt(maxPwr.getText()));
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(this, nfe.getMessage(),
					"Power format error: ", JOptionPane.ERROR_MESSAGE);
		}

		// set trainer
		String profile = trainerPanel.getProfileDescription();
		PowerProfiles pp = PowerProfiles.INSTANCE;
		Power p = pp.getProfile(profile);
		userPrefs.setPowerProfile(profile);
		if (p.getResitanceLevels() > 1) {
			int level = trainerPanel.getResistanceLevel();
			userPrefs.setResistance(level);
		} else {
			userPrefs.setResistance(1);
		}

		// set ANT Ids
		userPrefs.setSCId(antPanel.getSCId());
		userPrefs.setHRMId(antPanel.getHRMId());
		antPanel.close();
	}
}