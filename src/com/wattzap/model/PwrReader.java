package com.wattzap.model;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.jfree.data.xy.XYSeries;

import au.com.bytecode.opencsv.CSVReader;

import com.gpxcreator.gpxpanel.GPXFile;
import com.wattzap.controller.MessageBus;
import com.wattzap.controller.Messages;
import com.wattzap.model.dto.Point;
import com.wattzap.model.dto.TrainingData;
import com.wattzap.model.dto.TrainingItem;

/*
 * Wrapper class for WattzAp Power Routes
 * 
 * Format is:
 * 1st line is POWER | HEARTRATE
 * Then repeated lines of
 * time (hh:mm:ss), (int) power, (int) cadence, (double) speed
 * 
 * where int is a Power (or HR level?)
 * 
 * basically dist = speed / 3600 * time in seconds
 * 
 * we vary power or hr value within power band changing at random intervals up or down by certain amount, this makes
 * it more interesting.
 * 
 * @author David George (c) Copyright 2013-2015
 * @date 10 October 2014
 */
@RouteAnnotation
public class PwrReader extends RouteReader {
	private String filename;
	double distanceFromStart = 0.0;
	Random rand = new Random();

	@Override
	public String getExtension() {
		// TODO Auto-generated method stub
		return "pwr";
	}

	@Override
	public String getFilename() {
		return filename;
	}

	@Override
	public String getName() {
		return filename;
	}

	@Override
	public GPXFile getGpxFile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public XYSeries getSeries() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Point[] getPoints() {
		return points;
	}

	@Override
	public double getDistanceMeters() {
		// TODO Auto-generated method stub
		return distanceFromStart;
	}

	@Override
	public int routeType() {
		return this.POWER;
	}

	@Override
	public double getMaxSlope() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMinSlope() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void load(String filename) {
		CSVReader reader = null;
		distanceFromStart = 0.0;
		this.filename = filename.substring(0, filename.lastIndexOf('.'));
		ArrayList<Point> route = new ArrayList<Point>();

		try {
			reader = new CSVReader(new FileReader(filename));

			String[] nextLine;

			TrainingData tData = new TrainingData();
			tData.setPwr(true);
			tData.setCdc(false);

			int lastTime = 0;
			double speed = 25;
			int power = 0;
			String cadence = null;
			int intF = 0;
			while ((nextLine = reader.readNext()) != null) {
				// nextLine[] is an array of values from the line
				String f1 = nextLine[0];

				if (f1.trim().isEmpty() || f1.trim().startsWith("#")) {
					// comment or empty, ignore
					continue;
				}

				// time since start
				int runTime = 0;
				if (f1.indexOf(':') != -1) {
					// minutes:seconds
					int minutes = Integer.parseInt(f1.substring(0,
							f1.indexOf(':')));
					int seconds = Integer
							.parseInt(f1.substring(f1.indexOf(':') + 1));
					runTime = (minutes * 60) + seconds;
				} else {
					// just seconds
					runTime = Integer.parseInt(f1) * 60;
				}

				// length of this segment
				int segmentTime = runTime - lastTime;

				// if segment is too long we insert some extra synchronization
				// points, this also lets us vary power a bit.
				for (int i = 60; i < (segmentTime); i += 30) {
					Point p = new Point();
					p.setTime((lastTime + i) * 1000);
					p.setSpeed(speed);
					p.setDistanceFromStart(distanceFromStart + (speed / 3.6)
							* i);
					p.setPower(getPower(power));
					TrainingItem item = new TrainingItem();
					item.setDistanceMeters(p.getDistanceFromStart());
					item.setPower(p.getPower());
					item.setCadence(cadence);
					tData.addItem(item);
					route.add(p);
				}

				// read power
				if (!nextLine[1].isEmpty()) {
					String v = nextLine[1];
					power = Integer.parseInt(v.trim());
				}

				switch (power) {
				case 1:
					intF += (55 * 55) * (runTime - lastTime);
					break;
				case 2:
					intF += (66 * 66) * (runTime - lastTime);
					break;
				case 3:
					intF += (73 * 73) * (runTime - lastTime);
					break;
				case 4:
					intF += (97 * 97) * (runTime - lastTime);
					break;
				case 5:
					intF += (113 * 113) * (runTime - lastTime);
					break;
				default:
					intF += (121 * 121) * (runTime - lastTime);
					break;
				}

				if (!nextLine[2].isEmpty()) {
					cadence = nextLine[2];
				}

				Point p = new Point();
				p.setPower(getPower(power));
				distanceFromStart += (speed / 3.6) * (runTime - lastTime);

				if (!nextLine[3].isEmpty()) {
					String v = nextLine[3];
					speed = Double.parseDouble(v.trim());
				}
				p.setSpeed(speed);
				p.setTime(runTime * 1000);
				p.setDistanceFromStart(distanceFromStart);

				TrainingItem item = new TrainingItem();
				item.setDescription("To "
						+ String.format("%.2f", p.getDistanceFromStart() / 1000)
						+ " km");
				item.setDistanceMeters(p.getDistanceFromStart());
				item.setPower(p.getPower());
				item.setCadence(cadence);
				tData.addItem(item);
				route.add(p);

				lastTime = runTime;
			}// while
			System.out.println("Intensity Factor " + (Math.sqrt(intF / lastTime))
					* ((double) lastTime / 3600));

			points = route.toArray(new Point[route.size()]);
			MessageBus.INSTANCE.send(Messages.TRAINING, tData);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	/**
	 * Power is either a training level from 1 to 7 or a % of FTP We convert it
	 * to a real power in watts based on the user's FTP
	 * 
	 * @param power
	 * @return
	 */
	private final int getPower(int power) {

		if (power <= 7) {
			int powerLow;
			int powerHigh;
			// training level
			switch (power) {
			case 1:
				// active recovery < 55%
				powerHigh = (int) ((double) UserPreferences.INSTANCE
						.getMaxPower() * 0.55);
				powerLow = powerHigh / 2;
				power = powerLow + rand.nextInt((powerHigh - powerLow));
				break;
			case 2:
				// Endurance 56 - 75%
				powerHigh = (int) ((double) UserPreferences.INSTANCE
						.getMaxPower() * 0.75);
				powerLow = (int) ((double) UserPreferences.INSTANCE
						.getMaxPower() * 0.56);
				power = powerLow + rand.nextInt((powerHigh - powerLow));
				break;
			case 3:
				// Tempo 76 - 90%
				powerHigh = (int) ((double) UserPreferences.INSTANCE
						.getMaxPower() * 0.9);
				powerLow = (int) ((UserPreferences.INSTANCE.getMaxPower()) * 0.66);
				power = powerLow + rand.nextInt((powerHigh - powerLow));
				break;
			case 4:
				// Lactate Threshold 91-105%
				powerHigh = (int) ((double) UserPreferences.INSTANCE
						.getMaxPower() * 1.05);
				powerLow = (int) ((double) UserPreferences.INSTANCE
						.getMaxPower() * 0.91);
				power = powerLow + rand.nextInt((powerHigh - powerLow));
				break;
			case 5:
				// VO2Max 106-120
				powerHigh = (int) ((double) UserPreferences.INSTANCE
						.getMaxPower() * 1.2);
				powerLow = (int) ((double) UserPreferences.INSTANCE
						.getMaxPower() * 1.06);
				power = powerLow + rand.nextInt((powerHigh - powerLow));
				break;
			case 6:
				// Anaerobic Capacity
				powerHigh = (int) ((double) UserPreferences.INSTANCE
						.getMaxPower() * 1.50);
				powerLow = (int) ((double) UserPreferences.INSTANCE
						.getMaxPower() * 1.21);
				power = powerLow + rand.nextInt((powerHigh - powerLow));
				break;
			case 7:
				// Neuromuscular
				powerHigh = 0;
				powerLow = (int) ((double) UserPreferences.INSTANCE
						.getMaxPower() * 1.50);
				power = powerLow;
				break;
			}
		} else {
			// % of FTP
			power = (int) ((double) UserPreferences.INSTANCE.getMaxPower() * power);
		}
		return power;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

}
