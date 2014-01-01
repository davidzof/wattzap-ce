/**
 * 
 */
package com.omniscient.log4jcontrib.swingappender.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

/**
 * Creates a UI to display log messages from a SwingAppender
 * 
 * @author pshah
 * 
 */
public class SwingAppenderUI {
	// UI attributes
	private JFrame jframe;
	private JButton startPause; // button for start/pause - toggles
	private JButton stop; // stop button
	private JButton clear; // button to clear the text area
	private JButton search; // search button
	private JTextField searchField; // search field
	private JPanel buttonsPanel; // panel to hold all buttons
	private JTextPane logMessagesDisp; // display area
	private JScrollPane scrollPane;
	// buffer to hold log statements when the UI is set to PAUSE
	private List logBuffer;
	// flag to indicate if we should display new log events
	private int appState;

	/* Constants */
	public static final String STYLE_REGULAR = "regular";
	public static final String STYLE_HIGHLIGHTED = "highlighted";
	public static final String START = "start";
	public static final String PAUSE = "pause";
	public static final String STOP = "stop";
	public static final String CLEAR = "clear";
	public static final int STARTED = 0;
	public static final int PAUSED = 1;
	public static final int STOPPED = 2;

	/**
	 * An instance for SwingAppenderUI class. This holds the Singleton.
	 */
	private static SwingAppenderUI instance;

	/**
	 * Method to get an instance of the this class. This method ensures that
	 * SwingAppenderUI is a Singleton using a doule checked locking mechanism.
	 * 
	 * @return An instance of SwingAppenderUI
	 */
	public static SwingAppenderUI getInstance() {
		if (instance == null) {
			synchronized (SwingAppenderUI.class) {
				if (instance == null) {
					instance = new SwingAppenderUI();
				}
			}
		}
		return instance;
	}

	/**
	 * Private constructer to ensure that this object cannot e instantiated from
	 * outside this class.
	 */
	private SwingAppenderUI() {
		// set internal attributes
		logBuffer = new ArrayList();
		appState = STARTED;

		// create main window
		jframe = new JFrame();

		jframe.setTitle("WattzAp Debug");
		ImageIcon img = new ImageIcon("icons/preferences.jpg");
		jframe.setIconImage(img.getImage());

		// initialize buttons
		initButtonsPanel();

		// create text area to hold the log messages
		initMessageDispArea();

		// add components to the contentPane
		jframe.getContentPane().add(BorderLayout.NORTH, buttonsPanel);
		jframe.getContentPane().add(BorderLayout.CENTER, scrollPane);
		jframe.setSize(800, 600);
		jframe.setVisible(true);
	}

	/**
	 * Displays the log in the text area unless dispMsg is set to false in which
	 * case it adds the log to a buffer. When dispMsg becomes true, the buffer
	 * is first flushed and it's contents are displayed in the text area.
	 * 
	 * @param log
	 *            The log message to be displayed in the text area
	 */
	public void doLog(String log) {
		if (appState == STARTED) {
			try {
				StyledDocument sDoc = logMessagesDisp.getStyledDocument();
				if (!logBuffer.isEmpty()) {
					Iterator iter = logBuffer.iterator();
					while (iter.hasNext()) {
						sDoc.insertString(0, (String) iter.next(),
								sDoc.getStyle(STYLE_REGULAR));
						iter.remove();
					}
				}
				sDoc.insertString(0, log, sDoc.getStyle(STYLE_REGULAR));
			} catch (BadLocationException ble) {
				System.err.println("Bad Location Exception : "
						+ ble.getMessage());
			}
		} else if (appState == PAUSED) {
			logBuffer.add(log);
		}
	}

	/**
	 * creates a panel to hold the buttons
	 */
	private void initButtonsPanel() {
		// TODO: Add clear button to clear the log statements.
		buttonsPanel = new JPanel();
		startPause = new JButton(PAUSE);
		startPause.addActionListener(new StartPauseActionListener());
		stop = new JButton(STOP);
		stop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				appState = STOPPED;
				startPause.setText(START);
			}
		});
		clear = new JButton(CLEAR);
		clear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				logMessagesDisp.setText("");
			}
		});

		searchField = new JTextField(25);
		search = new JButton("Search");
		search.addActionListener(new SearchActionListener());
		buttonsPanel.add(startPause);
		buttonsPanel.add(stop);
		buttonsPanel.add(clear);
		buttonsPanel.add(searchField);
		buttonsPanel.add(search);

	}

	/**
	 * Creates a scrollable text area
	 */
	private void initMessageDispArea() {
		logMessagesDisp = new JTextPane();
		scrollPane = new JScrollPane(logMessagesDisp);
		scrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		// add styles
		StyledDocument sDoc = logMessagesDisp.getStyledDocument();
		Style def = StyleContext.getDefaultStyleContext().getStyle(
				StyleContext.DEFAULT_STYLE);
		Style s1 = sDoc.addStyle(STYLE_REGULAR, def);
		StyleConstants.setFontFamily(def, "SansSerif");

		Style s2 = sDoc.addStyle(STYLE_HIGHLIGHTED, s1);
		StyleConstants.setBackground(s2, Color.BLUE);

	}

	/**************** inner classes *************************/

	/**
	 * Accepts and responds to action events generated by the startPause button.
	 */
	class StartPauseActionListener implements ActionListener {
		/**
		 * Toggles the value of the startPause button. Also toggles the value of
		 * dispMsg.
		 * 
		 * @param evt
		 *            The action event
		 */
		public void actionPerformed(ActionEvent evt) {
			JButton srcButton = (JButton) evt.getSource();
			if (srcButton.getText().equals(START)) {
				srcButton.setText(PAUSE);
				appState = STARTED;
			} else if (srcButton.getText().equals(PAUSE)) {
				appState = PAUSED;
				srcButton.setText(START);
			}
		}
	}

	class SearchActionListener implements ActionListener {

		public void actionPerformed(ActionEvent evt) {
			JButton srcButton = (JButton) evt.getSource();
			if (!"Search".equals(srcButton.getText())) {
				return;
			}
			System.err.println("Highlighting search results");
			String searchTerm = searchField.getText();
			String allLogText = logMessagesDisp.getText();
			int startIndex = 0;
			int selectionIndex = -1;
			Highlighter hLighter = logMessagesDisp.getHighlighter();
			// clear all previous highlightes
			hLighter.removeAllHighlights();
			DefaultHighlighter.DefaultHighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(
					Color.BLUE);
			while ((selectionIndex = allLogText.indexOf(searchTerm, startIndex)) != -1) {
				startIndex = selectionIndex + searchTerm.length();
				try {
					int newLines = getNumberOfNewLinesTillSelectionIndex(
							allLogText, selectionIndex);
					hLighter.addHighlight(selectionIndex - newLines,
							(selectionIndex + searchTerm.length() - newLines),
							highlightPainter);
				} catch (BadLocationException ble) {
					System.err.println("Bad Location Exception: "
							+ ble.getMessage());
				}
			}
		}

		private int getNumberOfNewLinesTillSelectionIndex(String allLogText,
				int selectionIndex) {
			int numberOfNewlines = 0;
			int pos = 0;
			while ((pos = allLogText.indexOf("\n", pos)) != -1
					&& pos <= selectionIndex) {
				numberOfNewlines++;
				pos++;
			}
			return numberOfNewlines;
		}

	}

	public void close() {
		// clean up code for UI goes here.
		jframe.setVisible(false);
	}
}