package com.wattzap.view;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.wattzap.model.UserPreferences;
import com.wattzap.utils.Registration;

/*
 * Registration Process View Object.
 * (c) 2013 David George
 */
public class AboutPanel extends JFrame implements ActionListener {
	private static final long serialVersionUID = -7939830514817673972L;
	private JLabel about;
	private JTextField serialNo; // allow cut and paste
	private JLabel message;
	private JCheckBox debug;
	JTextField regKey;
	UserPreferences userPrefs = UserPreferences.INSTANCE;
	StringBuilder serial = new StringBuilder();
	int minutes = UserPreferences.INSTANCE.getEvalTime();

	private static Logger logger = LogManager.getLogger(AboutPanel.class
			.getName());

	public AboutPanel() {
		setTitle("About");
		ImageIcon img = new ImageIcon("icons/preferences.jpg");
		setIconImage(img.getImage());
		Container contentPane = getContentPane();
		contentPane.setLayout(new FlowLayout());
		minutes -= 120; // Dock 2 hours from the run time
		UserPreferences.INSTANCE.setEvalTime(minutes);

		try {
			byte[] bytesOfMessage = userPrefs.getSerial().getBytes("UTF-8");

			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] hash = md.digest(bytesOfMessage);

			for (byte b : hash) {
				serial.append(String.format("%02x", b));
			}
		} catch (Exception e) {
			logger.debug(e.getLocalizedMessage());
		}

		setBackground(Color.GRAY);

		int style1 = Font.CENTER_BASELINE;
		Font font = new Font("Arial", style1, 13);

		about = new JLabel();
		about.setFont(font);

		serialNo = new JTextField();
		serialNo.setEditable(false);
		serialNo.setFont(font);
		serialNo.setBackground(null); // this is the same as a JLabel
		serialNo.setBorder(null); // remove the border

		serialNo.setText("Serial Number: " + serial.toString().toUpperCase());
		add(about);
		add(serialNo);

		message = new JLabel();
		message.setFont(font);

		boolean isRegistered = userPrefs.isRegistered();

		if (isRegistered) {
			String key = userPrefs.getRegistrationKey();
			try {
				if (Registration.register(key, serial.toString())) {
					message.setText("Your software is registered");
				} else {
					message.setText("Incorrect registration key");
					isRegistered = false;
				}

			} catch (Exception e) {
				logger.info("Error in registration key "
						+ e.getLocalizedMessage());
				message.setText("Error in registration key");
				isRegistered = false;
			}

		} else {
			message.setText("Your software is not registered");
		}
		String blurb = "<html>Virtual Turbo<br/><br/>Version: beta.1<br/>Date: 16th September 2013<br/>(c) 2013, All rights reserved<br/>";
		if (!isRegistered) {
			blurb += "You have " + minutes
					+ " minutes left to evaluate this software<br/>";
		}
		about.setText(blurb);
		add(message);

		if (!isRegistered) {
			regKey = new JTextField(45);
			add(regKey);

			JButton registrationButton = new JButton("Register");
			registrationButton.setPreferredSize(new Dimension(120, 30));
			registrationButton.setActionCommand("register");
			registrationButton.addActionListener(this);
			add(registrationButton);
		}

		debug = new JCheckBox("Debug");
		if (userPrefs.isDebug()) {
			debug.setSelected(true);
		} else {
			debug.setSelected(false);
		}
		debug.addActionListener(this);
		add(debug);

		Dimension d = new Dimension(550, 300);
		this.setPreferredSize(d);
		pack();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		System.out.println("command " + command);
		if ("register".equals(command)) {
			String key = regKey.getText();
			try {
				if (!key.isEmpty()
						&& Registration.register(key, serial.toString().trim())) {

					userPrefs.setRegistrationKey(key);
					message.setText("Your software has been registered successfully");
				} else {
					message.setText("Incorrect registration key");
				}

			} catch (Exception ex) {
				logger.info("Error in registration key "
						+ ex.getLocalizedMessage());
				message.setText("Error in registration key, please check");

			}
		} else if ("Debug".equals(command)) {
			userPrefs.setDebug(debug.isSelected());
		}

		revalidate();
		setVisible(true);
	}
}
