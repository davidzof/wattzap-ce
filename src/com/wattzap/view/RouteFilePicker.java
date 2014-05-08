package com.wattzap.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.wattzap.controller.MessageBus;
import com.wattzap.controller.Messages;
import com.wattzap.model.GPXReader;
import com.wattzap.model.RLVReader;
import com.wattzap.model.RouteReader;
import com.wattzap.model.UserPreferences;

/**
 * (c) 2013 David George / TrainingLoops.com
 * 
 * Speed and Cadence ANT+ processor.
 * 
 * @author David George
 * @date 11 June 2013
 */
public class RouteFilePicker extends JFileChooser implements ActionListener {
	private static final long serialVersionUID = 1L;
	JFrame frame;

	private static Logger logger = LogManager.getLogger("Route File Picker");

	public RouteFilePicker(JFrame panel) {
		super();
		this.frame = panel;

		File cwd = new File(UserPreferences.INSTANCE.getRouteDir());
		setCurrentDirectory(cwd);
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"GPX Files and RLV files (.gpx, .rlv)", "gpx", "rlv");
		setFileFilter(filter);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		logger.debug(command);

		int retVal = showOpenDialog(frame);
		File file = getSelectedFile();
		try {
			if (retVal == JFileChooser.APPROVE_OPTION) {
				UserPreferences.INSTANCE.setRouteDir(file.getParent());
				RouteReader track;
				if (file.getName().endsWith(".gpx")) {
					track = new GPXReader();
				} else {
					track = new RLVReader();
				}
				track.load(file.getAbsolutePath());
				MessageBus.INSTANCE.send(Messages.GPXLOAD, track);
			} else {
				logger.info("Open command cancelled by user.");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(frame,
					ex.getMessage() + " " + file.getAbsolutePath(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/** Listen to the slider. */
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider) e.getSource();
		if (!source.getValueIsAdjusting()) {
			int fps = (int) source.getValue();
		}
	}
}