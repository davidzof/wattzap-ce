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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import com.wattzap.controller.MessageBus;
import com.wattzap.controller.MessageCallback;
import com.wattzap.controller.Messages;
import com.wattzap.model.UserPreferences;
import com.wattzap.model.ant.AdvancedSpeedCadenceListener;
import com.wattzap.model.ant.Ant;
import com.wattzap.model.ant.AntListener;
import com.wattzap.model.ant.CadenceListener;
import com.wattzap.model.ant.HeartRateListener;
import com.wattzap.model.ant.SpeedListener;
import com.wattzap.model.dto.Telemetry;

/**
 * Searches for and pairs ANT devices
 * 
 * @author David George
 * @date 25th August 2013
 */
public class AntPanel extends JPanel implements ActionListener, ItemListener,
		MessageCallback {
	private static final long serialVersionUID = 1L;
	private JTextField sandcField;
	private JTextField hrmIdField;
	private JTextField cadenceField;
	private JTextField speedField;
	private JLabel speedLabel;
	private JLabel sandcLabel;
	private JLabel hrm;
	private JLabel cadenceLabel;
	private JLabel status;
	private Ant antDevice;
	private int hrmID;
	private int scID;
	JCheckBox antUSBM;
	JCheckBox scCheckBox;
	JCheckBox cadCheckBox;
	JCheckBox speedCheckBox;
	JCheckBox powCheckBox;
	JCheckBox hrmCheckBox;
	JButton pairButton;
	// Interface Text
	private final JLabel sCIDlabel;
	private final JLabel cadIdLabel;
	private final JLabel speedIdLabel;
	private final JLabel hrLabel;

	private UserPreferences userPrefs = UserPreferences.INSTANCE;
	private HashMap<String, AntListener> antListeners;

	public AntPanel() {
		super();
		MigLayout layout = new MigLayout();
		setLayout(layout);

		// speed and cadence sensor
		sCIDlabel = new JLabel();

		sandcField = new JTextField(10);
		sandcField.setText("" + userPrefs.getSCId());
		add(sCIDlabel);
		add(sandcField);
		sandcLabel = new JLabel();
		sandcLabel.setText("0 km/h");
		add(sandcLabel, "w 60!");
		scCheckBox = new JCheckBox();
		add(scCheckBox, "gapleft 30, wrap");
		scCheckBox.addItemListener(this);

		// Cadence Sensor
		cadIdLabel = new JLabel();
		cadenceField = new JTextField(10);
		cadenceField.setText(""); // cadence ID
		add(cadIdLabel);
		add(cadenceField);
		cadenceLabel = new JLabel();
		cadenceLabel.setText("0 rpm");
		add(cadenceLabel, "w 60!");
		cadCheckBox = new JCheckBox();
		cadCheckBox.addItemListener(this);
		add(cadCheckBox, "gapleft 30, wrap");

		// Speed Sensor
		speedIdLabel = new JLabel();

		speedField = new JTextField(10);
		speedField.setText(""); // cadence ID
		add(speedIdLabel);
		add(speedField);
		speedLabel = new JLabel();
		speedLabel.setText("0 km/h");
		add(speedLabel, "w 60!");
		speedCheckBox = new JCheckBox();
		add(speedCheckBox, "gapleft 30, wrap");

		// Heart Rate Strap
		JLabel label2 = new JLabel();
		label2.setText("HRM Id");
		hrmIdField = new JTextField(10);
		hrmIdField.setText("" + userPrefs.getHRMId());
		add(label2);
		add(hrmIdField, "wrap");

		hrLabel = new JLabel();
		hrm = new JLabel();
		hrm.setText("0 bpm");
		add(hrLabel);
		add(hrm, "wrap");

		antUSBM = new JCheckBox("ANTUSB-m Stick");
		antUSBM.setSelected(userPrefs.isANTUSB());
		antUSBM.addItemListener(this);

		add(antUSBM, "wrap");

		pairButton = new JButton("Pair");
		pairButton.setPreferredSize(new Dimension(60, 30));
		pairButton.setActionCommand("pair");
		pairButton.addActionListener(this);

		JButton stopButton = new JButton(userPrefs.messages.getString("stop"));
		stopButton.setPreferredSize(new Dimension(60, 30));
		stopButton.setActionCommand("stop");
		stopButton.addActionListener(this);

		add(pairButton);
		add(stopButton, "wrap");

		status = new JLabel();
		status.setText("");
		add(status, "span");

		MessageBus.INSTANCE.register(Messages.SPEEDCADENCE, this);
		MessageBus.INSTANCE.register(Messages.SPEED, this);
		MessageBus.INSTANCE.register(Messages.CADENCE, this);
		MessageBus.INSTANCE.register(Messages.HEARTRATE, this);
		MessageBus.INSTANCE.register(Messages.LOCALE, this);

		setText();
	}

	@Override
	public void callback(Messages message, Object o) {
		Telemetry t = (Telemetry) o;
		switch (message) {
		case HEARTRATE:
			int hr = t.getHeartRate();
			if (hr != -1) {
				hrm.setText(Integer.toString(hr) + " bpm");
			}
			break;
		case SPEEDCADENCE:
			if (userPrefs.isMetric()) {
				sandcLabel.setText(String.format("%.1f", t.getSpeedKMH())
						+ " km/h");
			} else {
				sandcLabel.setText(String.format("%.1f", t.getSpeedMPH())
						+ " mph");
			}
			break;
		case SPEED:
			double speed = t.getSpeedKMH();
			if (userPrefs.isMetric()) {
				speedLabel.setText(String.format("%.1f", t.getSpeedKMH())
						+ " km/h");
			} else {
				speedLabel.setText(String.format("%.1f", t.getSpeedMPH())
						+ " mph");
			}

			break;
		case CADENCE:
			int cadence = t.getCadence();
			cadenceLabel.setText(Integer.toString(cadence) + " rpm");

			break;
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		Object source = e.getItemSelectable();
		System.out.println("source " + source);
		if (source == scCheckBox) {
			if (scCheckBox.isSelected()) {
				cadCheckBox.setSelected(false);
				speedCheckBox.setSelected(false);
			}
		} else if (source == cadCheckBox) {
			if (cadCheckBox.isSelected()) {
				scCheckBox.setSelected(false);
			}

		} else if (source == antUSBM) {
			if (antUSBM.isSelected()) {
				userPrefs.setAntUSBM(true);
			} else {
				userPrefs.setAntUSBM(false);

			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		if ("pair".equals(command)) {
			antListeners = new HashMap<String, AntListener>();

			AntListener listener = new AdvancedSpeedCadenceListener();
			antListeners.put(listener.getName(), listener);
			listener = new CadenceListener();
			antListeners.put(listener.getName(), listener);
			listener = new SpeedListener();
			antListeners.put(listener.getName(), listener);
			listener = new HeartRateListener();
			antListeners.put(listener.getName(), listener);
			antDevice = new Ant(antListeners);
			// FIXME will need to set all ids to zero first
			antDevice.open();
			status.setText("Attempting pairing...");

		} else {
			status.setText("Pairing complete...");
			MessageBus.INSTANCE.unregister();
			hrmID = antDevice.getHRMChannelId();
			hrmIdField.setText("" + hrmID);
			scID = antDevice.getSCChannelId();
			sandcField.setText("" + scID);
			cadenceField.setText("12345");
			speedField.setText("12345");
			
			antDevice.close();
			antListeners = null;
		}

	}

	private void setText() {
		sCIDlabel.setText("Speed and Cadence ID");
		cadIdLabel.setText("Cadence ID");
		speedIdLabel.setText("Speed ID");
		hrLabel.setText(userPrefs.messages.getString("heartrate"));

	}

	public void close() {
		status.setText("");
		speedLabel.setText("");
		hrm.setText("");

	}

	public int getSCId() {
		return scID;
	}

	public int getHRMId() {
		return hrmID;
	}
}
