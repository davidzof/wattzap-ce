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

/**
 * (c) 2014 Wattzap.com
 * 
 * @author David George
 * @date 22 September 2014
 */
public class BucketPanel extends JPanel implements ActionListener {
	private final DistributionGraph graph;
	private int scale;
	private boolean keepZeroes = false;

	public BucketPanel(DistributionGraph graph, int scale) {
		this.graph = graph;

		if (scale > 0) {
			int values[] = { 5, 10, 15, 20, 25, 30, 40, 50 };
			JLabel label = new JLabel();
			label.setText("Bucket Size: ");
			add(label);

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
		JCheckBox keepZero = new JCheckBox("Keep Zero Values");
		keepZero.setSelected(true);
		keepZero.setActionCommand("zeros");
		keepZero.addActionListener(this);

		this.add(keepZero);
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
}
