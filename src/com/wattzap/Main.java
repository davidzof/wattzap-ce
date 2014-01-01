package com.wattzap;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.IOException;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.omniscient.log4jcontrib.swingappender.SwingAppender;
import com.sun.jna.NativeLibrary;
import com.wattzap.controller.MenuItem;
import com.wattzap.controller.Messages;
import com.wattzap.model.UserPreferences;
import com.wattzap.model.ant.AdvancedSpeedCadenceListener;
import com.wattzap.model.ant.Ant;
import com.wattzap.model.ant.DummySpeedCadenceListener;
import com.wattzap.model.ant.HeartRateListener;
import com.wattzap.utils.TcxWriter;
import com.wattzap.view.AboutPanel;
import com.wattzap.view.ControlPanel;
import com.wattzap.view.MainFrame;
import com.wattzap.view.Map;
import com.wattzap.view.Odometer;
import com.wattzap.view.Profile;
import com.wattzap.view.RouteFilePicker;
import com.wattzap.view.VideoPlayer;
import com.wattzap.view.prefs.Preferences;
import com.wattzap.view.training.TrainingAnalysis;
import com.wattzap.view.training.TrainingDisplay;
import com.wattzap.view.training.TrainingPicker;

/**
 * (c) 2013 David George / Wattzap.com
 * 
 * 
 * @author David George
 * @date 11 June 2013
 */
public class Main implements Runnable {
	private static Logger logger = LogManager.getLogger("Main");

	public static void main(String[] args) {
		// Debug
		Level level = setLogLevel();
		NativeLibrary.addSearchPath("libvlc", "C:/usr/vlc-2.0.6/");
		// configure the appender
		String PATTERN = "%r [%t] %p %c %x %m%n";
		String logFile = UserPreferences.INSTANCE.getWD() + "/logfile.txt";
		FileAppender fileAppender;
		try {
			fileAppender = new FileAppender(new PatternLayout(PATTERN), logFile);
			fileAppender.setThreshold(level);
			fileAppender.activateOptions();
			// add appender to any Logger (here is root)
			Logger.getRootLogger().addAppender(fileAppender);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} // create appender

		// Turn on Debug window
		if (UserPreferences.INSTANCE.isDebug()) {
			SwingAppender appender = new SwingAppender(); // create appender
			// configure the appender

			appender.setLayout(new PatternLayout(PATTERN));
			appender.setThreshold(level);
			appender.activateOptions();
			// add appender to any Logger (here is root)
			Logger.getRootLogger().addAppender(appender);
		}

		logger.info("Setting log level => " + level.toString());

		logger.info("Database Version "
				+ UserPreferences.INSTANCE.getDBVersion());
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}

			EventQueue.invokeLater(new Main());
		} catch (Exception e) {
			// catch everything and log
			logger.error(e.getLocalizedMessage());
			UserPreferences.INSTANCE.shutDown();
		}
	}

	@Override
	public void run() {
		MainFrame frame = new MainFrame();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		// frame.setSize(screenSize.width, screenSize.height-100);
		frame.setBounds(UserPreferences.INSTANCE.getMainBounds());

		// Performs an isregister check, be careful if we move below AboutPanel
		Odometer odo = new Odometer();
		VideoPlayer videoPlayer = new VideoPlayer(frame, odo);
		try {
			videoPlayer.init();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(frame, e.getMessage(), "Warning",
					JOptionPane.WARNING_MESSAGE);
			logger.info(e.getMessage());

			videoPlayer = null;
		}

		MigLayout layout = new MigLayout("center", "[]10px[]", "");
		Container contentPane = frame.getContentPane();
		contentPane.setBackground(Color.BLACK);
		contentPane.setLayout(layout);

		// create view
		Map map = new Map(frame);
		Profile profile = new Profile(screenSize, frame);
		profile.setVisible(false);

		TrainingDisplay trainingDisplay = new TrainingDisplay(screenSize);

		// Menu Bar
		JMenuBar menuBar = new JMenuBar();

		JMenu appMenu = new JMenu("Application");
		menuBar.add(appMenu);
		// Preferences
		Preferences preferences = new Preferences();
		JMenuItem prefMenuItem = new JMenuItem("Preferences");
		prefMenuItem.addActionListener(preferences);
		appMenu.add(prefMenuItem);

		JMenuItem aboutMenuItem = new JMenuItem("About");
		appMenu.add(aboutMenuItem);
		// NOTE: Sets up timer for unregistered users.
		AboutPanel about = new AboutPanel();
		aboutMenuItem.addActionListener(about);

		JMenuItem quitMenuItem = new JMenuItem("Quit");
		appMenu.add(quitMenuItem);
		quitMenuItem.addActionListener(frame);

		// Route
		JMenu fileMenu = new JMenu("Route");
		menuBar.add(fileMenu);
		JMenuItem openMenuItem = new JMenuItem("Open");
		fileMenu.add(openMenuItem);

		RouteFilePicker picker = new RouteFilePicker(frame);
		openMenuItem.addActionListener(picker);

		MenuItem closeMenuItem = new MenuItem(Messages.CLOSE, "Close");
		fileMenu.add(closeMenuItem);

		closeMenuItem.setAccelerator(KeyStroke.getKeyStroke('C', Toolkit
				.getDefaultToolkit().getMenuShortcutKeyMask(), false));

		// Submenu: Training
		JMenu trainingMenu = new JMenu("Training");
		menuBar.add(trainingMenu);

		// Submenu: training
		JMenuItem trainMenuItem = new JMenuItem("Open");
		trainingMenu.add(trainMenuItem);

		TrainingPicker tPicker = new TrainingPicker(frame);
		trainMenuItem.addActionListener(tPicker);

		JMenuItem analMenuItem = new JMenuItem("Analyze");
		trainingMenu.add(analMenuItem);

		TrainingAnalysis analysis = new TrainingAnalysis(trainingDisplay);
		analMenuItem.addActionListener(analysis);

		JMenuItem recoverMenuItem = new JMenuItem("Recover Data");
		trainingMenu.add(recoverMenuItem);

		JMenuItem saveMenuItem = new JMenuItem("Save as TCX");
		trainingMenu.add(saveMenuItem);

		TcxWriter tcxWriter = new TcxWriter(frame, trainingDisplay);
		saveMenuItem.addActionListener(tcxWriter);
		recoverMenuItem.addActionListener(tcxWriter);
		// JMenuItem csvMenuItem = new JMenuItem("Save as CSV");
		// trainingMenu.add(csvMenuItem);

		frame.setJMenuBar(menuBar);
		// End Menu

		AdvancedSpeedCadenceListener scListener = null;
		try {
			scListener = new AdvancedSpeedCadenceListener();
			Ant antDevice = new Ant(scListener, new HeartRateListener());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(frame, "ANT+ " + e.getMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);
			logger.error("ANT+ " + e.getMessage());
			DummySpeedCadenceListener dummyListener = new DummySpeedCadenceListener();
		}

		frame.add(trainingDisplay, "cell 0 0");
		frame.add(profile, "cell 0 1, grow");
		// by default add to telemetry frame
		frame.add(odo, "cell 0 2, grow");

		ControlPanel cp = new ControlPanel();
		frame.add(cp, "cell 0 3");

		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		// frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);
	}

	private static Level setLogLevel() {
		final String LOGGER_PREFIX = "log4j.logger.";

		for (String propertyName : System.getProperties().stringPropertyNames()) {
			if (propertyName.startsWith(LOGGER_PREFIX)) {
				String loggerName = propertyName.substring(LOGGER_PREFIX
						.length());
				String levelName = System.getProperty(propertyName, "");
				Level level = Level.toLevel(levelName); // defaults to DEBUG
				if (!"".equals(levelName)
						&& !levelName.toUpperCase().equals(level.toString())) {
					logger.error("Skipping unrecognized log4j log level "
							+ levelName + ": -D" + propertyName + "="
							+ levelName);
					continue;
				}
				return level;

			}
		}
		return Level.ERROR;
	}
}
