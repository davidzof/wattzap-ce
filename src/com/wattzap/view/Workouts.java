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
import java.awt.Component;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jfree.data.xy.XYSeries;

import com.wattzap.controller.DistributionAccessor;
import com.wattzap.controller.MessageBus;
import com.wattzap.controller.MessageCallback;
import com.wattzap.controller.Messages;
import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.Telemetry;
import com.wattzap.model.dto.TrainingItem;
import com.wattzap.model.dto.WorkoutData;
import com.wattzap.utils.ActivityReader;
import com.wattzap.utils.TcxWriter;
import com.wattzap.view.graphs.DistributionGraph;
import com.wattzap.view.graphs.GenericScatterGraph;
import com.wattzap.view.graphs.MMPGraph;
import com.wattzap.view.graphs.SCHRGraph;
import com.wattzap.view.training.TrainingAnalysis;

/**
 * List of workouts stored in the system
 * 
 * @author David George (c) 2014,2015
 * @date 17 April 2014
 */
public class Workouts extends JPanel implements ActionListener, MessageCallback {
	private static final long serialVersionUID = 1L;
	private List<WorkoutData> workoutList;
	private List<Integer> selectedRows;
	WorkoutData workoutData = null;
	public final static String IMPORTDIR = "/Imports/";

	private boolean listChanged = true;
	private final JTable table;
	private final JFrame frame;

	ArrayList<Telemetry> telemetry[] = null;
	private final UserPreferences userPrefs = UserPreferences.INSTANCE;

	private static final Logger logger = LogManager.getLogger("Workouts");

	private final static String scGraph = "SCG";
	public final static String hrWattsGraph = "PWG"; // heart rate vs power
	public final static String qaGraph = "QAG"; // quadrant analysis
	public final static double pi = 3.14159;

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

	// menus
	private final JMenu summaryMenu;
	private final JMenuItem mmpMenuItem;
	private final JMenuItem schrMenuItem;
	private final JMenuItem importMenuItem;
	private final JMenu fatMenu;
	private final JMenu scatMenu;
	private final JMenuItem cpgMenuItem;
	private final JMenuItem powerWattsMenuItem;
	private final JMenuItem quadAnalysisMenuItem;
	private final JMenu distMenu;
	private final JMenuItem pdgMenuItem;
	private final JMenuItem cadMenuItem;
	private final JMenuItem hrMenuItem;
	private final JMenuItem tlMenuItem;
	private final JMenuItem tlhrMenuItem;

	private final static String[] columnNames = { "Date", "Time", "Source",
			"QPower", "Max HR", "Ave HR", "Max Cadence", "Ave Cadence",
			"5Sec W/kg", "1Min W/kg", "5Min W/kg", "20Min W/kg", "Load",
			"Stress" };

	public Workouts() {
		super(new GridLayout(1, 0));

		this.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		selectedRows = null;

		DefaultTableModel model = new DefaultTableModel(0,columnNames.length);

		loadData(model);
		table = new JTable(model);
		model.fireTableDataChanged();

		table.setPreferredScrollableViewportSize(new Dimension(1000, 400));
		table.setFillsViewportHeight(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.getColumnModel().getColumn(0).setPreferredWidth(130);
		table.getColumnModel().getColumn(1).setPreferredWidth(100);
		// table.getColumnModel().getColumn(0).setCellRenderer(
		// new MyCellRenderer());

		table.setRowSelectionAllowed(true);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		table.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					/*
					 * Called each time there is a change to the Workouts
					 * selection (deselect, select)
					 * 
					 * @see
					 * javax.swing.event.ListSelectionListener#valueChanged(
					 * javax.swing.event.ListSelectionEvent)
					 */
					public void valueChanged(ListSelectionEvent e) {
						if (!e.getValueIsAdjusting()) {
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
								listChanged = true;
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
		frame = new JFrame( );

		ImageIcon img = new ImageIcon("icons/turbo.jpg");
		frame.setIconImage(img.getImage());
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		this.setOpaque(true); // content panes must be opaque
		frame.setContentPane(this);

		// Menu
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		summaryMenu = new JMenu();
		summaryMenu.setMnemonic(KeyEvent.VK_S);
		summaryMenu.setMargin(new Insets(0, 0, 0, 10));
		menuBar.add(summaryMenu);
		// MMP, hr/cad/pwr graph
		// pwr/time, hr/time, cad, time
		mmpMenuItem = new JMenuItem();
		mmpMenuItem.setActionCommand(mmpGraph);
		summaryMenu.add(mmpMenuItem);
		mmpMenuItem.addActionListener(this);

		schrMenuItem = new JMenuItem();
		schrMenuItem.setActionCommand(schrGraph);
		summaryMenu.add(schrMenuItem);
		schrMenuItem.addActionListener(this);

		importMenuItem = new JMenuItem();
		importMenuItem.setActionCommand(importer);
		summaryMenu.add(importMenuItem);
		importMenuItem.addActionListener(this);

		// 1sec...?
		fatMenu = new JMenu();
		fatMenu.setMnemonic(KeyEvent.VK_F);
		fatMenu.setMargin(new Insets(0, 0, 0, 10));
		menuBar.add(fatMenu);

		// pwr/cad, pwr/dist, speed/cadence
		scatMenu = new JMenu();
		scatMenu.setMnemonic(KeyEvent.VK_A);
		menuBar.add(scatMenu);
		scatMenu.setMargin(new Insets(0, 0, 0, 10));

		// menuBar.add(new JSeparator());
		cpgMenuItem = new JMenuItem();
		cpgMenuItem.setActionCommand(scGraph);
		cpgMenuItem.addActionListener(this);
		scatMenu.add(cpgMenuItem);

		powerWattsMenuItem = new JMenuItem();
		powerWattsMenuItem.setActionCommand(hrWattsGraph);
		powerWattsMenuItem.addActionListener(this);
		scatMenu.add(powerWattsMenuItem);

		quadAnalysisMenuItem = new JMenuItem(
				userPrefs.messages.getString("quadAnal"));
		quadAnalysisMenuItem.setActionCommand(qaGraph);
		quadAnalysisMenuItem.addActionListener(this);
		scatMenu.add(quadAnalysisMenuItem);

		// distribution
		distMenu = new JMenu();
		distMenu.setMnemonic(KeyEvent.VK_D);
		menuBar.add(distMenu);
		pdgMenuItem = new JMenuItem();
		pdgMenuItem.setActionCommand(pdGraph);
		distMenu.add(pdgMenuItem);
		pdgMenuItem.addActionListener(this);

		cadMenuItem = new JMenuItem();
		cadMenuItem.setActionCommand(cdGraph);
		distMenu.add(cadMenuItem);
		cadMenuItem.addActionListener(this);

		hrMenuItem = new JMenuItem();
		hrMenuItem.setActionCommand(hrdGraph);
		distMenu.add(hrMenuItem);
		hrMenuItem.addActionListener(this);

		tlMenuItem = new JMenuItem();
		tlMenuItem.setActionCommand(tlGraph);
		distMenu.add(tlMenuItem);
		tlMenuItem.addActionListener(this);

		tlhrMenuItem = new JMenuItem();
		tlhrMenuItem.setActionCommand(tlhrGraph);
		distMenu.add(tlhrMenuItem);
		tlhrMenuItem.addActionListener(this);

		doText();
		// Display the window.
		frame.pack();
		frame.setVisible(true);

		MessageBus.INSTANCE.register(Messages.LOCALE, this);
	}

	public void updateModel() {
		DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
		loadData(tableModel);
		tableModel.fireTableDataChanged();
	}

	public void setVisible(boolean flag) {
		if (!frame.isVisible()) {
			frame.setVisible(flag);
			super.setVisible(flag);
		}
		DefaultTableModel tableModel = (DefaultTableModel) table.getModel();

		loadData(tableModel);
		// table = new JTable(model);
		tableModel.fireTableDataChanged();
		frame.toFront();
	}

	private void loadData(DefaultTableModel tableModel) {
		DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		workoutList = UserPreferences.INSTANCE.listWorkouts();

		tableModel.setRowCount(0);
		// String[] data = new String[columnNames.length];
		Object[] data = new Object[columnNames.length];
		for (WorkoutData workout : workoutList) {
			data[0] = workout.getDateAsString();
			// data[0] = new Boolean(true); - checkbox
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

		if (schrGraph.equals(command)) {
			// Speed,Cadence, HR graph - this can only take a single line, don't
			// reload data
			SCHRGraph();
			return;
		}
		if (importer.equals(command)) {
			// Import new workouts
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

			// All workouts to be imported are dumped into an "Imports"
			// directory
			String workoutDir = UserPreferences.INSTANCE.getUserDataDirectory()
					+ IMPORTDIR;

			ActivityReader ar = new ActivityReader();

			File dir = new File(workoutDir);
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
				importedFiles.append(userPrefs.messages.getString("noFiles"));
			} else {

				importedFiles.append(userPrefs.messages.getString("imported") + ":\n\n");
				for (String file : ar.getImportedFileList()) {
					importedFiles.append(file);
					importedFiles.append("\n");
				}// for

				updateModel();

			}
			JOptionPane.showMessageDialog(this, importedFiles.toString(),
					userPrefs.messages.getString("import"), JOptionPane.INFORMATION_MESSAGE);

			return;
		}

		/*
		 * Load selected data for other graphs
		 */
		if (!load()) {
			return;
		}
		if (mmpGraph.equals(command)) {
			mmpGraph();
			return;
		}
		if (scGraph.equals(command)) {
			CSScatterPlot();
			return;
		}
		if (qaGraph.equals(command)) {
			QuadrantAnalysis();
			return;
		}
		if (hrWattsGraph.equals(command)) {
			// Heart Rate vs Watts Scatter Plot
			HRWattsScatterPlot();
			return;
		}
		if (pdGraph.equals(command)) {
			// Power distribution graph
			DistributionGraph(new DistributionAccessor() {
				public int getKey(Telemetry t) {
					return getKey(t.getPower());
				}
			}, 15, userPrefs.messages.getString("pdGr"),
					userPrefs.messages.getString("poWtt"));
			return;
		}
		if (cdGraph.equals(command)) {
			// Cadence distribution graph
			DistributionGraph(new DistributionAccessor() {
				public int getKey(Telemetry t) {
					return getKey(t.getCadence());
				}
			}, 5, userPrefs.messages.getString("cDgr"),
					userPrefs.messages.getString("cDrpm"));
			return;
		}
		if (hrdGraph.equals(command)) {
			DistributionGraph(new DistributionAccessor() {
				public int getKey(Telemetry t) {
					if (t.getHeartRate() < 30) {
						return -1; // ignore these values
					}

					return getKey(t.getHeartRate());
				}
			}, 10, userPrefs.messages.getString("hrDgr"),
					userPrefs.messages.getString("hrBpm"));
			return;
		}
		if (tlGraph.equals(command)) {
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
			}, 0, userPrefs.messages.getString("trainDist"), userPrefs.messages.getString("trainlevel"));
			return;
		}
		if (tlhrGraph.equals(command)) {
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
			}, 0, userPrefs.messages.getString("trainlevelhr"), userPrefs.messages.getString("trainlevel"));
			return;
		}
	}

	/**
	 * Load one or more workouts selected in view. We concatenate all data into
	 * a telemetry array.
	 * 
	 * Side effect: Global workoutData points to the last workout data loaded.
	 * FIX: we should change this
	 * 
	 * @return false - no data could be loaded due to a selection error. True :
	 *         data is ready to use.
	 */
	private boolean load() {
		if (selectedRows == null || selectedRows.isEmpty()) {
			JOptionPane.showMessageDialog(this,
					userPrefs.messages.getString("noDataDisp"), userPrefs.messages.getString("noData"),
					JOptionPane.ERROR_MESSAGE);
			return false;
		} else if (listChanged == false && telemetry != null) {
			// data already loaded and nothing changed
			return true;
		}

		String workoutDir = UserPreferences.INSTANCE.getUserDataDirectory()
				+ TcxWriter.WORKOUTDIR;

		telemetry = new ArrayList[selectedRows.size()];

		int count = 0;
		for (int i : selectedRows) {
			workoutData = workoutList.get(i);
			String fileName = workoutData.getTcxFile();
			try {
				telemetry[count] = ActivityReader.readTelemetry(workoutDir
						+ fileName);
			} catch (Exception e1) {
				// bah, do nothing
				logger.error(e1.getLocalizedMessage());
			}
			count++;
		}// for

		listChanged = false;
		return true;
	}

	/**
	 * Will reanalyze workouts on the selected rows, saving the data to the
	 * database.
	 */
	void reanalyze() {
		if (selectedRows == null || selectedRows.isEmpty()) {
			JOptionPane.showMessageDialog(this,
					userPrefs.messages.getString("noDataDisp"), userPrefs.messages.getString("noData"),
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		String workoutDir = UserPreferences.INSTANCE.getUserDataDirectory()
				+ TcxWriter.WORKOUTDIR;

		telemetry = new ArrayList[selectedRows.size()];

		int count = 0;
		for (int i : selectedRows) {
			workoutData = workoutList.get(i);
			String fileName = workoutData.getTcxFile();
			int ftp = workoutData.getFtp();
			try {
				telemetry[count] = ActivityReader.readTelemetry(workoutDir
						+ fileName);

				workoutData = TrainingAnalysis.analyze(telemetry[count]);
				workoutData.setTcxFile(fileName);
				workoutData.setFtp(ftp);
				userPrefs.updateWorkout(workoutData); // saves data to RDBMS
				workoutList.set(i, workoutData); // FIXME updates local cache of
													// workouts but won't fire
													// list changed event?
			} catch (Exception e1) {
				logger.error(e1.getLocalizedMessage());
			}
			count++;

		}// for

		listChanged = false;
		return;
	}

	/*
	 * We don't destroy workouts view, we just hide it.
	 */
	void quit() {
		frame.setVisible(false);
	}

	/**
	 * Create Mean Maximal Power Graph
	 * 
	 * This is a graph of power plotted by time.
	 */
	public void mmpGraph() {
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
		XYSeries series = new XYSeries(userPrefs.messages.getString("mmp"));
		for (Entry<Integer, Long> entry : powerValues.descendingMap()
				.entrySet()) {
			Integer pwr = entry.getKey(); // power, Y axis
			if (total == 0) {
				// first time thru
				total = entry.getValue();
				if (total > 1) {
					// add extra point at 1 second
					series.addOrUpdate(1, (double) pwr);
				}
			} else {
				total += entry.getValue(); // time in milliseconds - X axis
			}
			series.addOrUpdate(total / 1000, (double) pwr);
		}// for

		MMPGraph mmp = new MMPGraph(series);

		JFrame frame = new JFrame(userPrefs.messages.getString("mmp"));
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
	 * Quadrant Analysis
	 */
	private void QuadrantAnalysis() {
		XYSeries series = new XYSeries(userPrefs.messages.getString("forceVeloc"));

		for (int i = 0; i < telemetry.length; i++) {
			for (Telemetry t : telemetry[i]) {

				if (t.getCadence() > 50 && t.getPower() > 50) {
					// CPV - (Cadence * crankLength (meters) * 2 * Pi) / 60
					double cpv = (t.getCadence() * 0.1725 * 2 * pi) / 60;
					// AEPF = (power * 60) / (Cadence * 2 * Pi * Crank Length)
					double aepf = (t.getPower() * 60)
							/ (t.getCadence() * 2 * pi * 0.1725);
					series.addOrUpdate(cpv, aepf);
				}
			}// for
		}// for

		GenericScatterGraph mmp = new GenericScatterGraph(series, "CPV (m/s)",
				"AEPF (newtons)");

		XYSeries series1 = new XYSeries("FTP");

		for (int cadence = 16; cadence < 160; cadence++) {
			// CPV - (Cadence * crankLength (meters) * 2 * Pi) / 60
			double cpv = (cadence * 0.1725 * 2 * 3.142) / 60;
			// AEPF = (power * 60) / (Cadence * 2 * Pi * Crank Length)
			double aepf = (UserPreferences.INSTANCE.getMaxPower() * 60)
					/ (cadence * 2 * 3.142 * 0.1725);
			series1.add(cpv, aepf);
		}
		mmp.addLine(series1);

		XYSeries series2 = new XYSeries("CPV (80 rpm)");
		double cpv = (80 * 0.1725 * 2 * 3.142) / 60;
		series2.add(cpv, 0);
		series2.add(cpv, 700);
		mmp.addLine(series2);

		XYSeries series3 = new XYSeries("CPV (80 rpm)");
		double aepf = (UserPreferences.INSTANCE.getMaxPower() * 60)
				/ (80 * 2 * 3.142 * 0.1725);
		series3.add(0, aepf);
		series3.add(3, aepf);
		mmp.addLine(series3);

		JFrame frame = new JFrame(userPrefs.messages.getString("quadAnal"));
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
		XYSeries series = new XYSeries(userPrefs.messages.getString("cadPow"));

		for (int i = 0; i < telemetry.length; i++) {
			for (Telemetry t : telemetry[i]) {

				if (t.getCadence() > 0 && t.getPower() > 0) {
					series.addOrUpdate(t.getPower(), t.getCadence());
				}
			}// for
		}// for

		GenericScatterGraph mmp = new GenericScatterGraph(series,
				userPrefs.messages.getString("poWtt"),
				userPrefs.messages.getString("cDrpm"));
		JFrame frame = new JFrame(userPrefs.messages.getString("cadPow"));
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
		XYSeries series = new XYSeries(userPrefs.messages.getString("hrWatts"));

		for (int i = 0; i < telemetry.length; i++) {
			for (Telemetry t : telemetry[i]) {

				if (t.getHeartRate() > 0 && t.getPower() > 0) {
					series.addOrUpdate(t.getPower(), t.getHeartRate());
				}
			}// for
		}// for

		GenericScatterGraph mmp = new GenericScatterGraph(series,
				userPrefs.messages.getString("poWtt"),
				userPrefs.messages.getString("hrBpm"));
		JFrame frame = new JFrame(userPrefs.messages.getString("hrPow"));
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

	/**
	 * Draw a graph of Power, Cadence and Heart Rate (and maybe speed?).
	 */
	public void SCHRGraph() {
		if (selectedRows != null && selectedRows.size() > 1) {
			JOptionPane.showMessageDialog(this, userPrefs.messages.getString("sglWk"),
					userPrefs.messages.getString("selErr"), JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (!load()) {
			return;
		}

		SCHRGraph pchrGraph = new SCHRGraph(telemetry);
		// show data with smoothing of 1 second
		pchrGraph.updateValues(1);
		pchrGraph.updateWorkoutData(workoutData);

		JFrame frame = new JFrame(userPrefs.messages.getString("rideSum"));
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

	/*
	 * Delete one or more rows of data from the database.
	 */
	private void deleteSelectedRows(int[] rows) {
		DefaultTableModel dm = (DefaultTableModel) table.getModel();
		for (int i = rows.length - 1; i >= 0; i--) {
			dm.removeRow(rows[i]);

			String fileName = workoutList.get(rows[i]).getTcxFile();
			userPrefs.deleteWorkout(fileName);

			String path = UserPreferences.INSTANCE.getUserDataDirectory()
					+ TcxWriter.WORKOUTDIR + fileName;

			File file = new File(path);
			if (file.delete()) {
				// log an error
				logger.error("deleted " + path);
			}
			workoutList.remove(rows[i]);

		}// for
		selectedRows = null;
	}

	/*
	 * Setup button text, makes it easy to update if locale is changed
	 */
	private void doText() {
		frame.setTitle("Wattzap Analyzer - " + userPrefs.messages.getString("training_analysis"));
		
		summaryMenu.setText(userPrefs.messages.getString("summary"));
		mmpMenuItem.setText(userPrefs.messages.getString("mmp"));
		schrMenuItem.setText(userPrefs.messages.getString("schr"));

		importMenuItem.setText(userPrefs.messages.getString("import"));
		fatMenu.setText(userPrefs.messages.getString("fatigue"));
		scatMenu.setText(userPrefs.messages.getString("scatter"));
		cpgMenuItem.setText(userPrefs.messages.getString("cpg"));
		powerWattsMenuItem.setText(userPrefs.messages.getString("poWt"));
		quadAnalysisMenuItem.setText(userPrefs.messages.getString("quadAnal"));

		distMenu.setText(userPrefs.messages.getString("distribution"));
		pdgMenuItem.setText(userPrefs.messages.getString("power"));
		cadMenuItem.setText(userPrefs.messages.getString("cadence"));
		hrMenuItem.setText(userPrefs.messages.getString("heartrate"));
		tlMenuItem.setText(userPrefs.messages.getString("trainlevel"));
		tlhrMenuItem.setText(userPrefs.messages.getString("trainlevelhr"));

		for (int i = 0; i < table.getColumnCount(); i++) {
			TableColumn column1 = table.getTableHeader().getColumnModel()
					.getColumn(i);
			column1.setHeaderValue(columnNames[i]);
		}

	}

	/**
	 * Change text language if we get a LOCALE message
	 */
	@Override
	public void callback(Messages message, Object o) {
		switch (message) {
		case LOCALE:
			doText();
			break;
		}
	}

	public class MyCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {

			if (value instanceof JComboBox) {
				return (JComboBox) value;
			}
			if (value instanceof Boolean) {
				JCheckBox cb = new JCheckBox();
				cb.setSelected(((Boolean) value).booleanValue());
				return cb;
			}
			if (value instanceof JCheckBox) {
				return (JCheckBox) value;
			}
			return new JTextField(value.toString());
		}

	}
}
