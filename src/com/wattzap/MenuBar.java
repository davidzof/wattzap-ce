package com.wattzap;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import com.wattzap.utils.TcxWriter;
import com.wattzap.view.MainFrame;
import com.wattzap.view.RouteFilePicker;
import com.wattzap.view.prefs.Preferences;
import com.wattzap.view.training.TrainingAnalysis;
import com.wattzap.view.training.TrainingDisplay;
import com.wattzap.view.training.TrainingPicker;

public class MenuBar extends JMenuBar {
	private static final long serialVersionUID = 8868201635857315675L;
	private MainFrame frame;
	
	public void init(MainFrame frame) {
		this.frame = frame;
		
		JMenu fileMenu = new JMenu("File");
		add(fileMenu);
		JMenuItem openMenuItem = new JMenuItem("Open Course");
		fileMenu.add(openMenuItem);

		RouteFilePicker picker = new RouteFilePicker(frame);
		openMenuItem.addActionListener(picker);

		JMenuItem quitMenuItem = new JMenuItem("Quit");
		fileMenu.add(quitMenuItem);
		quitMenuItem.addActionListener(frame);

		JMenu appMenu = new JMenu("Application");
		add(appMenu);
		
		Preferences preferences = new Preferences();
		JMenuItem prefMenuItem = new JMenuItem("Preferences");
		prefMenuItem.addActionListener(preferences);
		appMenu.add(prefMenuItem);
		JMenuItem aboutMenuItem = new JMenuItem("About");
		appMenu.add(aboutMenuItem);
		
		frame.setJMenuBar(this);
	}
	
	public void trainingInit(TrainingDisplay trainingDisplay) {
		// Submenu: Training
		JMenu trainingMenu = new JMenu("Training");
		add(trainingMenu);

		JMenuItem analMenuItem = new JMenuItem("Analyze");
		trainingMenu.add(analMenuItem);
		
		TrainingAnalysis analysis = new TrainingAnalysis(trainingDisplay);
		analMenuItem.addActionListener(analysis);
		

		JMenuItem trainMenuItem = new JMenuItem("Open Training");
		trainingMenu.add(trainMenuItem);

		TrainingPicker tPicker = new TrainingPicker(frame);
		trainMenuItem.addActionListener(tPicker);

		JMenuItem saveMenuItem = new JMenuItem("Save as TCX");
		trainingMenu.add(saveMenuItem);

		TcxWriter tcxWriter = new TcxWriter(frame, trainingDisplay);
		saveMenuItem.addActionListener(tcxWriter);

		JMenuItem csvMenuItem = new JMenuItem("Save as CSV");
		trainingMenu.add(csvMenuItem);

	}

}
