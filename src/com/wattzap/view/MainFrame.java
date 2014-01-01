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

		if (!UserPreferences.INSTANCE.isRegistered()
				&& UserPreferences.INSTANCE.getEvalTime() <= 0) {
			logger.info("Out of time " + UserPreferences.INSTANCE.getEvalTime());
			UserPreferences.INSTANCE.shutDown();
			System.exit(0);
		}

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
