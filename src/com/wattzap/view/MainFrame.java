package com.wattzap.view;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.wattzap.model.UserPreferences;

public class MainFrame extends JFrame implements ActionListener {
	private static final long serialVersionUID = -4597500546349817204L;
	private static long startTime;
	private static final String appName = "WattzAp";

	private Logger logger = LogManager.getLogger(MainFrame.class.getName());

	public MainFrame() {
		super();

		setTitle(appName);
		ImageIcon img = new ImageIcon("icons/turbo.jpg");
		setIconImage(img.getImage());


		
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				checkRegistered();
			}
		});
		startTime = System.currentTimeMillis();

		// Look in AboutPanel to see where we dock 120 minutes from at start
		if (!UserPreferences.INSTANCE.isRegistered() && UserPreferences.INSTANCE.getEvalTime() <= 0) {
			logger.info("Out of time " + UserPreferences.INSTANCE.getEvalTime());
			UserPreferences.INSTANCE.shutDown();
			System.exit(0);
		}
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		logger.info(command);
		if (command.equals("Close")) {
			revalidate();
		} else {
			checkRegistered();
		}
	}

	private void checkRegistered() {
		if (!UserPreferences.INSTANCE.isRegistered()) {
			long endTime = System.currentTimeMillis();
			if (endTime > startTime) {
				endTime -= startTime;

				int minutes = UserPreferences.INSTANCE.getEvalTime();
				// Look in AboutPanel to see where we dock 120 minutes from at start
				minutes += (120 - (endTime / 60000));
				UserPreferences.INSTANCE.setEvalTime(minutes);
				logger.info("Evaluation " + minutes + " minutes left");
			}
		}
		UserPreferences.INSTANCE.shutDown();
		System.exit(0);
	}
}
