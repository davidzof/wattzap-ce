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
package com.wattzap.view;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.MessageDigest;

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
import com.wattzap.utils.StringXORer;

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
	/*
	 * Strings - encoded. This is some anti-piracy stuff to stop hackers easily
	 * string grepping for the obsfucated About dialog. Won't hold them long.
	 */
	private static final String decodeKey = "CAFEBABE";
	// <html>WattzAp Virtual Turbo Trainer<br/><br/>Version: x.x.x 
	// <br/>Date: xx xxx xxx<br/>(c) xxxx, All rights reserved<br/>
	private static final String blurb = "fykyKC5/FSQ3NTwEMmEWMDEjKWUWMyMsLSQ0ZREuJDE0IDQgfiMwan19JDdtfxQgMTIvKix7YndtcGh1fiMwan0FJzEne2J0djUuZQM0JTAwNXonMG58bSBoZndycHZpYwAqKWIzKyIrNTVlMCQxIDE3IyF+IzBqfQ==";
	// Your software is registered
	private static final String registered = "Gi4zN2IyLSM3Nic3J2ErNmMzIyIrMjYgMSQi";
	// Incorrect registration key
	private static final String incorrectKey = "Ci8lKjAzJyY3YTQgJSgxMTEgMiwtL2IuJjg=";
	// Your software is not registered
	private static final String notRegistered = "Gi4zN2IyLSM3Nic3J2ErNmMvKTFiMyciKjIyIDAkJg==";
	// Error in registration key
	private static final String keyError = "BjM0KjBhKytjMyMiKzI2NyI1LyosYSkgOg==";
	// "Your software has been registered successfully"
	private static final String successMsg = "Gi4zN2IyLSM3Nic3J2EqJDBhJCAnL2I3JiYvNjYkMCAnYTUwISInNjAnMykuOA==";
	// You have %d minutes left to evaluate this software<br/>
	private static final String evalMsg = "Gi4zZSogNCBjZCJlLygsMDckNWUuJCQxYzUpZSc3Iyk2IDIgYjUqLDBhNSokNTUkMSR6JzBufA==";
	// About WattzAp
	private static final String aboutTitle = "AiMpMDZhFSQ3NTwEMg==";
	// Register
	private static final String registerMsg = "ESQhLDE1Jzc=";
	// Debug
	private static final String debugMsg = "ByQkMCU=";
	// "Serial Number: "
	private static final String serialMsg = "ECQ0LCMtYgs2LCQgMHti";

	private static Logger logger = LogManager.getLogger("About");

	public AboutPanel() {
		setTitle(StringXORer.decode(aboutTitle, decodeKey));
		ImageIcon img = new ImageIcon("icons/preferences.jpg");
		setIconImage(img.getImage());
		Container contentPane = getContentPane();
		contentPane.setLayout(new FlowLayout());

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

		serialNo.setText(StringXORer.decode(serialMsg, decodeKey)
				+ serial.toString().toUpperCase());
		add(about);
		add(serialNo);

		message = new JLabel();
		message.setFont(font);

		boolean isRegistered = userPrefs.isRegistered();

		if (isRegistered) {
			String key = userPrefs.getRegistrationKey();
			try {
				if (Registration.register(key, serial.toString())) {
					message.setText(StringXORer.decode(registered, decodeKey));
				} else {
					message.setText(StringXORer.decode(incorrectKey, decodeKey));
					isRegistered = false;
				}

			} catch (Exception e) {
				logger.info(StringXORer.decode(keyError, decodeKey) + " "
						+ e.getLocalizedMessage());
				message.setText(StringXORer.decode(keyError, decodeKey));
				isRegistered = false;
			}

		} else {
			message.setText(StringXORer.decode(notRegistered, decodeKey));
		}

		if (!isRegistered) {
			int minutes = UserPreferences.INSTANCE.getEvalTime();
			about.setText(StringXORer.decode(blurb, decodeKey)
					+ String.format(StringXORer.decode(evalMsg, decodeKey),
							minutes));
		} else {
			about.setText(StringXORer.decode(blurb, decodeKey));
		}

		add(message);

		if (!isRegistered) {
			regKey = new JTextField(45);
			add(regKey);

			JButton registrationButton = new JButton(StringXORer.decode(
					registerMsg, decodeKey));
			registrationButton.setPreferredSize(new Dimension(120, 30));
			registrationButton.setActionCommand(StringXORer.decode(registerMsg,
					decodeKey));
			registrationButton.addActionListener(this);
			add(registrationButton);
		}

		debug = new JCheckBox(StringXORer.decode(debugMsg, decodeKey));
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
		logger.debug(command);
		if (StringXORer.decode(registerMsg, decodeKey).equals(command)) {
			String key = regKey.getText();
			try {
				if (!key.isEmpty()
						&& Registration.register(key, serial.toString().trim())) {

					userPrefs.setRegistrationKey(key);
					message.setText(StringXORer.decode(successMsg, decodeKey));
				} else {
					message.setText(StringXORer.decode(incorrectKey, decodeKey));
				}

			} catch (Exception ex) {
				logger.info(StringXORer.decode(keyError, decodeKey) + " "
						+ ex.getLocalizedMessage());
				message.setText(StringXORer.decode(keyError, decodeKey));

			}
		} else if (StringXORer.decode(debugMsg, decodeKey).equals(command)) {
			userPrefs.setDebug(debug.isSelected());
		}

		if (!userPrefs.isRegistered()) {
			int minutes = UserPreferences.INSTANCE.getEvalTime();
			about.setText(StringXORer.decode(blurb, decodeKey)
					+ String.format(StringXORer.decode(evalMsg, decodeKey),
							minutes));
		} else {
			about.setText(StringXORer.decode(blurb, decodeKey));
		}

		revalidate();
		setVisible(true);
	}
}
