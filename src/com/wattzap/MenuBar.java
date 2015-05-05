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
package com.wattzap;

import java.awt.Toolkit;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import com.wattzap.controller.MenuItem;
import com.wattzap.controller.MessageBus;
import com.wattzap.controller.MessageCallback;
import com.wattzap.controller.Messages;
import com.wattzap.model.UserPreferences;
import com.wattzap.view.AboutPanel;
import com.wattzap.view.prefs.Preferences;

/**
 * Main menu bar
 * 
 * Externalize menu setup to this class. Registers for Locale change messages so
 * we can reinitialize text when language changes.
 * 
 * (c) 2014 David George / Wattzap.com
 * 
 * @author David George
 * @date 25 November 2014
 */
public class MenuBar /* extends JMenuBar */implements MessageCallback {
	private final static UserPreferences userPrefs = UserPreferences.INSTANCE;
	private JMenuItem prefMenuItem;
	private JMenuItem aboutMenuItem;
	public JMenuItem quitMenuItem;
	public JMenu appMenu;
	public JMenu fileMenu;
	public JMenuItem openMenuItem;
	private MenuItem closeMenuItem;

	public MenuBar() {
		appMenu = new JMenu();

		// Preferences
		Preferences preferences = new Preferences();
		prefMenuItem = new JMenuItem();
		prefMenuItem.addActionListener(preferences);

		// About Dialog
		aboutMenuItem = new JMenuItem();
		// NOTE: Sets up timer for unregistered users.
		// TODO - choose between OS and non OS edtion
		AboutPanel about = new AboutPanel();
		aboutMenuItem.addActionListener(about);

		quitMenuItem = new JMenuItem();
		quitMenuItem.setAccelerator(KeyStroke.getKeyStroke('Q', Toolkit
				.getDefaultToolkit().getMenuShortcutKeyMask(), false));

		// Routes
		fileMenu = new JMenu();
		openMenuItem = new JMenuItem();
		fileMenu.add(openMenuItem);
		openMenuItem.setAccelerator(KeyStroke.getKeyStroke('O', Toolkit
				.getDefaultToolkit().getMenuShortcutKeyMask(), false));

		closeMenuItem = new MenuItem(Messages.CLOSE);
		fileMenu.add(closeMenuItem);

		closeMenuItem.setAccelerator(KeyStroke.getKeyStroke('C', Toolkit
				.getDefaultToolkit().getMenuShortcutKeyMask(), false));

		appMenu.add(prefMenuItem);
		appMenu.add(aboutMenuItem);
		appMenu.add(quitMenuItem);

		doText();
		MessageBus.INSTANCE.register(Messages.LOCALE, this);
	}

	/*
	 * Setup menubar text
	 */
	private void doText() {
		appMenu.setText("Application");
		prefMenuItem.setText(userPrefs.messages.getString("preferences"));
		aboutMenuItem.setText(userPrefs.messages.getString("about"));
		quitMenuItem.setText(userPrefs.messages.getString("quit"));
		fileMenu.setText(userPrefs.messages.getString("route"));
		openMenuItem.setText(userPrefs.messages.getString("open"));
		closeMenuItem.setText(userPrefs.messages.getString("close"));
	}

	/**
	 * Change text of menu bar if we get a LOCALE message
	 */
	@Override
	public void callback(Messages message, Object o) {
		switch (message) {
		case LOCALE:
			doText();
			break;
		}
	}
}
