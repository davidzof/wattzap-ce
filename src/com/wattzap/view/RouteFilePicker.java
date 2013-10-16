package com.wattzap.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.wattzap.model.GPXData;
import com.wattzap.model.UserPreferences;

public class RouteFilePicker extends JFileChooser implements ActionListener {
	private static final long serialVersionUID = 1L;
	JFrame frame;
	private List<ActionListener> listeners = new ArrayList<ActionListener>();
	private static String lastLocation = null;

	private static Logger logger = LogManager
			.getLogger(Profile.class.getName());

	public RouteFilePicker(JFrame panel) {
		super();
		this.frame = panel;

		File cwd = new File(UserPreferences.INSTANCE.getRouteDir());
		setCurrentDirectory(cwd);
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"GPX Files (.gpx)", "gpx");
		setFileFilter(filter);
	}

	public void actionPerformed(ActionEvent e) {

		logger.debug("Action " + e);

		int retVal = showOpenDialog(frame);

		if (retVal == JFileChooser.APPROVE_OPTION) {
			File file = getSelectedFile();
			UserPreferences.INSTANCE.setRouteDir(file.getParent());
			GPXData track = new GPXData(file.getAbsolutePath());
			notifyListeners(track);
		} else {
			System.out.println("Open command cancelled by user.");
		}
	}

	private void notifyListeners(GPXData track) {
		for (ActionListener l : listeners) {
			l.actionPerformed(new ActionEvent(track, 0, "gpxload"));
		}
	}

	public void addOpenListener(ActionListener l) {
		listeners.add(l);
	}
}