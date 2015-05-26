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
package com.wattzap;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
import com.wattzap.model.UserPreferences;
import com.wattzap.model.ant.AdvancedSpeedCadenceListener;
import com.wattzap.model.ant.Ant;
import com.wattzap.model.ant.AntListener;
import com.wattzap.model.ant.CadenceListener;
import com.wattzap.model.ant.DummySpeedCadenceListener;
import com.wattzap.model.ant.HeartRateListener;
import com.wattzap.model.ant.PowerListener;
import com.wattzap.model.ant.SpeedListener;
import com.wattzap.view.AntOdometer;
import com.wattzap.view.ControlPanel;
import com.wattzap.view.MainFrame;
import com.wattzap.view.Map;
import com.wattzap.view.Odometer;
import com.wattzap.view.Profile;
import com.wattzap.view.VideoPlayer;
import net.miginfocom.swing.MigLayout;
import org.apache.log4j.*;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;

/**
 * Main entry point
 * <p>
 * (c) 2013 David George / Wattzap.com
 *
 * @author David George
 * @date 11 June 2013
 */
public class Main implements Runnable {
    private static Logger logger = LogManager.getLogger("Main");
    private final static UserPreferences userPrefs = UserPreferences.INSTANCE;

    public static void main(String[] args) {
        // Debug
        Level level = setLogLevel();
        // hard coded for debugging, not important, normally libvlc is found on
        // lib path
        NativeLibrary.addSearchPath("libvlc", ".");
        // configure the appender
        String PATTERN = "%r [%t] %p %c %x %m%n";
        String logFile = userPrefs.getWD() + "/logfile.txt";
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
        if (userPrefs.isDebug()) {
            SwingAppender appender = new SwingAppender(); // create appender
            // configure the appender

            appender.setLayout(new PatternLayout(PATTERN));
            appender.setThreshold(level);
            appender.activateOptions();
            // add appender to any Logger (here is root)
            Logger.getRootLogger().addAppender(appender);
        }

        logger.info("Setting log level => " + level.toString());

        logger.info("Database Version " + userPrefs.getDBVersion());
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
            userPrefs.shutDown();
        }
    }

    @Override
    public void run() {
        MainFrame frame = new MainFrame();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        // frame.setSize(screenSize.width, screenSize.height-100);
        frame.setBounds(userPrefs.getMainBounds());

        // Must be declared above Odometer
        // AdvancedSpeedCadenceListener scListener = null;
        JPanel odo = null;
        try {
            HashMap<String, AntListener> antListeners = new HashMap<String, AntListener>();
            int id = userPrefs.getSCId();
            if (id > 0) {
                AntListener listener = new AdvancedSpeedCadenceListener();
                antListeners.put(listener.getName(), listener);
            }

            id = userPrefs.getSpeedId();
            if (id > 0) {
                AntListener listener = new SpeedListener();
                antListeners.put(listener.getName(), listener);
            }

            id = userPrefs.getCadenceId();
            if (id > 0) {
                AntListener listener = new CadenceListener();
                antListeners.put(listener.getName(), listener);
            }

            id = userPrefs.getHRMId();
            if (id > 0) {
                AntListener listener = new HeartRateListener();
                antListeners.put(listener.getName(), listener);
            }

            id = userPrefs.getPowerId();
            if (id > 0) {
                AntListener listener = new PowerListener();
                antListeners.put(listener.getName(), listener);
            }
            new Ant(antListeners).register();
            odo = new AntOdometer();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "ANT+ " + e.getMessage(),
                    userPrefs.messages.getString("warning"),
                    JOptionPane.WARNING_MESSAGE);
            logger.error("ANT+ " + e.getMessage());
            new DummySpeedCadenceListener();
            userPrefs.setAntEnabled(false);
            odo = new Odometer();
        }

        // Performs an isregister check, be careful if we move below AboutPanel
        VideoPlayer videoPlayer = new VideoPlayer(frame, odo);
        try {
            videoPlayer.init();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, e.getMessage(),
                    userPrefs.messages.getString("warning"),
                    JOptionPane.INFORMATION_MESSAGE);
            logger.info(e.getMessage());

            videoPlayer = null;
        }

        MigLayout layout = new MigLayout("center", "[]10px[]", "");
        Container contentPane = frame.getContentPane();
        contentPane.setBackground(Color.BLACK);
        contentPane.setLayout(layout);

        // create view
        new Map(frame);
        Profile profile = new Profile(screenSize);
        profile.setVisible(false);

        // Menu Bar
		new MenuBar(frame);

        frame.add(profile, "cell 0 1, grow");
        // by default add to telemetry frame
        frame.add(odo, "cell 0 2, grow");

        ControlPanel cp = new ControlPanel();
        frame.add(cp, "cell 0 3");

        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        // frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);

        // show Gauge view (javafx version)
//		JavaFxGaugesView gaugesView = new JavaFxGaugesView();
//		gaugesView.show();

        // show Gauge view (swing version)
        JavaSwingGaugesView gaugesView = new JavaSwingGaugesView();
        gaugesView.show();
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
