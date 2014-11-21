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
import com.wattzap.model.dto.Telemetry;

/**
 * Searches for and pairs ANT devices
 * 
 * @author David George
 * @date 25th August 2013
 */
public class AntPanel extends JPanel implements ActionListener, MessageCallback {
	private static final long serialVersionUID = 1L;
	private JTextField sandcField;
	private JTextField hrmIdField;
	private JTextField cadenceField;
	private JLabel speedLabel;
	private JLabel hrm;
	private JLabel cadenceLabel;
	private JLabel status;
	private Ant antDevice;
	private int hrmID;
	private int scID;
	JCheckBox antUSBM;

	private UserPreferences userPrefs = UserPreferences.INSTANCE;
	private  HashMap<String,AntListener> antListeners;
	
	public AntPanel() {
		super();
		MigLayout layout = new MigLayout();
		setLayout(layout);

		// speed and cadence sensor
		JLabel sCIDlabel = new JLabel();
		sCIDlabel.setText("Speed and Cadence ID");
		sandcField = new JTextField(10);
		sandcField.setText("" + userPrefs.getSCId());
		add(sCIDlabel);
		add(sandcField, "wrap");

		JLabel speed = new JLabel();
		speed.setText(userPrefs.messages.getString("speed"));
		speedLabel = new JLabel();
		speedLabel.setText("0 km/h");
		add(speed);
		add(speedLabel, "wrap");

		// Cadence Sensor
		JLabel label5 = new JLabel();
		label5.setText("Cadence ID");
		cadenceField = new JTextField(10);
		cadenceField.setText("ccc");
		add(label5);
		add(cadenceField, "wrap");

		JLabel label6 = new JLabel();
		label6.setText(userPrefs.messages.getString("cadence"));
		cadenceLabel = new JLabel();
		cadenceLabel.setText("0 rpm");
		add(label6);
		add(cadenceLabel, "wrap");

		JLabel label2 = new JLabel();
		label2.setText("HRM Id");
		hrmIdField = new JTextField(10);
		hrmIdField.setText("" + userPrefs.getHRMId());
		add(label2);
		add(hrmIdField, "wrap");

		JLabel label4 = new JLabel();
		label4.setText(userPrefs.messages.getString("heartrate"));
		hrm = new JLabel();
		hrm.setText("0 bpm");
		add(label4);
		add(hrm, "wrap");

		antUSBM = new JCheckBox("ANTUSB-m Stick");
		antUSBM.setSelected(userPrefs.isANTUSB());
		antUSBM.setActionCommand("antusbm");
		antUSBM.addActionListener(this);

		add(antUSBM, "wrap");

		JButton pairButton = new JButton("Pair");
		pairButton.setPreferredSize(new Dimension(60, 30));
		pairButton.setActionCommand("start");
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
		MessageBus.INSTANCE.register(Messages.CADENCE, this);
		MessageBus.INSTANCE.register(Messages.HEARTRATE, this);
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
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		if ("start".equals(command)) {
			antListeners = new HashMap<String,AntListener>(); 

			AntListener listener = new AdvancedSpeedCadenceListener();
			antListeners.put(listener.getName(), listener);
			listener = new CadenceListener();
			antListeners.put(listener.getName(), listener);
			listener = new HeartRateListener();
			antListeners.put(listener.getName(), listener);
			antDevice = new Ant(antListeners);
			// FIXME will need to set all ids to zero first
			antDevice.open();
			status.setText("Attempting pairing...");
			// MessageBus.INSTANCE.send(Messages.START, new Double(0));

		} else if ("antusbm".equals(command)) {
			if (antUSBM.isSelected()) {
				userPrefs.setAntUSBM(true);
			} else {
				userPrefs.setAntUSBM(false);

			}

		} else {
			status.setText("Pairing complete...");
			// MessageBus.INSTANCE.send(Messages.STOP, null);
			MessageBus.INSTANCE.unregister();
			hrmID = antDevice.getHRMChannelId();
			hrmIdField.setText("" + hrmID);
			scID = antDevice.getSCChannelId();
			sandcField.setText("" + scID);
			antDevice.close();
			antListeners = null;
		}

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
