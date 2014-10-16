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
package com.wattzap.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jfree.data.xy.XYSeries;

import com.wattzap.controller.DistributionAccessor;
import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.Telemetry;
import com.wattzap.model.dto.TrainingItem;
import com.wattzap.model.dto.WorkoutData;
import com.wattzap.utils.ActivityReader;
import com.wattzap.view.graphs.GenericScatterGraph;
import com.wattzap.view.graphs.DistributionGraph;
import com.wattzap.view.graphs.MMPGraph;
import com.wattzap.view.graphs.SCHRGraph;

/**
 * List of workouts stored in the system
 * 
 * @author David George (c) Copyright 17 January 2014
 * @date 17 April 2014
 */
public class Workouts extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private List<WorkoutData> workoutList;
	private List<Integer> selectedRows;
	private final JTable table;
	private final JFrame frame;

	ArrayList<Telemetry> telemetry[] = null;
	private final UserPreferences userPrefs = UserPreferences.INSTANCE;

	private static Logger logger = LogManager.getLogger("Workouts");

	private final static String scGraph = "SCG";
	public final static String hrWattsGraph = "PWG";

	// summary graphs
	private final static String mmpGraph = "MMP";
	private final static String schrGraph = "SCHR";
	// distribution graphs
	private final static String pdGraph = "PDG";
	private final static String cdGraph = "CDG";
	private final static String hrdGraph = "HRDG";
	private final static String tlGraph = "TLDG";
	private final static String tlhrGraph = "TLHRDG";
	public final static String importer = "IMP";

	private final static String[] columnNames = { "Date", "Time", "Source",
			"QPower", "Max HR", "Ave HR", "Max Cadence", "Ave Cadence",
			"5Sec W/kg", "1Min W/kg", "5Min W/kg", "20Min W/kg", "Load",
			"Stress" };

	public Workouts() {
		super(new GridLayout(1, 0));
		selectedRows = null;

		DefaultTableModel model = new DefaultTableModel(columnNames, 0);
		loadData(model);
		table = new JTable(model);
		model.fireTableDataChanged();

		table.setPreferredScrollableViewportSize(new Dimension(1000, 400));
		table.setFillsViewportHeight(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.getColumnModel().getColumn(0).setPreferredWidth(130);
		table.getColumnModel().getColumn(1).setPreferredWidth(100);

		table.setRowSelectionAllowed(true);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		table.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {

						ListSelectionModel lsm = (ListSelectionModel) e
								.getSource();

						if (lsm.isSelectionEmpty()) {
							selectedRows = null;
						} else {
							selectedRows = new ArrayList<Integer>();
							// Find out which indexes are selected.
							int minIndex = lsm.getMinSelectionIndex();
							int maxIndex = lsm.getMaxSelectionIndex();
							for (int i = minIndex; i <= maxIndex; i++) {
								if (lsm.isSelectedIndex(i)) {
									selectedRows.add(i);
								}
							}
						}
					}
				});

		// Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(table);

		this.setLayout(new BorderLayout());
		// Add the scroll pane to this panel.
		add(scrollPane, BorderLayout.CENTER);

		WorkoutButtonPanel wBP = new WorkoutButtonPanel(this);
		add(wBP, BorderLayout.PAGE_END);
		// need button for Load, Delete

		// Create and set up the window.
		frame = new JFrame("Wattzap Analyzer - Workout View");
		ImageIcon img = new ImageIcon("icons/turbo.jpg");
		frame.setIconImage(img.getImage());
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		this.setOpaque(true); // content panes must be opaque
		frame.setContentPane(this);

		// Menu
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		JMenu summaryMenu = new JMenu(userPrefs.messages.getString("summary"));
		summaryMenu.setMnemonic(KeyEvent.VK_S);
		summaryMenu.setMargin(new Insets(0, 0, 0, 10));
		menuBar.add(summaryMenu);
		// MMP, hr/cad/pwr graph
		// pwr/time, hr/time, cad, time
		JMenuItem mmpMenuItem = new JMenuItem(
				userPrefs.messages.getString("mmp"));
		mmpMenuItem.setActionCommand(mmpGraph);
		summaryMenu.add(mmpMenuItem);
		mmpMenuItem.addActionListener(this);

		JMenuItem schrMenuItem = new JMenuItem(
				userPrefs.messages.getString("schr"));
		schrMenuItem.setActionCommand(schrGraph);
		summaryMenu.add(schrMenuItem);
		schrMenuItem.addActionListener(this);

		JMenuItem importMenuItem = new JMenuItem(
				userPrefs.messages.getString("import"));
		importMenuItem.setActionCommand(importer);
		summaryMenu.add(importMenuItem);
		importMenuItem.addActionListener(this);

		// 1sec...?
		JMenu fatMenu = new JMenu(userPrefs.messages.getString("fatigue"));
		fatMenu.setMnemonic(KeyEvent.VK_F);
		fatMenu.setMargin(new Insets(0, 0, 0, 10));
		menuBar.add(fatMenu);

		// pwr/cad, pwr/dist, speed/cadence
		JMenu scatMenu = new JMenu(userPrefs.messages.getString("scatter"));
		scatMenu.setMnemonic(KeyEvent.VK_A);
		menuBar.add(scatMenu);
		scatMenu.setMargin(new Insets(0, 0, 0, 10));
		// menuBar.add(new JSeparator());
		JMenuItem cpgMenuItem = new JMenuItem(
				userPrefs.messages.getString("cpg"));
		cpgMenuItem.setActionCommand(scGraph);
		scatMenu.add(cpgMenuItem);
		cpgMenuItem.addActionListener(this);

		JMenuItem powerWattsMenuItem = new JMenuItem(
				userPrefs.messages.getString("poWt"));
		powerWattsMenuItem.setActionCommand(hrWattsGraph);
		scatMenu.add(powerWattsMenuItem);
		powerWattsMenuItem.addActionListener(this);

		// distribution
		JMenu distMenu = new JMenu(userPrefs.messages.getString("distribution"));
		distMenu.setMnemonic(KeyEvent.VK_D);
		menuBar.add(distMenu);
		JMenuItem pdgMenuItem = new JMenuItem(
				userPrefs.messages.getString("power"));
		pdgMenuItem.setActionCommand(pdGraph);
		distMenu.add(pdgMenuItem);
		pdgMenuItem.addActionListener(this);

		JMenuItem cadMenuItem = new JMenuItem(
				userPrefs.messages.getString("cadence"));
		cadMenuItem.setActionCommand(cdGraph);
		distMenu.add(cadMenuItem);
		cadMenuItem.addActionListener(this);

		JMenuItem hrMenuItem = new JMenuItem(
				userPrefs.messages.getString("heartrate"));
		hrMenuItem.setActionCommand(hrdGraph);
		distMenu.add(hrMenuItem);
		hrMenuItem.addActionListener(this);

		JMenuItem tlMenuItem = new JMenuItem(
				userPrefs.messages.getString("trainlevel"));
		tlMenuItem.setActionCommand(tlGraph);
		distMenu.add(tlMenuItem);
		tlMenuItem.addActionListener(this);

		JMenuItem tlhrMenuItem = new JMenuItem(
				userPrefs.messages.getString("trainlevelhr"));
		tlhrMenuItem.setActionCommand(tlhrGraph);
		distMenu.add(tlhrMenuItem);
		tlhrMenuItem.addActionListener(this);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public void setVisible(boolean flag) {
		if (!frame.isVisible()) {
			frame.setVisible(flag);
			super.setVisible(flag);
		}
		frame.toFront();
	}

	private void loadData(DefaultTableModel tableModel) {
		DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		workoutList = UserPreferences.INSTANCE.listWorkouts();

		tableModel.setRowCount(0);
		String[] data = new String[columnNames.length];
		for (WorkoutData workout : workoutList) {
			data[0] = workout.getDateAsString();
			data[1] = timeFormat.format(new Date(workout.getTime()));
			data[2] = workout.getSourceAsString();
			data[3] = "" + workout.getQuadraticPower();
			data[4] = "" + workout.getMaxHR();
			data[5] = "" + workout.getAveHR();
			data[6] = "" + workout.getMaxCadence();
			data[7] = "" + workout.getAveCadence();

			data[8] = String.format("%.2f", workout.getFiveSecondPwr()
					/ workout.getWeight());
			data[9] = String.format("%.2f",
					workout.getOneMinutePwr() / workout.getWeight());
			data[10] = String.format("%.2f", workout.getFiveMinutePwr()
					/ workout.getWeight());
			data[11] = String.format("%.2f", workout.getTwentyMinutePwr()
					/ workout.getWeight());

			// round up
			data[12] = String.format("%.2f", workout.getIntensity() * 100);
			data[13] = "" + workout.getStress();
			tableModel.addRow(data);
		}
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		if (scGraph.equals(command)) {
			CSScatterPlot();
		} else if (hrWattsGraph.equals(command)) {
			HRWattsScatterPlot();
		} else if (mmpGraph.equals(command)) {
			mmpGraph();
		} else if (schrGraph.equals(command)) {
			SCHRGraph();
		} else if (pdGraph.equals(command)) {
			DistributionGraph(new DistributionAccessor() {
				public int getKey(Telemetry t) {
					return getKey(t.getPower());
				}
			}, 15, userPrefs.messages.getString("pdGr"),
					userPrefs.messages.getString("poWtt"));

		} else if (cdGraph.equals(command)) {
			DistributionGraph(new DistributionAccessor() {
				public int getKey(Telemetry t) {
					return getKey(t.getCadence());
				}
			}, 5, userPrefs.messages.getString("cDgr"),
					userPrefs.messages.getString("cDrpm"));
		} else if (hrdGraph.equals(command)) {
			DistributionGraph(new DistributionAccessor() {
				public int getKey(Telemetry t) {
					if (t.getHeartRate() < 30) {
						return -1; // ignore these values
					}

					return getKey(t.getHeartRate());
				}
			}, 10, userPrefs.messages.getString("hrDgr"),
					userPrefs.messages.getString("hrBpm"));

		} else if (tlGraph.equals(command)) {
			// Training Zone Graph
			DistributionGraph(new DistributionAccessor() {
				public int getKey(Telemetry t) {
					if (!keepZeroes && t.getPower() < 5) {
						return -1;
					}
					return TrainingItem.getTrainingLevel(t.getPower());
				}

				public String getValueLabel(int v) {
					return TrainingItem.getTrainingName(v) + " " + v;
				}
			}, 0, "Training Level Distribution Graph", "Training Level");
		} else if (tlhrGraph.equals(command)) {
			// Training Zone Graph
			DistributionGraph(new DistributionAccessor() {
				public int getKey(Telemetry t) {
					if (!keepZeroes && t.getPower() < 5) {
						return -1;
					}
					return TrainingItem.getHRTrainingLevel(t.getHeartRate());
				}

				public String getValueLabel(int v) {
					return TrainingItem.getTrainingName(v) + " " + v;
				}
			}, 0, "Training Level (Heart Rate)", "Training Level");
		} else if (importer.equals(command)) {
			if (!UserPreferences.INSTANCE.isRegistered()
					&& (UserPreferences.INSTANCE.getEvalTime()) <= 0) {
				logger.info("Out of time "
						+ UserPreferences.INSTANCE.getEvalTime());
				JOptionPane.showMessageDialog(this,
						UserPreferences.INSTANCE.messages
								.getString("trial_expired"),
						UserPreferences.INSTANCE.messages.getString("warning"),
						JOptionPane.WARNING_MESSAGE);
				return;
			}

			String workoutDir = UserPreferences.INSTANCE.getUserDataDirectory()
					+ "/Imports/";

			ActivityReader ar = new ActivityReader();

			File dir = new File(workoutDir);
			Set<File> fileTree = new HashSet<File>();
			for (File entry : dir.listFiles()) {
				if (entry.isFile()) {
					try {
						ar.readActivity(entry.getCanonicalPath());
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}

			StringBuilder importedFiles = new StringBuilder();
			if (ar.getImportedFileList().isEmpty()) {
				importedFiles.append("No files to import");
			} else {

				importedFiles.append("Imported:\n\n");
				for (String file : ar.getImportedFileList()) {
					importedFiles.append(file);
					importedFiles.append("\n");
				}// for

				DefaultTableModel tableModel = (DefaultTableModel) table
						.getModel();

				loadData(tableModel);
				// table = new JTable(model);
				tableModel.fireTableDataChanged();

			}
			JOptionPane.showMessageDialog(this, importedFiles.toString(),
					"Import", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	void load() {
		if (selectedRows == null || selectedRows.isEmpty()) {
			return;
		}

		String workoutDir = UserPreferences.INSTANCE.getUserDataDirectory()
				+ "/Workouts/";

		telemetry = new ArrayList[selectedRows.size()];

		int count = 0;
		for (int i : selectedRows) {
			WorkoutData data = workoutList.get(i);
			String fileName = data.getTcxFile();
			try {
				telemetry[count] = ActivityReader.readTelemetry(workoutDir
						+ fileName);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			count++;
		}// for
	}

	public void mmpGraph() {
		if (telemetry == null) {
			JOptionPane.showMessageDialog(this, "No Data",
					"No data to display, load a workout first",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		TreeMap<Integer, Long> powerValues = new TreeMap<Integer, Long>();
		for (int i = 0; i < telemetry.length; i++) {
			Telemetry first = null;
			for (Telemetry t : telemetry[i]) {

				if (first == null) {
					// first time through
					first = t;
				} else {
					int power = t.getPower();
					if (power > 50) {
						if (powerValues.containsKey(power)) {
							long time = powerValues.get(power);
							powerValues.put(power,
									time + (t.getTime() - first.getTime()));
						} else {
							powerValues.put(power,
									t.getTime() - first.getTime());
						}

						first = t;
					}
				}
			}// for
		}// for

		long total = 0;
		XYSeries series = new XYSeries("Mean Maximal Power");
		for (Entry<Integer, Long> entry : powerValues.descendingMap()
				.entrySet()) {
			Integer pwr = entry.getKey();
			total += entry.getValue();
			series.addOrUpdate(total / 1000, (double) pwr);
		}// for

		MMPGraph mmp = new MMPGraph(series);

		JFrame frame = new JFrame("Mean Maximal Power");
		ImageIcon img = new ImageIcon("icons/turbo.jpg");
		frame.setIconImage(img.getImage());
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// Create and set up the content pane.

		mmp.setOpaque(true); // content panes must be opaque
		frame.setContentPane(mmp);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * Cadence / Speed Scatter plot
	 */
	private void CSScatterPlot() {
		if (telemetry == null) {
			JOptionPane.showMessageDialog(this, "No Data",
					"No data to display, load a workout first",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		XYSeries series = new XYSeries("Cadence Power Scatter Plot");

		for (int i = 0; i < telemetry.length; i++) {
			for (Telemetry t : telemetry[i]) {

				if (t.getCadence() > 0 && t.getPower() > 0) {
					series.addOrUpdate(t.getPower(), t.getCadence());
				}
			}// for
		}// for

		GenericScatterGraph mmp = new GenericScatterGraph(series, "Power",
				"Cadence");
		JFrame frame = new JFrame("Cadence Power Scatter Plot");
		ImageIcon img = new ImageIcon("icons/turbo.jpg");
		frame.setIconImage(img.getImage());
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// Create and set up the content pane.

		mmp.setOpaque(true); // content panes must be opaque
		frame.setContentPane(mmp);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public void HRWattsScatterPlot() {
		if (telemetry == null) {
			JOptionPane.showMessageDialog(this, "No Data",
					"No data to display, load a workout first",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		XYSeries series = new XYSeries("Heart Rate / Watts Scatter Plot");

		for (int i = 0; i < telemetry.length; i++) {
			for (Telemetry t : telemetry[i]) {

				if (t.getHeartRate() > 0 && t.getPower() > 0) {
					series.addOrUpdate(t.getPower(), t.getHeartRate());
				}
			}// for
		}// for

		GenericScatterGraph mmp = new GenericScatterGraph(series, "Power",
				"Heartrate");
		JFrame frame = new JFrame("Heart Rate / Power Scatter Plot");
		ImageIcon img = new ImageIcon("icons/turbo.jpg");
		frame.setIconImage(img.getImage());
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// Create and set up the content pane.

		mmp.setOpaque(true); // content panes must be opaque
		frame.setContentPane(mmp);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public void DistributionGraph(DistributionAccessor da, int scale,
			String title, String label) {
		if (telemetry == null) {
			JOptionPane.showMessageDialog(this, "No Data",
					"No data to display, load a workout(s) first",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		DistributionGraph dgGraph = new DistributionGraph(telemetry, da, label,
				scale);
		dgGraph.updateValues(scale, true);

		JFrame frame = new JFrame(title);
		ImageIcon img = new ImageIcon("icons/turbo.jpg");
		frame.setIconImage(img.getImage());
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// Create and set up the content pane.

		dgGraph.setOpaque(true); // content panes must be opaque
		frame.setContentPane(dgGraph);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public void SCHRGraph() {
		if (telemetry == null) {
			JOptionPane.showMessageDialog(this, "No Data",
					"No data to display, load a workout(s) first",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		SCHRGraph pchrGraph = new SCHRGraph(telemetry);
		// show data with smoothing of 1 second
		pchrGraph.updateValues(1);

		JFrame frame = new JFrame("Ride Summary");
		ImageIcon img = new ImageIcon("icons/turbo.jpg");
		frame.setIconImage(img.getImage());
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// Create and set up the content pane.
		pchrGraph.setOpaque(true); // content panes must be opaque
		frame.setContentPane(pchrGraph);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	void delete() {
		if (selectedRows == null && selectedRows.isEmpty()) {
			JOptionPane.showMessageDialog(this, "No Rows",
					"No rows selected for delete", JOptionPane.WARNING_MESSAGE);
			return;
		}

		StringBuilder output = new StringBuilder();
		output.append(UserPreferences.INSTANCE.messages.getString("delMsg")
				+ "\n\n");

		int[] rows = new int[selectedRows.size()];
		Iterator<Integer> row = selectedRows.iterator();
		for (int i = 0; i < selectedRows.size(); i++) {
			rows[i] = row.next().intValue();
			WorkoutData data = workoutList.get(rows[i]);
			output.append(data.getTcxFile() + "\n");
		}// for

		// 0 is Yes
		int deleteData = JOptionPane.showConfirmDialog(this, output,
				"Workout Delete", JOptionPane.YES_NO_OPTION);

		if (deleteData == 0) {
			deleteSelectedRows(rows);

		}
	}

	private void deleteSelectedRows(int[] rows) {
		DefaultTableModel dm = (DefaultTableModel) table.getModel();
		for (int i = rows.length - 1; i >= 0; i--) {
			dm.removeRow(rows[i]);

			String fileName = workoutList.get(rows[i]).getTcxFile();
			userPrefs.deleteWorkout(fileName);

			String path = UserPreferences.INSTANCE.getUserDataDirectory()
					+ "/Workouts/" + fileName;

			File file = new File(path);
			if (file.delete()) {
				// log an error
				logger.error("deleted " + path);
			}
			workoutList.remove(rows[i]);

		}// for
		selectedRows = null;
	}
}