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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.wattzap.controller.MessageBus;
import com.wattzap.controller.MessageCallback;
import com.wattzap.controller.Messages;
import com.wattzap.model.RouteReader;
import com.wattzap.model.UserPreferences;

/**
 * Control button panel at bottom of main screen to start/stop routes
 * 
 * (c) 2014 David George / Wattzap.com
 * 
 * @author David George
 * @date 1 January 2014
 */
public class ControlPanel extends JPanel implements ActionListener,
		ChangeListener, MessageCallback {
	private static final long serialVersionUID = 1L;
	private final static UserPreferences userPrefs = UserPreferences.INSTANCE;
	private JSlider startPosition;
	private int start;

	public ControlPanel() {
		JButton stopButton = new JButton(
				UserPreferences.INSTANCE.getString("stop"));
		stopButton.setActionCommand("stop");
		JButton startButton = new JButton(
				UserPreferences.INSTANCE.getString("start"));
		startButton.setActionCommand("start");

		startButton.addActionListener(this);
		stopButton.addActionListener(this);

		setBackground(Color.black);
		add(startButton);
		add(stopButton);

		MessageBus.INSTANCE.register(Messages.GPXLOAD, this);
		MessageBus.INSTANCE.register(Messages.CLOSE, this);

		// Border b = BorderFactory.createLineBorder( Color.black, 10 );
		Border b = BorderFactory.createEmptyBorder(10, 0, 10, 0);
		this.setBorder(b);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if ("start".equals(command)) {
			if (!userPrefs.isAntEnabled() && userPrefs.getMaxPower() == 0) {
				JOptionPane.showMessageDialog(this,
						UserPreferences.INSTANCE
								.getString("ftpWarning"),
						UserPreferences.INSTANCE.getString("warning"),
						JOptionPane.WARNING_MESSAGE);
			}

			MessageBus.INSTANCE.send(Messages.START, new Double(start));
		} else {
			MessageBus.INSTANCE.send(Messages.STOP, null);
		}
	}

	/**
	 * Listen to the slider. As it changes send START POSITION messages to any
	 * listeners.
	 */
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider) e.getSource();
		if (!source.getValueIsAdjusting()) {
			start = (int) source.getValue();
			MessageBus.INSTANCE.send(Messages.STARTPOS, new Double(start));
		}
	}

	@Override
	public void callback(Messages message, Object o) {
		switch (message) {
		case GPXLOAD:
			if (startPosition != null) {
				remove(startPosition);
			}
			startPosition = new JSlider(JSlider.HORIZONTAL, 0, 0, 0);
			startPosition.setBounds(0, 0, 500, 100);

			startPosition.addChangeListener(this);
			startPosition.setPreferredSize(new Dimension(500, 40));
			Font font = new Font("Serif", Font.ITALIC, 14);

			startPosition.setFont(font);
			startPosition.setForeground(Color.ORANGE);
			add(startPosition);

			start = 0;
			startPosition.setValue(0);
			RouteReader routeData = (RouteReader) o;

			double distance = routeData.getDistanceMeters();
			startPosition.setVisible(true);
			startPosition.setMaximum((int) (distance / 1000));

			if (distance > 20000) {
				// more than 20km
				int ticks = (int) distance / 5000;
				ticks = (int) (Math.ceil(ticks / 10d) * 10 );
				startPosition.setMajorTickSpacing(ticks);
				startPosition.setMinorTickSpacing(1);
			} else {
				int ticks = (int) distance / 1000;
				ticks = (int) (Math.ceil(ticks / 10) * 1);
				startPosition.setMajorTickSpacing(ticks);
				startPosition.setMinorTickSpacing(1);
			}

			startPosition.setPaintLabels(true);
			startPosition.setPaintTicks(true);
			startPosition.revalidate();

			break;
		case CLOSE:
			if (startPosition != null) {
				remove(startPosition);
			}
			startPosition = null;
			break;
		}
	}

}
