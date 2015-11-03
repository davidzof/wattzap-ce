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
package com.wattzap.view.graphs;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.wattzap.controller.MessageBus;
import com.wattzap.controller.MessageCallback;
import com.wattzap.controller.Messages;
import com.wattzap.model.UserPreferences;

/**
 * (c) 2014 Wattzap.com
 * 
 * @author David George
 * @date 22 September 2014
 */
public class BucketPanel extends JPanel implements ActionListener, MessageCallback {
	private final DistributionGraph graph;
	private int scale;
	private boolean keepZeroes = false;
	private final UserPreferences userPrefs = UserPreferences.INSTANCE;
	private final JLabel binLabel;
	private final JCheckBox keepZero;

	public BucketPanel(DistributionGraph graph, int scale) {
		this.graph = graph;
		binLabel = new JLabel();
		
		if (scale > 0) {
			int values[] = { 3, 5, 10, 15, 20, 25, 30, 40, 50 };
			add(binLabel);

			@SuppressWarnings("rawtypes")
			JComboBox combo = new JComboBox();
			for (int i = 0; i < values.length; i++) {
				combo.addItem(values[i]);
				if (values[i] == scale) {
					this.scale = scale;
					combo.setSelectedIndex(i);
				}
			}// for
			combo.addActionListener(this);

			setBackground(Color.LIGHT_GRAY);
			this.add(combo);
		}
		keepZero = new JCheckBox(userPrefs
				.getString("zeroValue"));
		keepZero.setSelected(true);
		keepZero.setActionCommand("zeros");
		keepZero.addActionListener(this);

		this.add(keepZero);
		
		doText();
		MessageBus.INSTANCE.register(Messages.LOCALE, this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if ("zeros".equals(e.getActionCommand())) {
			JCheckBox cb = (JCheckBox) e.getSource();

			keepZeroes = cb.isSelected();
		} else {
			@SuppressWarnings("rawtypes")
			JComboBox combo = (JComboBox) e.getSource();
			scale = ((Integer) combo.getSelectedItem()).intValue();

		}
		graph.updateValues(scale, keepZeroes);
	}
	
	/*
	 * Setup button text, makes it easy to update if locale is changed
	 */
	private void doText() {
		binLabel.setText(userPrefs
				.getString("binSize") + ": ");
		keepZero.setText(userPrefs
				.getString("zeroValue"));
		
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

}
