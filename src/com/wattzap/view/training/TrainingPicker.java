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
package com.wattzap.view.training;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import au.com.bytecode.opencsv.CSVReader;

import com.wattzap.controller.MessageBus;
import com.wattzap.controller.Messages;
import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.TrainingData;
import com.wattzap.model.dto.TrainingItem;
import com.wattzap.utils.FileName;

public class TrainingPicker extends JFileChooser implements ActionListener {
	private static final long serialVersionUID = 1L;
	JFrame frame;

	public TrainingPicker(JFrame panel) {
		super();
		this.frame = panel;
		File cwd = new File(UserPreferences.INSTANCE.getTrainingDir());
		setCurrentDirectory(cwd);

		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Training Files (.trn)", "trn");
		setFileFilter(filter);
	}

	public void actionPerformed(ActionEvent e) {
		int runningTime = 0;
		int retVal = showOpenDialog(frame);

		if (retVal == JFileChooser.APPROVE_OPTION) {
			File file = getSelectedFile();
			UserPreferences.INSTANCE.setTrainingDir(file.getParent());

			CSVReader reader;
			TrainingData tData = new TrainingData();
			tData.setName(FileName.removeExtension(file.getName()));
			
			try {
				reader = new CSVReader(new FileReader(file));

				String[] nextLine;

				while ((nextLine = reader.readNext()) != null) {
					// nextLine[] is an array of values from the line
					String f1 = nextLine[0];

					if (f1.trim().isEmpty() || f1.trim().startsWith("#")) {
						continue;
					}
					TrainingItem item = new TrainingItem();

					if (f1.indexOf(':') != -1) {
						// minutes seconds
						int minutes = Integer.parseInt(f1.substring(0,
								f1.indexOf(':')));
						int seconds = Integer.parseInt(f1.substring(f1
								.indexOf(':') + 1));
						runningTime += (minutes * 60) + seconds;
					} else {
						runningTime += Integer.parseInt(f1) * 60;

					}
					item.setTime(runningTime);

					item.setDescription(nextLine[1]);
					if (!nextLine[2].isEmpty()) {
						item.setHr(nextLine[2].trim());
						tData.setHr(true);
					}
					if (!nextLine[3].isEmpty()) {
						item.setPower(nextLine[3]);
						tData.setPwr(true);
					}
					if (!nextLine[4].isEmpty()) {
						item.setCadence(nextLine[4]);
						tData.setCdc(true);
					}

					tData.addItem(item);
				}// while
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(frame, ex.getMessage() + " ",
						"Error", JOptionPane.ERROR_MESSAGE);
			}

			MessageBus.INSTANCE.send(Messages.TRAINING, tData);
		} else {
			// System.out.println("Open command cancelled by user.");
		}
	}
}