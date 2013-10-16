package com.wattzap.view.prefs;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

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
public class AntPanel extends JPanel implements ActionListener, ChangeListener {
	private JTextField sandcField;
	private JTextField hrmIdField;
	private JLabel speedLabel;
	private JLabel hrm;
	private JLabel status;
	private Ant antDevice;
	private int hrmID;
	private int scID;

	private UserPreferences userPrefs = UserPreferences.INSTANCE;

	public AntPanel() {
		super();
		MigLayout layout = new MigLayout();
		setLayout(layout);

		JLabel label1 = new JLabel();
		label1.setText("Speed and Cadence ID");
		sandcField = new JTextField(10);
		sandcField.setText("" + userPrefs.getSCId());
		add(label1);
		add(sandcField, "wrap");

		JLabel label3 = new JLabel();
		label3.setText("Speed");
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
		label4.setText("Heart Rate");
		hrm = new JLabel();
		hrm.setText("0 bpm");
		add(label4);
		add(hrm, "wrap");

		JButton pairButton = new JButton("Pair");
		pairButton.setPreferredSize(new Dimension(60, 30));
		pairButton.setActionCommand("pair");
		pairButton.addActionListener(this);
		JButton stopButton = new JButton("Stop");
		stopButton.setPreferredSize(new Dimension(60, 30));
		stopButton.setActionCommand("stop");
		stopButton.addActionListener(this);
		add(pairButton);
		add(stopButton, "wrap");

		status = new JLabel();
		status.setText("");
		add(status, "span");
	}

	public void stateChanged(ChangeEvent e) {
		Telemetry t = (Telemetry) e.getSource();

		int hr = t.getHeartRate();
		if (hr != -1) {
			hrm.setText(Integer.toString(hr) + " bpm");
		}
		speedLabel.setText(Double.toString(t.getSpeed()) + " km/h");

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		AdvancedSpeedCadenceListener scListener = new AdvancedSpeedCadenceListener();
		HeartRateListener hrListener = new HeartRateListener();
		hrListener.addChangeListener(this);
		scListener.addChangeListener(this);

		if ("pair".equals(command)) {
			antDevice = new Ant(scListener, hrListener);
			antDevice.open(0, 0); // 0 is wildcard id
			status.setText("Attempting pairing...");

		} else {
			status.setText("Pairing complete...");
			hrmID = antDevice.getHRMChannelId();
			hrmIdField.setText("" + hrmID);

			scID = antDevice.getSCChannelId();
			sandcField.setText("" + scID);
			antDevice.close();

		}

	}

	public int getSCId() {
		return scID;
	}

	public int getHRMId() {
		return hrmID;
	}
}
