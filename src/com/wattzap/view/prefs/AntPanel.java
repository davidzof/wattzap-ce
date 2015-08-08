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
import com.wattzap.model.ant.PowerListener;
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
	private Ant antDevice;

	// device id display
	private JLabel sandcField;
	private JLabel cadenceField;
	private JLabel speedField;
	private JLabel hrmIdField;
	private JLabel powIdField;

	// Device values
	private JLabel sandcLabel;
	private JLabel speedLabel;
	private JLabel hrm;
	private JLabel cadenceLabel;
	private JLabel powLabel;
	private JLabel status;

	private int scID;
	private int hrmID;
	private int speedID;
	private int cadenceID;
	private int powerID;

	// Checkboxen
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
	private final JLabel hrmIdLabel;
	private final JLabel powIdLabel;

	private UserPreferences userPrefs = UserPreferences.INSTANCE;
	private HashMap<String, AntListener> antListeners;

	public AntPanel() {
		super();
		MigLayout layout = new MigLayout();
		setLayout(layout);

		// speed and cadence sensor
		sCIDlabel = new JLabel();
		sandcField = new JLabel();
		scID = userPrefs.getSCId();
		sandcField.setText(": " + scID);

		sandcLabel = new JLabel();
		sandcLabel.setText("0 km/h");
		scCheckBox = new JCheckBox();
		add(sCIDlabel);
		add(sandcField);
		add(sandcLabel, "w 80!");
		add(scCheckBox, "gapleft 30, wrap");

		if (userPrefs.getSCId() > 0) {
			scCheckBox.setVisible(true);
			scCheckBox.setSelected(true);
		} else {
			scCheckBox.setVisible(false);
		}
		scCheckBox.addItemListener(this);

		// Cadence Sensor
		cadIdLabel = new JLabel();
		cadenceField = new JLabel();
		cadenceID = userPrefs.getCadenceId();
		cadenceField.setText(": " + cadenceID); // cadence ID
		cadenceLabel = new JLabel();
		cadenceLabel.setText("0 rpm");
		cadCheckBox = new JCheckBox();

		if (userPrefs.getCadenceId() > 0) {
			cadCheckBox.setVisible(true);
			cadCheckBox.setSelected(true);
		} else {
			cadCheckBox.setVisible(false);
		}
		add(cadIdLabel);
		add(cadenceField);
		add(cadenceLabel, "w 80!");
		add(cadCheckBox, "gapleft 30, wrap");
		cadCheckBox.addItemListener(this);

		// Speed Sensor
		speedIdLabel = new JLabel();

		speedField = new JLabel();

		speedID = userPrefs.getSpeedId();
		speedField.setText(": " + speedID); // speed Sensor
		speedLabel = new JLabel();
		speedLabel.setText("0 km/h");
		speedCheckBox = new JCheckBox();
		add(speedIdLabel);
		add(speedField);
		add(speedLabel, "w 80!");
		add(speedCheckBox, "gapleft 30, wrap");
		if (userPrefs.getSpeedId() > 0) {
			speedCheckBox.setVisible(true);
			speedCheckBox.setSelected(true);
		} else {
			speedCheckBox.setVisible(false);
		}
		speedCheckBox.addItemListener(this);

		// Power Meter
		powIdLabel = new JLabel();
		powIdField = new JLabel();
		powerID = userPrefs.getPowerId();
		powIdField.setText(": " + powerID); // cadence ID
		powLabel = new JLabel();
		powLabel.setText("0 watts");
		powCheckBox = new JCheckBox();
		add(powIdLabel);
		add(powIdField);
		add(powLabel, "w 80!");
		add(powCheckBox, "gapleft 30, wrap");
		
		if (userPrefs.getPowerId() > 0) {
			powCheckBox.setVisible(true);
			powCheckBox.setSelected(true);
		} else {
			powCheckBox.setVisible(false);
		}
		powCheckBox.addItemListener(this);
		
		// Heart Rate Strap
		hrmIdLabel = new JLabel();
		hrmIdField = new JLabel();
		hrmID = userPrefs.getHRMId();
		hrmIdField.setText(": " + hrmID);
		hrmCheckBox = new JCheckBox();
		hrm = new JLabel();
		hrm.setText("0 bpm");

		add(hrmIdLabel);
		add(hrmIdField);
		add(hrm);
		add(hrmCheckBox, "gapleft 30, wrap");
		
		if (userPrefs.getHRMId() > 0) {
			hrmCheckBox.setVisible(true);
			hrmCheckBox.setSelected(true);
		} else {
			hrmCheckBox.setVisible(false);
		}
		hrmCheckBox.addItemListener(this);
		
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

		MessageBus.INSTANCE.register(Messages.SPEED, this);
		MessageBus.INSTANCE.register(Messages.CADENCE, this);
		MessageBus.INSTANCE.register(Messages.HEARTRATE, this);
		MessageBus.INSTANCE.register(Messages.LOCALE, this);

		setText();
	}

	@Override
	public void callback(Messages message, Object o) {

		switch (message) {
		case HEARTRATE:
			int heartRate = (Integer) o;
			int hr = heartRate;
			if (hr != -1) {
				hrm.setText(Integer.toString(hr) + " bpm");
			}
			break;
		case SPEED:
			Telemetry t = (Telemetry) o;
			if (userPrefs.isMetric()) {
				speedLabel.setText(String.format("%.1f", t.getSpeedKMH())
						+ " km/h");
				sandcLabel.setText(String.format("%.1f", t.getSpeedKMH())
						+ " km/h");
			} else {
				speedLabel.setText(String.format("%.1f", t.getSpeedMPH())
						+ " mph");
				sandcLabel.setText(String.format("%.1f", t.getSpeedMPH())
						+ " mph");
			}
			if (t.getPower() != -1) {
				powLabel.setText(t.getPower() + " watts");
			}

			break;
		case CADENCE:
			int cadence = (Integer) o;
			cadenceLabel.setText(Integer.toString(cadence) + " rpm");

			break;
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		Object source = e.getItemSelectable();
		if (source == scCheckBox) {
			if (scCheckBox.isSelected()) {
				cadCheckBox.setSelected(false);
				speedCheckBox.setSelected(false);
			}
		} else if (source == cadCheckBox) {
			if (cadCheckBox.isSelected()) {
				scCheckBox.setSelected(false);
			}
		} else if (source == speedCheckBox) {
			if (speedCheckBox.isSelected()) {
				scCheckBox.setSelected(false);
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
			listener = new PowerListener();
			antListeners.put(listener.getName(), listener);
			antDevice = new Ant(antListeners);
			// FIXME will need to set all ids to zero first
			
			scCheckBox.setVisible(false);
			scCheckBox.setSelected(false);
			hrmCheckBox.setVisible(false);
			hrmCheckBox.setSelected(false);
			sandcField.setText(": 0");
			hrmIdField.setText(": 0");
			
			antDevice.open();
			status.setText("Attempting pairing...");

		} else {
			status.setText("Pairing complete...");
			MessageBus.INSTANCE.unregister();
			hrmID = antDevice.getChannelId(HeartRateListener.name);
			if (hrmID > 0) {
				hrmCheckBox.setVisible(true);
			}
			hrmIdField.setText(": " + hrmID);

			scID = antDevice.getChannelId(AdvancedSpeedCadenceListener.name);
			if (scID > 0) {
				scCheckBox.setVisible(true);
			}
			sandcField.setText(": " + scID);

			cadenceID = antDevice.getChannelId(CadenceListener.name);
			if (cadenceID > 0) {
				cadCheckBox.setVisible(true);
			}
			cadenceField.setText(": " + cadenceID);

			speedID = antDevice.getChannelId(SpeedListener.name);
			if (speedID > 0) {
				speedCheckBox.setVisible(true);
			}
			speedField.setText(": " + speedID);

			powerID = antDevice.getChannelId(PowerListener.name);
			if (powerID > 0) {
				powCheckBox.setVisible(true);
			}
			powIdField.setText(": " + powerID);

			antDevice.close();
			antListeners = null;
		}

	}

	private void setText() {
		sCIDlabel.setText("Speed and Cadence ID");
		cadIdLabel.setText("Cadence ID");
		speedIdLabel.setText("Speed ID");
		powIdLabel.setText("Power ID");
		hrmIdLabel.setText("HRM Id");
	}

	public void close() {
		status.setText("");
		speedLabel.setText("");
		hrm.setText("");
	}

	public int getSCId() {
		if (scCheckBox.isSelected()) {
			return scID;
		}
		return 0;
	}

	public int getHRMId() {
		if (hrmCheckBox.isSelected()) {
			return hrmID;
		}
		return 0;
	}

	public int getSpeedId() {
		if (speedCheckBox.isSelected()) {
			return speedID;
		}
		return 0;
	}

	public int getCadenceId() {
		if (cadCheckBox.isSelected()) {
			return cadenceID;
		}
		return 0;
	}

	public int getPwrId() {
		if (powCheckBox.isSelected()) {
			return powerID;
		}
		return 0;
	}
}
