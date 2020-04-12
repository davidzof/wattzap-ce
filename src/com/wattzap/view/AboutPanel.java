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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import net.miginfocom.layout.CC;
import net.miginfocom.swing.MigLayout;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.wattzap.model.UserPreferences;

/*
 * Registration Process View Object.
 * (c) 2013 David George
 */
public class AboutPanel extends JFrame implements ActionListener {
	private static final long serialVersionUID = -7939830514817673972L;
	private JLabel about;
	private JCheckBox debug;
	JTextField regKey;
	UserPreferences userPrefs = UserPreferences.INSTANCE;
	StringBuilder serial = new StringBuilder();
	
	private static final StringBuffer aboutText = new StringBuffer();

	private static Logger logger = LogManager.getLogger("About");

	public AboutPanel() {
		aboutText.append("<html>WattzAp Unified Edition<br/><br/>Version 2.8.0<br/>10th April 2020<p>© 2013-2020 David George<br/>Additional contributions from:<br/>Pir43");
		
		setTitle("About Panel");
		ImageIcon img = new ImageIcon("icons/preferences.jpg");
		setIconImage(img.getImage());
		Container contentPane = getContentPane();
		contentPane.setLayout(new MigLayout());

		setBackground(Color.GRAY);

		int style1 = Font.CENTER_BASELINE;
		Font font = new Font("Arial", style1, 13);

		about = new JLabel();
		about.setFont(font);
		String key = userPrefs.getRegistrationKey();
		if (key == null || key.length() == 0) {
			userPrefs.setRegistrationKey("Community Edition");
		}
		about.setText(aboutText.toString());
		add(about,"alignx right, span");

		debug = new JCheckBox("Enable Debugging");
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
		if ("Enable Debugging".equals(command)) {
			userPrefs.setDebug(debug.isSelected());
		}

		this.invalidate();
		this.validate();
		setVisible(true);
	}
}