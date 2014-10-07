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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * (c) 2014 Wattzap.com
 * 
 * @author David George
 * @date 22 September 2014
 */
public class SmoothingPanel extends JPanel implements ItemListener {
	private static final long serialVersionUID = 1L;
	private final SCHRGraph graph;

	public SmoothingPanel(SCHRGraph graph) {
		this.graph = graph;

		JLabel label = new JLabel();
		label.setText("Smoothing: ");
		add(label);

		JComboBox combo = new JComboBox();
		combo.addItem("1 sec");
		combo.addItem("2 sec");
		combo.addItem("5 sec");
		combo.addItem("10 sec");
		combo.addItem("20 sec");
		combo.addItem("30 sec");
		combo.addItem("45 sec");
		combo.addItem("60 sec");
		combo.addItem("120 sec");
		combo.addItemListener(this);

		setBackground(Color.LIGHT_GRAY);
		this.add(combo);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			JComboBox combo = (JComboBox) e.getSource();

			String item = (String) combo.getSelectedItem();
			// trim of " sec" part of string and convert to integer
			int smoothing = Integer.parseInt(item.substring(0,
					item.length() - 4));
			graph.updateValues(smoothing);
		}

	}
}
