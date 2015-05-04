/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wattzap.model;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jfree.data.xy.XYSeries;

import com.gpxcreator.gpxpanel.GPXFile;
import com.gpxcreator.gpxpanel.Route;
import com.gpxcreator.gpxpanel.Waypoint;
import com.gpxcreator.gpxpanel.WaypointGroup;
import com.wattzap.model.dto.Point;

/**
 * Tacx TTS file reader.
 * 
 * @author JaroslawP
 * @author David George
 * @date 1 April 2015
 */
@RouteAnnotation
public class TTSReader extends RouteReader {
	private static double frameRate;
	private ArrayList<Point> pointList = null;
	private ProgramPoint[] programList = null;
	private List<byte[]> content;
	private byte[] pre;

	private static String currentFile;
	private static int imageId;

	private static double maxSlope;
	private static double minSlope;
	private static XYSeries series;

	private double totalDistance = 0.0;

	private String fileName;
	// Block Types
	private final static int PROGRAM_DATA = 1032;
	private final static int DISTANCE_FRAME = 5020;
	private final static int GPS_DATA = 5050;
	private final static int GENERAL_INFO = 1031;
	private final static int TRAINING_TYPE = 5010;
	private final static int SEGMENT_INFO = 1041;
	private final static int SEGMENT_RANGE = 1050;
	private static final int VIDEO_INFO = 2010;

	private static Logger logger = LogManager.getLogger("TTS Reader");

	private static GPXFile gpxFile = new GPXFile();

	private static int[] key = { 0xD6, 0x9C, 0xD8, 0xBC, 0xDA, 0xA9, 0xDC,
			0xB0, 0xDE, 0xB6, 0xE0, 0x95, 0xE2, 0xC3, 0xE4, 0x97, 0xE6, 0x92,
			0xE8, 0x85, 0xEA, 0x8E, 0xEC, 0x9E, 0xEE, 0x91, 0xF0, 0xB1, 0xF2,
			0xD2, 0xF4, 0xD4, 0xF6, 0xD7, 0xF8, 0xB1, 0xFA, 0x9E, 0xFC, 0xDD,
			0xFE, 0x96, 0x00, 0x72, 0x02, 0x23, 0x04, 0x71, 0x06, 0x6F, 0x08,
			0x6C, 0x0A, 0x2B, 0x0C, 0x7E, 0x0E, 0x4E, 0x10, 0x67, 0x12, 0x7A,
			0x14, 0x7A, 0x16, 0x65, 0x18, 0x39, 0x1A, 0x74, 0x1C, 0x7B, 0x1E,
			0x3F, 0x20, 0x44, 0x22, 0x75, 0x24, 0x40, 0x26, 0x55, 0x28, 0x50,
			0x2A, 0x0B, 0x2C, 0x18, 0x2E, 0x19, 0x30, 0x08, 0x32, 0x0B, 0x34,
			0x02, 0x36, 0x1F, 0x38, 0x10, 0x3A, 0x1F, 0x3C, 0x17, 0x3E, 0x17,
			0x40, 0x62, 0x42, 0x65, 0x44, 0x65, 0x46, 0x6E, 0x48, 0x69, 0x4A,
			0x25, 0x4C, 0x28, 0x4E, 0x2A, 0x50, 0x35, 0x52, 0x36, 0x54, 0x31,
			0x56, 0x77, 0x58, 0x09, 0x5A, 0x29, 0x5C, 0x6D, 0x5E, 0x2B, 0x60,
			0x52, 0x62, 0x00, 0x64, 0x31, 0x66, 0x2E, 0x68, 0x26, 0x6A, 0x25,
			0x6C, 0x4D, 0x6E, 0x3C, 0x70, 0x28, 0x72, 0x20, 0x74, 0x21, 0x76,
			0x32, 0x78, 0x34, 0x7A, 0x55, 0x7C, 0x37, 0x7E, 0x0A, 0x80, 0xF2,
			0x82, 0xF7, 0x84, 0xC7, 0x86, 0xC2, 0x88, 0xDA, 0x8A, 0xDE, 0x8C,
			0xDF, 0x8E, 0xCA, 0x90, 0xE5, 0x92, 0xFC, 0x94, 0xB0, 0x96, 0xC9,
			0x98, 0xF4, 0x9A, 0xAF, 0x9C, 0xF6, 0x9E, 0xFA, 0xA0, 0xD5, 0xA2,
			0xCB, 0xA4, 0xCC, 0xA6, 0xD4, 0xA8, 0x83, 0xAA, 0x8F, 0xAC, 0xD9,
			0xAE, 0xDD, 0xB0, 0xD8, 0xB2, 0xDD, 0xB4, 0xD2, 0xB6, 0xDB, 0xB8,
			0xE6, 0xBA, 0xE4, 0xBC, 0xE2, 0xBE, 0xE0, 0xC0, 0xAF, 0xC2, 0xA4,
			0xC4, 0xE2, 0xC6, 0xA9, 0xC8, 0xBC, 0xCA, 0xAD, 0xCC, 0xAB, 0xCE,
			0xEE };

	@Override
	public String getExtension() {
		return "tts";
	}

	@Override
	public String getFilename() {
		// TODO Auto-generated method stub
		return fileName;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "IT_Stelvio08";
	}

	@Override
	public GPXFile getGpxFile() {
		// TODO Auto-generated method stub
		return gpxFile;
	}

	@Override
	public XYSeries getSeries() {
		// TODO Auto-generated method stub
		return series;
	}

	@Override
	public Point[] getPoints() {
		return points;
	}

	@Override
	public void load(String fileName) {
		this.fileName = fileName.substring(0, fileName.lastIndexOf('.'));
		try {
			parseFile(fileName);
		} catch (IOException ex) {
			logger.error("Cannot read " + fileName + "::"
					+ ex.getLocalizedMessage());
		} catch (IllegalArgumentException ex) {
			logger.error("Wrong file format " + fileName + "::"
					+ ex.getLocalizedMessage());
		}

	}

	@Override
	public void close() {
		pointList = null;

	}

	@Override
	public double getDistanceMeters() {
		// TODO Auto-generated method stub
		return totalDistance;
	}

	@Override
	public int routeType() {
		return SLOPE;
	}

	@Override
	public double getMaxSlope() {
		// TODO Auto-generated method stub
		return maxSlope;
	}

	@Override
	public double getMinSlope() {
		// TODO Auto-generated method stub
		return minSlope;
	}

	private void parseFile(String fileName) throws FileNotFoundException,
			IOException, IllegalArgumentException {

		content = new ArrayList<byte[]>();
		pre = new byte[2];
		int lastSize = -1;
		InputStream is = new FileInputStream(fileName);
		for (;;) {
			if (!readData(is, pre, false)) {
				break;
			}
			if (isHeader(pre)) {
				byte[] header = new byte[14];
				if (readData(is, header, true)) {
					content.add(header);
					lastSize = getUInt(header, 6) * getUInt(header, 10);
				} else {
					throw new IllegalArgumentException("Cannot read header");
				}

				// one byte data.. unconditionally read as data, no-one is able
				// to check it
				if (lastSize < 2) {
					byte[] data = new byte[lastSize];
					if (readData(is, data, false)) {
						content.add(data);
					} else {
						throw new IllegalArgumentException("Cannot read "
								+ lastSize + "b data");
					}
					lastSize = -1;
				}
			} else {
				if (lastSize < 2) {
					throw new IllegalArgumentException(
							"Data not allowed, header " + getUShort(pre, 0));
				}
				byte[] data = new byte[lastSize];
				if (readData(is, data, true)) {
					content.add(data);
				} else {
					throw new IllegalArgumentException("Cannot read "
							+ lastSize + "b data");
				}
				lastSize = -1;
			}
		}// for
		is.close();
		loadHeaders();

	}

	private static String toHex(byte bb) {
		int b = bb;
		if (b < 0) {
			b += 256;
		}
		String s = Integer.toHexString(b);
		if (s.length() == 1) {
			return "0" + s;
		} else {
			return s;
		}
	}

	private static final Map<Integer, String> strings = new HashMap<Integer, String>();

	static {
		strings.put(1001, "route name");
		strings.put(1002, "route description");
		strings.put(1041, "segment name");
		strings.put(1042, "segment description");
		strings.put(2001, "company");
		strings.put(2004, "serial");
		strings.put(2005, "time");
		strings.put(2007, "link");
		strings.put(5001, "product");
		strings.put(5002, "video name");
		strings.put(6001, "infobox #1");
	}

	private interface Formatter {

		String format(int version, byte[] data);
	}

	private static final Map<Integer, Formatter> formatters = new HashMap<Integer, Formatter>();

	private enum StringType {

		NONPRINTABLE, BLOCK, STRING, IMAGE, CRC
	};

	private void loadHeaders() {
		pointList = new ArrayList<Point>();
		series = new XYSeries("");

		int[] key2 = rehashKey(key, 17);
		int[] keyH = null;

		int blockType = -1;
		int version = -1;
		int stringId = -1;
		StringType stringType = StringType.BLOCK;

		int fingerprint = 0;
		int bytes = 0;
		for (byte[] data : content) {
			if (isHeader(data)) {
				String hdr = bytes + " [" + Integer.toHexString(bytes) + "]: "
						+ getUShort(data, 0) + "." + getUShort(data, 2) + " v"
						+ getUShort(data, 4) + " " + getUInt(data, 6) + "x"
						+ getUInt(data, 10);
				logger.info(hdr);
				fingerprint = getUInt(data, 6);
				keyH = encryptHeader(iarr(data), key2);

				stringType = StringType.NONPRINTABLE;
				switch (getUShort(data, 2)) {
				case 10: // crc of the data?
					// I don't know how to compute it.. and to which data it
					// belongs..
					// for sure I'm not going to check these, I assume file is
					// not broken
					// (why it can be?)
					stringType = StringType.CRC;
					break;
				case 110: // UTF-16 string
					stringType = StringType.STRING;
					stringId = getUShort(data, 0);
					break;
				case 120: // image fingerprint
					stringId = getUShort(data, 0) + 1000;
					break;
				case 121: // imageType? always 01
					break;
				case 122: // image bytes, name is present in previous string
							// from the block
					stringType = StringType.IMAGE;
					break;
				default:
					stringType = StringType.BLOCK;
					blockType = getUShort(data, 2);
					version = getUShort(data, 4);
					logger.info("\nblock type " + blockType + " version "
							+ version);
					stringId = -1;
					break;
				}
			} else {
				int[] decrD = decryptData(iarr(data), keyH);

				keyH = null;
				logger.info("::");

				String result = null;
				switch (stringType) {
				case CRC:
					break;
				case IMAGE:
					logger.info("[image " + blockType + "." + (stringId - 1000)
							+ "]");
					try {
						result = currentFile + "." + (imageId++) + ".png";
						FileOutputStream file = new FileOutputStream(result);
						file.write(barr(decrD));
						file.close();
					} catch (IOException e) {
						result = "cannot create: " + e;
					}
					break;
				case STRING:
					if (strings.containsKey(blockType + stringId)) {
						logger.info("[" + strings.get(blockType + stringId) + "]");
					} else {
						logger.info("[" + blockType + "." + stringId + "]");
					}
					StringBuilder str = new StringBuilder();
					for (int i = 0; i < decrD.length / 2; i++) {
						char c = (char) (decrD[2 * i] | (int) decrD[2 * i + 1] << 8);
						str.append(c);
					}
					result = str.toString();
					break;
				case BLOCK:
					blockProcessing(blockType, version, barr(decrD));
					break;
				}
			}
			logger.info("\n");
			bytes += data.length;
		}

		// merge program points
		int pointCount = 0;
		long lastDistance = 0;
		double lastSlope = 0.0;
		Point lastPoint = pointList.get(0);
		int programCount = programList.length;
		for (int i = 0; i < programCount; i++) {
			ProgramPoint pp = programList[i];
			while (pointCount < pointList.size()) {
				Point p = pointList.get(pointCount);
				if (pp.distance > p.getDistanceFromStart()
						|| i == programCount - 1) {
					// see which point is closest
					if ((p.getDistanceFromStart() - lastDistance) < (p
							.getDistanceFromStart() - pp.distance)) {
						p.setGradient(pp.slope);
					} else {
						p.setGradient(lastSlope);
					}
					// turn into meters
					p.setDistanceFromStart(p.getDistanceFromStart() / 100);
					// convert to mS
					p.setTime(p.getTime() * 1000);

					// speed = d/t
					if (pointCount > 0) {
						p.setSpeed((3600 * (p.getDistanceFromStart() - lastPoint
								.getDistanceFromStart()))
								/ ((p.getTime() - lastPoint.getTime())));
					}
					lastPoint = p;
					// meters / meters
					logger.debug(p);
					series.add(p.getDistanceFromStart() / 1000,
							p.getElevation());
				} else {
					break;
				}
				lastDistance = pp.distance;
				lastSlope = pp.slope;

				pointCount++;
			}// while

		}// for
			// fill in speed for first point
		pointList.get(0).setSpeed(pointList.get(1).getSpeed());
		;
		totalDistance = pointList.get(pointList.size() - 1)
				.getDistanceFromStart();
		points = pointList.toArray(new Point[pointList.size()]);
		programList = null; // let the GC recover the memory
		pointList = null;

	}

	private void blockProcessing(int blockType, int version, byte[] data) {

		switch (blockType) {
		case PROGRAM_DATA:
			programData(version, data);
			break;
		case DISTANCE_FRAME:
			distanceToFrame(version, data);
			break;

		case GPS_DATA:
			GPSData(version, data);
			break;
		case GENERAL_INFO:
			generalInfo(version, data);
			break;
		case TRAINING_TYPE:
			trainingType(version, data);
			break;
		case SEGMENT_INFO:
			segmentInfo(version, data);
			break;
		case SEGMENT_RANGE:
			segmentRange(version, data);
			break;
		case VIDEO_INFO:
			videoInfo(version, data);
		default:
			logger.error("Unidentified block type " + blockType);
		}

	}

	/*
	 * Block routines
	 */
	private void videoInfo(int version, byte[] data) {
	}
	
	private void segmentRange(int version, byte[] data) {
		if (data.length % 10 != 0) {
			logger.error("Segment Range data wrong length " + data.length);
		}
		StringBuilder b = new StringBuilder();
		b.append("[segment range]");
		for (int i = 0; i < data.length / 10; i++) {
			b.append(" [" + i + "=" + (getUInt(data, i * 10 + 0) / 100000.0)
					+ "-" + (getUInt(data, i * 10 + 4) / 100000.0));
			if (getUShort(data, i * 10 + 6) != 0) {
				b.append("/0x"
						+ Integer.toHexString(getUShort(data, i * 10 + 6)));
			}
			b.append("]");
		}
		logger.info(b.toString());
	}

	// segment range; 548300 is 5.483km. What is short value in "old" files?
	private void segmentInfo(int version, byte[] data) {
		if ((version == 1104) && (data.length == 8)) {
			logger.info("[segment range] " + (getUInt(data, 0) / 100000.0)
					+ "-" + (getUInt(data, 4) / 100000.0));
		}
		if ((version == 1000) && (data.length == 10)) {
			logger.info("[segment range] " + (getUInt(data, 2) / 100000.0)
					+ "-" + (getUInt(data, 6) / 100000.0) + "/"
					+ getUShort(data, 0));
		}

	}

	// 1 for "plain" RLV, 2 for ERGOs
	private void trainingType(int version, byte[] data) {
		if (version == 1004) {
			switch (data[5]) {
			case 1:
				logger.info("[video type] RLV");
			case 2:
				logger.info("[video type] ERGO");
			}
		}
	}

	// it looks like part of GENERALINFO, definition of type is included:
	// DWORD WattSlopePulse;//0 = Watt program, 1 = Slope program, 2 = Pulse
	// (HR) program
	// DWORD TimeDist; //0 = Time based program, 1 = distance based program
	// I'm not sure about the order.. but only slope/distance (0/) and
	// power/time (1/1) pairs are handled..
	private void generalInfo(int version, byte[] data) {
		String programType;
		switch (data[0]) {
		case 0:
			programType = "slope";
			break;
		case 1:
			programType = "watt";
			break;
		case 2:
			programType = "heartRate";
			break;
		default:
			programType = "unknown program";
			break;
		}
		String trainingType;
		switch (data[1]) {
		case 0:
			trainingType = "distance";
			break;
		case 1:
			trainingType = "time";
			break;
		default:
			trainingType = "unknown training";
			break;
		}
		logger.info("[program type] " + programType + " -> " + trainingType);

		return;
	}

	// It screams.. "I'm GPS position!". Distance followed by lat, lon,
	// altitude
	private void GPSData(int version, byte[] data) {
		if (data.length % 16 != 0) {
			logger.error("GPS Points Data wrong length " + data.length);
			return;
		}

		gpxFile.addRoute();
		List<Route> routes = gpxFile.getRoutes();
		Route route = routes.get(0);
		WaypointGroup path = route.getPath();

		logger.info("[" + (data.length / 16) + " gps points]");
		int pointCount = 0;
		int lastDistance = 0;
		double lastLat = 0;
		double lastLon = 0;
		double lastAlt = 0;
		int gpsCount = data.length / 16;
		for (int i = 0; i < gpsCount; i++) {
			int distance = getUInt(data, i * 16);
			// out.print("[" + i + "] " + distance + " cm, ");
			double lat = Float.intBitsToFloat(getUInt(data, i * 16 + 4));
			double lon = Float.intBitsToFloat(getUInt(data, i * 16 + 8));
			double altitude = Float.intBitsToFloat(getUInt(data, i * 16 + 12));

			// GPX Data
			Waypoint wayPoint = new Waypoint(lat, lon);
			wayPoint.setEle(altitude);
			path.addWaypoint(wayPoint);

			while (pointCount < pointList.size()) {
				Point p = pointList.get(pointCount);
				/*
				 * we're straddling two points or we've run out of gps points to
				 * use
				 */
				if (distance > p.getDistanceFromStart() || i == gpsCount - 1) {

					// see which point is closest
					if ((p.getDistanceFromStart() - lastDistance) < (p
							.getDistanceFromStart() - distance)) {
						// System.out.println("[" + pointCount + "]"
						// + p.getDistanceFromStart() + "/"
						// + distance + " : " + p.getTime()
						// + " secs " + lat + "/" + lon + ", "
						// + altitude + " meters");
						p.setElevation(altitude);
						p.setLatitude(lat);
						p.setLongitude(lon);

					} else {
						// System.out.println("[" + pointCount + "]"
						// + (long) p.getDistanceFromStart() + "/"
						// + lastDistance + " : " + p.getTime()
						// + " secs " + lastLat + "/" + lastLon
						// + ", " + lastAlt + " meters");
						p.setElevation(lastAlt);
						p.setLatitude(lastLat);
						p.setLongitude(lastLon);
					}
				} else {
					break;
				}

				pointCount++;
			}// while

			lastDistance = distance;
			lastLat = lat;
			lastLon = lon;
			lastAlt = altitude;

		}

		gpxFile.updateAllProperties();

		return;
	}

	/*
	 * Distance to Frame Mapping
	 * 
	 * Format: 2102675 (cm)/77531 (frames)
	 */

	private void distanceToFrame(int version, byte[] data) {
		if (data.length % 8 != 0) {
			logger.error("Distance2Frame Data wrong length " + data.length);
			return;
		}

		/*
		 * We can calculate frame rate by dividing number of frames by no. of
		 * data points. If value is less than or equal to 30 this is the
		 * framerate, if between 30 and 100 divided by 2 (one datapoint every 2
		 * seconds of film - typical with tacx TTS files), if the value is more
		 * than 100 divide by 10 (typical with RLV files converted to TTS).
		 */
		logger.info("[" + (data.length / 8) + " video points][last frame "
				+ getUInt(data, data.length - 4) + "]");
		double dataPoints = (data.length / 8);
		double frames = getUInt(data, data.length - 4);
		frameRate = frames / dataPoints;

		if (frameRate > 30 && frameRate < 100) {
			frameRate = frameRate / 2.0;
		} else if (frameRate >= 100) {
			frameRate = frameRate / 10.0;
		}
		logger.info("Frame Rate " + frameRate);

		for (int i = 0; i < data.length / 8; i++) {
			Point p = new Point();
			p.setDistanceFromStart(getUInt(data, i * 8));
			int frame = getUInt(data, i * 8 + 4);

			p.setTime((int) (frame / frameRate));
			pointList.add(p);
		}

	}

	/**
	 * PROGRAM data
	 * 
	 * Typically it is slope/distance information
	 * 
	 * short slope // FLOAT RollingFriction; // Usually 4.0 // Now it is integer
	 * (/100=>[m], /100=>[s]) and short (/100=>[%], [W], // probably HR as
	 * well..) // Value selector is in 1031.
	 */
	private void programData(int version, byte[] data) {

		if (data.length % 6 != 0) {
			logger.error("Program Data wrong length " + data.length);
			return;
		}

		logger.info("[" + (data.length / 6) + " program points]");
		long distance = 0;
		int pointCount = data.length / 6;
		programList = new ProgramPoint[pointCount];
		for (int i = 0; i < pointCount; i++) {
			int slope = getUShort(data, i * 6);
			if ((slope & 0x8000) != 0) {
				slope -= 0x10000;
			}

			// slope %, distance meters
			distance += (getUInt(data, i * 6 + 2));
			ProgramPoint p = new ProgramPoint();
			p.slope = (double) slope / 100;
			if (i == 0) {
				// first time thru'
				minSlope = p.slope;
				maxSlope = p.slope;
			}
			if (p.slope < minSlope) {
				minSlope = p.slope;
			}
			if (p.slope > maxSlope) {
				maxSlope = p.slope;
			}
			p.distance = distance;

			programList[i] = p;
		}
	}

	/*
	 * Utility Routines
	 */

	private static int uint(byte b) {
		if (b < 0) {
			return (int) b + 256;
		} else {
			return (int) b;
		}
	}

	private static int[] rehashKey(int[] A_0, int A_1) {
		int i;
		char[] chArray1 = new char[A_0.length / 2];
		for (i = 0; i < chArray1.length; i++) {
			chArray1[i] = (char) (A_0[2 * i] + 256 * A_0[2 * i + 1]);
		}

		int num1 = 1000170181 + A_1;
		int num2 = 0;
		int num3 = 1;
		while (num2 < chArray1.length) {
			int index1 = num2;
			char[] chArray2 = chArray1;
			int index2 = index1;
			int num4 = (int) (short) chArray1[index1];
			int num5 = (int) 255;
			int num6 = num4 & num5;
			int num7 = num1;
			int num8 = 1;
			int num9 = num7 + num8;
			byte num10 = (byte) (num6 ^ num7);
			int num11 = 8;
			int num12 = num4 >> num11;
			int num13 = num9;
			int num14 = 1;
			num1 = num13 + num14;
			int num15 = (int) (byte) (num12 ^ num13);
			int num16 = (int) (uint(num10) << 8 | uint((byte) num15)) & 0xffff;
			chArray2[index2] = (char) num16;
			int num17 = 1;
			num2 += num17;
		}

		int[] ret = new int[chArray1.length];
		for (i = 0; i < ret.length; i++) {
			ret[i] = (int) chArray1[i];
		}
		return ret;
	}

	private static int[] encryptHeader(int[] A_0, int[] key2) {
		int[] bytes = key2;
		int[] numArray = new int[bytes.length];
		int index1 = 0;
		int index2 = 0;
		int num = 6;
		while (true) {
			switch (num) {
			case 0:
				index1 = 0;
				num = 2;
				continue;
			case 1:
				if (index2 < bytes.length) {
					numArray[index2] = (A_0[index1] ^ bytes[index2]);
					num = 5;
					continue;
				} else {
					num = 3;
					continue;
				}
			case 2:
				++index2;
				num = 4;
				continue;
			case 3:
				return numArray;
			case 4:
			case 6:
				num = 1;
				continue;
			case 5:
				if (index1++ >= A_0.length - 1) {
					num = 0;
					continue;
				} else {
					num = 2;
					continue;
				}
			default:
				throw new IllegalArgumentException("Restart function?");
			}
		}
	}

	private static int[] decryptData(int[] A_0, int[] A_1) {
		int[] numArray = new int[A_0.length];
		int index = 0;
		int num1 = 5;

		int num2 = -1000;
		int e = 0; // set before each block
		while (true) {
			switch (num1) {
			case 0:
				return numArray;
			case 1:
				e = 0;
				num1 = 4;
				continue;
			case 2:
				if (index < A_0.length) {
					numArray[index] = A_1[e] ^ A_0[index];
					num2 = e++;
					num1 = 6;
					continue;
				} else {
					num1 = 0;
					continue;
				}
			case 3:
			case 5:
				num1 = 2;
				continue;
			case 4:
				++index;
				num1 = 3;
				continue;
			case 6:
				if (num2 >= A_1.length - 1) {
					num1 = 1;
					continue;
				} else {
					num1 = 4;
					continue;
				}
			default:
				throw new IllegalArgumentException("Restart function?");
			}
		}
	}

	private static int[] iarr(byte[] a) {
		int[] r = new int[a.length];
		for (int i = 0; i < a.length; i++) {
			r[i] = (int) a[i];
		}
		return r;
	}

	private static byte[] barr(int[] a) {
		byte[] r = new byte[a.length];
		for (int i = 0; i < a.length; i++) {
			r[i] = (byte) a[i];
		}
		return r;
	}

	private boolean readData(InputStream is, byte[] buffer, boolean copyPre)
			throws IOException {
		int first = 0;
		if (copyPre) {
			buffer[0] = pre[0];
			buffer[1] = pre[1];
			first = 2;
		}
		return (is.read(buffer, first, buffer.length - first) == buffer.length
				- first);
	}

	private static int getUByte(byte[] buffer, int offset) {
		int b = buffer[offset];
		if (b < 0) {
			b += 256;
		}
		return b;
	}

	private static int getUShort(byte[] buffer, int offset) {
		return getUByte(buffer, offset) | (getUByte(buffer, offset + 1) << 8);
	}

	private static int getUInt(byte[] buffer, int offset) {
		return getUShort(buffer, offset)
				| (getUShort(buffer, offset + 2) << 16);
	}

	public static int readLEInt(byte[] buffer, int offset) {
		return (buffer[offset + 3]) << 24 | (buffer[offset + 2] & 0xff) << 16
				| (buffer[offset + 1] & 0xff) << 8
				| (buffer[offset + 0] & 0xff);
	}

	public float readLittleFloat(byte[] buffer, int offset) {
		return Float.intBitsToFloat(readLEInt(buffer, offset));
	}

	public long readLELong(byte[] buffer, int offset) {
		return (buffer[offset + 7]) << 56 | (buffer[offset + 6] & 0xff) << 48
				| (buffer[offset + 5] & 0xff) << 40
				| (buffer[offset + 4] & 0xff) << 32
				| (buffer[offset + 3] & 0xff) << 24
				| (buffer[offset + 2] & 0xff) << 16
				| (buffer[offset + 1] & 0xff) << 8
				| (buffer[offset + 0] & 0xff);
	}

	public double readLittleDouble(byte[] buffer, int offset) {
		return Double.longBitsToDouble(readLELong(buffer, offset));
	}

	private static String getHex(byte[] buffer, int offset) {
		StringBuilder b = new StringBuilder();
		for (int i = offset; i < buffer.length; i++) {
			b.append(' ');
			b.append(toHex(buffer[i]));
		}
		return b.toString();
	}

	private static boolean isHeader(byte[] buffer) {
		if (buffer.length < 2) {
			return false;
		}
		return getUShort(buffer, 0) <= 20;
	}

	class gpsPoint {

	}

}

class ProgramPoint {
	long distance;
	double slope;
}
