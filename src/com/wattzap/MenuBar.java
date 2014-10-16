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

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import com.wattzap.view.MainFrame;
import com.wattzap.view.RouteFilePicker;
import com.wattzap.view.prefs.Preferences;
import com.wattzap.view.training.TrainingDisplay;
import com.wattzap.view.training.TrainingPicker;

/**
 * Main menu bar
 * 
 * (c) 2013 David George / Wattzap.com
 * 
 * @author David George
 * @date 11 June 2013
 */
public class MenuBar extends JMenuBar {
	private static final long serialVersionUID = 8868201635857315675L;
	private MainFrame frame;

	public void init(MainFrame frame) {
		this.frame = frame;

		JMenu fileMenu = new JMenu("File");
		add(fileMenu);
		JMenuItem openMenuItem = new JMenuItem("Open Course");
		fileMenu.add(openMenuItem);

		RouteFilePicker picker = new RouteFilePicker(frame);
		openMenuItem.addActionListener(picker);

		JMenuItem quitMenuItem = new JMenuItem("Quit");
		fileMenu.add(quitMenuItem);
		quitMenuItem.addActionListener(frame);

		JMenu appMenu = new JMenu("Application");
		add(appMenu);

		Preferences preferences = new Preferences();
		JMenuItem prefMenuItem = new JMenuItem("Preferences");
		prefMenuItem.addActionListener(preferences);
		appMenu.add(prefMenuItem);
		JMenuItem aboutMenuItem = new JMenuItem("About");
		appMenu.add(aboutMenuItem);

		frame.setJMenuBar(this);
	}

	public void trainingInit(TrainingDisplay trainingDisplay) {
		// Submenu: Training
		JMenu trainingMenu = new JMenu("Training");
		add(trainingMenu);

		JMenuItem analMenuItem = new JMenuItem("Analyze");
		trainingMenu.add(analMenuItem);

		JMenuItem trainMenuItem = new JMenuItem("Open Training");
		trainingMenu.add(trainMenuItem);

		TrainingPicker tPicker = new TrainingPicker(frame);
		trainMenuItem.addActionListener(tPicker);

		JMenuItem saveMenuItem = new JMenuItem("Save as TCX");
		trainingMenu.add(saveMenuItem);

		JMenuItem csvMenuItem = new JMenuItem("Save as CSV");
		trainingMenu.add(csvMenuItem);

	}

}
