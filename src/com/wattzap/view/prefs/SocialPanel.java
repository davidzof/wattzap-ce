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

import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import com.wattzap.controller.MessageBus;
import com.wattzap.controller.MessageCallback;
import com.wattzap.controller.Messages;
import com.wattzap.model.UserPreferences;

/* 
 * Preferences for Social Sharing
 * 
 * @author David George
 * @date 1st May 2015
 */
public class SocialPanel extends JPanel implements MessageCallback {
	private static final long serialVersionUID = 1L;
	private final static Font font1 = new Font("Arial", Font.CENTER_BASELINE,
			12);
	private UserPreferences userPrefs = UserPreferences.INSTANCE;
	private JLabel selfLoopsUserLabel;
	private JLabel selfLoopsPassLabel;
	private JTextField selfLoopsUser = new JTextField(20);
	private JTextField selfLoopsPass = new JTextField(20);

	/**
	 * Display turbo panel.
	 */
	public SocialPanel() {
		super();
		// Self Loops
		JLabel selfLoops = new JLabel("SelfLoops.com");
		Font font = new Font("Arial", Font.BOLD, 13);
		selfLoops.setFont(font);
		selfLoopsUserLabel = new JLabel();
		selfLoopsPassLabel = new JLabel();
		selfLoopsUser = new JTextField(20);
		selfLoopsUser.setText(userPrefs.getSLUser());
		selfLoopsPass = new JTextField(20);
		selfLoopsPass.setText(userPrefs.getSLPass());

		// Layout
		MigLayout layout = new MigLayout();
		this.setLayout(layout);
		this.add(selfLoops, "span");
		this.add(selfLoopsUserLabel);
		this.add(selfLoopsUser, "span");
		this.add(selfLoopsPassLabel);
		this.add(selfLoopsPass, "span");

		MessageBus.INSTANCE.register(Messages.LOCALE, this);
		doText();
	}

	/*
	 * Setup menubar text, makes it easy to update menu if locale is changed
	 */
	private void doText() {
		selfLoopsUserLabel.setText(userPrefs.getString("user"));
		selfLoopsPassLabel.setText(userPrefs.getString("pass"));
	}

	/**
	 * Change text language if we get a LOCALE message
	 */
	@Override
	public void callback(Messages message, Object o) {
		switch (message) {
		case LOCALE:
			doText();
			break;
		}
	}

	String getSLUser() {
		return selfLoopsUser.getText();
	}

	String getSLPass() {
		return selfLoopsPass.getText();
	}
}
