package com.wattzap.view.graphs;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import com.wattzap.model.UserPreferences;

/**
 * Add a Save button to the toolbar to allow graph images to be saved.
 * 
 * @author David George (c) Copyright 2016
 * @date 12 February 2016
 */
public class GPanel extends JPanel implements ActionListener {
	static final private String SAVE = "save";
	static final private String CLOSE = "close";
	JPanel graph;
	String name;

	public GPanel(JPanel graph, String name) {
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.name = name;

		this.graph = graph;
		JToolBar toolBar = new JToolBar("Still draggable");
		addButtons(toolBar);

		add(toolBar);
		add(graph);
	}

	protected void addButtons(JToolBar toolBar) {
		toolBar.setBackground(Color.LIGHT_GRAY);

		JButton button = null;

		// first button
		button = makeNavigationButton("save", SAVE, "Save Graph as Image",
				"Save");
		toolBar.add(button);
		button = makeNavigationButton("close", CLOSE, "Close Window",
				"Close");
		toolBar.add(button);
	}

	protected JButton makeNavigationButton(String imageName,
			String actionCommand, String toolTipText, String altText) {
		// Look for the image.
		String imgLocation = "icons/" + imageName + ".png";

		// Create and initialize the button.
		JButton button = new JButton();
		button.setActionCommand(actionCommand);
		button.setToolTipText(toolTipText);
		button.addActionListener(this);

		ImageIcon img = new ImageIcon(imgLocation, altText);
		button.setIcon(img);
		// button.setText(altText);

		return button;
	}

	private void save() {
		BufferedImage bImg = new BufferedImage(graph.getWidth(),
				graph.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D cg = bImg.createGraphics();
		graph.paintAll(cg);
		try {
			if (ImageIO.write(bImg, "png",
					new File(UserPreferences.INSTANCE.getUserDataDirectory()
							+ "/" + name + ".png"))) {
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(SAVE)) {
			save();
		} else {
			JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
			frame.dispose();
		}
	}
}
