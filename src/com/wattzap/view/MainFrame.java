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

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.wattzap.controller.MessageBus;
import com.wattzap.controller.MessageCallback;
import com.wattzap.controller.Messages;
import com.wattzap.model.UserPreferences;

/**
 * Main Window, displays telemetry data and responds to close events
 * 
 * @author David George
 * @date 31 July 2013
 */
public class MainFrame extends JFrame implements ActionListener,
		MessageCallback {
	private static final long serialVersionUID = -4597500546349817204L;
	private static final String appName = "WattzAp";

	private Logger logger = LogManager.getLogger("Main Frame");

	public MainFrame() {
		super();

		setTitle(appName);
		ImageIcon img = new ImageIcon("icons/turbo.jpg");
		setIconImage(img.getImage());

		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				// remember position and size
				Rectangle r = getBounds();
				UserPreferences.INSTANCE.setMainBounds(r);
				UserPreferences.INSTANCE.shutDown();
				System.exit(0);
			}
		});

		MessageBus.INSTANCE.register(Messages.CLOSE, this);
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		logger.info(command);
		// remember position and size
		Rectangle r = this.getBounds();
		UserPreferences.INSTANCE.setMainBounds(r);
		UserPreferences.INSTANCE.shutDown();
		System.exit(0);
	}

	@Override
	public void callback(Messages message, Object o) {
		logger.info(message);
		switch (message) {
		case CLOSE:
			this.revalidate();

			break;
		}
	}
}
