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
import com.wattzap.model.ant.HeartRateListener;
import com.wattzap.model.dto.Telemetry;

/**
 * Pairs ANT devices
 * 
 * @author David George
 * @date 25th August 2013
 */
public class AntPanel extends JPanel implements ActionListener, MessageCallback {
	private static final long serialVersionUID = 1L;
	private JTextField sandcField;
	private JTextField hrmIdField;
	private JLabel speedLabel;
	private JLabel hrm;
	private JLabel status;
	private Ant antDevice;
	private int hrmID;
	private int scID;
	JCheckBox antUSBM;

	private UserPreferences userPrefs = UserPreferences.INSTANCE;
	AdvancedSpeedCadenceListener scListener;
	HeartRateListener hrListener;

	public AntPanel() {
		super();
		MigLayout layout = new MigLayout();
		setLayout(layout);

		scListener = new AdvancedSpeedCadenceListener();
		hrListener = new HeartRateListener();

		JLabel label1 = new JLabel();
		label1.setText("Speed and Cadence ID");
		sandcField = new JTextField(10);
		sandcField.setText("" + userPrefs.getSCId());
		add(label1);
		add(sandcField, "wrap");

		JLabel label3 = new JLabel();
		label3.setText(userPrefs.messages.getString("speed"));
		speedLabel = new JLabel();
		speedLabel.setText("0 km/h");
		add(label3);
		add(speedLabel, "wrap");

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
		MessageBus.INSTANCE.register(Messages.HEARTRATE, this);
	}

	@Override
	public void callback(Messages message, Object o) {
		Telemetry t = (Telemetry) o;
		switch(message) {
		case HEARTRATE:
			int hr = t.getHeartRate();
			System.out.println("hr " + hr);
			if (hr != -1) {
				hrm.setText(Integer.toString(hr) + " bpm");
			}
			break;
		case SPEEDCADENCE:
			speedLabel.setText(String.format("%.1f", t.getSpeed()) + " km/h");
			break;
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		if ("start".equals(command)) {
			antDevice = new Ant(scListener, hrListener);
			antDevice.open(0, 0); // 0 is wildcard id
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
