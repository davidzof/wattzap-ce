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

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import com.wattzap.controller.MenuItem;
import com.wattzap.controller.MessageBus;
import com.wattzap.controller.MessageCallback;
import com.wattzap.controller.Messages;
import com.wattzap.controller.SocialSharingController;
import com.wattzap.controller.TrainingController;
import com.wattzap.model.UserPreferences;
import com.wattzap.view.AboutPanel;
import com.wattzap.view.MainFrame;
import com.wattzap.view.RouteFilePicker;
import com.wattzap.view.prefs.Preferences;
import com.wattzap.view.training.TrainingDisplay;
import com.wattzap.view.training.TrainingPicker;

/**
 * Main menu bar
 * 
 * Externalize menu setup to this class. Registers for Locale change messages so
 * we can reinitialize text when language changes.
 * 
 * (c) 2014-2015 David George / Wattzap.com
 * 
 * @author David George
 * @date 25 November 2014
 */
public class MenuBar implements MessageCallback {
	private final static UserPreferences userPrefs = UserPreferences.INSTANCE;

	private final JMenu fileMenu;
	private final JMenu trainingMenu;
	private final JMenu socialMenu;
	private final JMenu appMenu;
	// Application Menu Items
	private JMenuItem prefMenuItem;
	private JMenuItem aboutMenuItem;
	private final JMenuItem quitMenuItem;
	// File Menu Items
	private final JMenuItem openMenuItem;
	private MenuItem closeMenuItem;
	// Training
	private final JMenuItem trainMenuItem;
	private final JMenuItem analizeMenuItem;
	private final JMenuItem viewMenuItem;
	private final JMenuItem recoverMenuItem;
	private final JMenuItem saveMenuItem;
	// Social Menu Items
	public JMenuItem selfLoopsUploadItem;

	public MenuBar(MainFrame frame) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		// Application Menu
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
		quitMenuItem.addActionListener(frame);

		appMenu.add(prefMenuItem);
		appMenu.add(aboutMenuItem);
		appMenu.add(quitMenuItem);

		// Routes
		fileMenu = new JMenu();
		openMenuItem = new JMenuItem();
		fileMenu.add(openMenuItem);
		openMenuItem.setAccelerator(KeyStroke.getKeyStroke('O', Toolkit
				.getDefaultToolkit().getMenuShortcutKeyMask(), false));

		RouteFilePicker picker = new RouteFilePicker(frame);
		openMenuItem.addActionListener(picker);

		closeMenuItem = new MenuItem(Messages.CLOSE);
		fileMenu.add(closeMenuItem);

		closeMenuItem.setAccelerator(KeyStroke.getKeyStroke('C', Toolkit
				.getDefaultToolkit().getMenuShortcutKeyMask(), false));

		// Submenu: Training
		trainingMenu = new JMenu();
		// menuBar.add(trainingMenu);
		TrainingDisplay trainingDisplay = new TrainingDisplay(screenSize);
		TrainingController trainingController = new TrainingController(
				trainingDisplay, frame);

		trainMenuItem = new JMenuItem();
		if (userPrefs.isAntEnabled()) {
			trainMenuItem.setActionCommand(TrainingController.open);
			trainingMenu.add(trainMenuItem);

			TrainingPicker tPicker = new TrainingPicker(frame);
			trainMenuItem.addActionListener(tPicker);
		}
		analizeMenuItem = new JMenuItem();
		trainingMenu.add(analizeMenuItem);
		analizeMenuItem.setActionCommand(TrainingController.analyze);

		saveMenuItem = new JMenuItem();
		saveMenuItem.setActionCommand(TrainingController.save);
		trainingMenu.add(saveMenuItem);

		viewMenuItem = new JMenuItem();
		viewMenuItem.setActionCommand(TrainingController.view);
		trainingMenu.add(viewMenuItem);

		recoverMenuItem = new JMenuItem();
		recoverMenuItem.setActionCommand(TrainingController.recover);
		trainingMenu.add(recoverMenuItem);

		analizeMenuItem.addActionListener(trainingController);
		saveMenuItem.addActionListener(trainingController);
		recoverMenuItem.addActionListener(trainingController);
		viewMenuItem.addActionListener(trainingController);

		frame.add(trainingDisplay, "cell 0 0");

		// Social
		socialMenu = new JMenu();
		selfLoopsUploadItem = new JMenuItem();
		socialMenu.add(selfLoopsUploadItem);

		SocialSharingController socialSharing = new SocialSharingController(
				trainingDisplay, frame);
		selfLoopsUploadItem.setActionCommand(SocialSharingController.selfLoopsUpload);
		selfLoopsUploadItem.addActionListener(socialSharing);

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(appMenu);
		menuBar.add(fileMenu);
		menuBar.add(trainingMenu);
		menuBar.add(socialMenu);

		frame.setJMenuBar(menuBar);

		doText();
		MessageBus.INSTANCE.register(Messages.LOCALE, this);
	}

	/*
	 * Setup menubar text, makes it easy to update menu if locale is changed
	 */
	private void doText() {
		appMenu.setText(userPrefs.getString("application"));
		prefMenuItem.setText(userPrefs.getString("preferences"));
		aboutMenuItem.setText(userPrefs.getString("about"));
		quitMenuItem.setText(userPrefs.getString("quit"));
		fileMenu.setText(userPrefs.getString("route"));
		openMenuItem.setText(userPrefs.getString("open"));
		closeMenuItem.setText(userPrefs.getString("close"));
		
		trainingMenu.setText(userPrefs.getString("training"));
		analizeMenuItem.setText(
				userPrefs.getString("analyze"));
		viewMenuItem.setText(
				userPrefs.getString("view"));
		recoverMenuItem.setText(
				userPrefs.getString("recover"));
		saveMenuItem.setText(
				userPrefs.getString("save"));
		//
		socialMenu.setText("Social");
		selfLoopsUploadItem.setText("SelfLoops Upload");
		
		trainMenuItem.setText(
					userPrefs.getString("open"));
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
