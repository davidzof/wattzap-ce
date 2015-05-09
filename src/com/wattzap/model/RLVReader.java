package com.wattzap.model;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lt.overdrive.trackparser.domain.Track;
import lt.overdrive.trackparser.domain.TrackPoint;
import lt.overdrive.trackparser.domain.Trail;
import lt.overdrive.trackparser.parsing.ParserException;
import lt.overdrive.trackparser.parsing.tcx.TcxParser;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jfree.data.xy.XYSeries;

import com.gpxcreator.gpxpanel.GPXFile;
import com.wattzap.controller.MessageBus;
import com.wattzap.controller.Messages;
import com.wattzap.model.dto.Point;
import com.wattzap.model.dto.TrainingData;
import com.wattzap.model.dto.TrainingItem;


/*
 * Wrapper class for Tacx Real Life Video Routes
 * 
 * @author David George (c) Copyright 2013
 * @author PiR43
 * @date 19 November 2013
 */
@RouteAnnotation
public class RLVReader extends RouteReader {
	public static final int RLV_VIDEO_INFO = 2010;
	public static final int RLV_FRAME_DISTANCE_MAPPING = 2020;
	public static final int RLV_INFOBOX = 2030;
	public static final int RLV_COURSE_INFO = 2040;
	public static final int RLV_FINGERPRINT = 2000; // RLV (.rlv)
	public static final int PGMF_FINGERPRINT = 1000;
	public static final int PGMF_INFORMATION = 1010;
	public static final int PGMF_PROGRAM = 1020;

	private Charset iso88591charset = Charset.forName("ISO-8859-1");
	private double totalDistance = 0.0;

	private String fileName;
	private String courseName;
	private float frameRate;
	private float orgRunWeight;
	private long frameOffset;
	private float altitudeStart;
	private double maxSlope;
	private double minSlope;
	private XYSeries series;
	private float maxPower;

	private int programType;

	// Time / Distance based
	int timeDist;
	public final static int TIME = 0;
	public final static int DISTANCE = 1;

	private static Logger logger = LogManager.getLogger("RLV Reader");

	@Override
	public String getExtension() {
		return "rlv";
	}

	@Override
	public int routeType() {
		return programType;
	}

	@Override
	public String getFilename() {
		// TODO Auto-generated method stub
		return fileName;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return courseName;
	}

	@Override
	public GPXFile getGpxFile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public XYSeries getSeries() {
		return series;
	}

	@Override
	public Point[] getPoints() {
		return points;
	}

	@Override
	public double getDistanceMeters() {
		return totalDistance;
	}

	@Override
	public double getMaxSlope() {
		return maxSlope;
	}

	@Override
	public double getMinSlope() {
		return minSlope;
	}

	@Override
	public void load(String filename) {
		//logger.setLevel(Level.DEBUG);
		totalDistance = 0.0;
		maxSlope = 0;
		minSlope = 0;
		maxPower = 0;

		filename = filename.substring(0, filename.lastIndexOf('.'));
		this.fileName = filename;
		this.series = new XYSeries("");

		ArrayList<Point> pgmfSegment = readPGMF(filename + ".pgmf");
		ArrayList<Point> rlvSegment = readRLV(filename + ".rlv");
		ArrayList<Point> tcxSegment = readTCX(filename + ".tcx");

		if(tcxSegment != null && tcxSegment.size() > 0){
			double pgmfDist = pgmfSegment.get(pgmfSegment.size()-1).getDistanceFromStart();
			double tcxDist = tcxSegment.get(tcxSegment.size()-1).getDistanceFromStart();
			logger.debug("pgmd Dist: "+pgmfDist+" tcx distance: "+tcxDist);
			for(int i=0; i<tcxSegment.size(); i++){
				tcxSegment.get(i).setDistanceFromStart(tcxSegment.get(i).getDistanceFromStart()*pgmfDist/tcxDist);
			}
			tcxDist = tcxSegment.get(tcxSegment.size()-1).getDistanceFromStart();
			logger.debug("pgmd Dist: "+pgmfDist+" tcx distance: "+tcxDist);
		}

		/*
		 * merge arrays, we basically go through the frame/distance array and
		 * add speed and time data to the slope/distance array.
		 */
		long lastFrame = 0;
		double runningDistance = 0.0;
		double speed = 0.0;
		long startTime = 0;
		switch (timeDist) {
		case TIME:
			logger.debug("RLV mode TIME " + timeDist);
			ArrayList<Point> mergedPoints = new ArrayList<Point>();
			TrainingData tData = new TrainingData();
			tData.setPwr(true);
			Iterator<Point> rlvI = rlvSegment.iterator();

			Point rlvP = null;
			if (rlvI.hasNext()) {
				rlvP = rlvI.next();
			}
			// RLV Record is: frame No and distance per Frame
			long frame = rlvP.getTime();
			long framesInRecord = frame - lastFrame;
			double distance = rlvP.getDistanceFromStart() * framesInRecord;
			speed = (distance * 3.6 * frameRate) / framesInRecord;
			startTime += framesInRecord * 1000 / frameRate;

			double normPower = 1;
			if (UserPreferences.INSTANCE.isVirtualPower()) {
				// set max power level to top of Level 5, VO2Max
				normPower = 1.2 * (UserPreferences.INSTANCE.getMaxPower() / maxPower);
			}
			// 40% of FTP
			float recoveryPower = UserPreferences.INSTANCE.getMaxPower() * 0.4f;
			if (recoveryPower == 0) {
				recoveryPower = 75;
			}

			double interDistance = 0;
			long interTime = 0;

			for (int c1 = 0; c1 < pgmfSegment.size();) {
				// Duration Seconds
				// Power Watts
				Point p = pgmfSegment.get(c1);
				if ((p.getDistanceFromStart() * 1000) > startTime) {
					if (rlvI.hasNext()) {
						runningDistance += distance;

						// Normalize power
						rlvP.setPower(p.getPower() * normPower);
						if (rlvP.getPower() < recoveryPower) {
							// put a floor on minimum power
							rlvP.setPower(recoveryPower);
						}

						rlvP.setSpeed(speed);
						// interDistance = (speed * (startTime - interTime)) /
						// (3600);
						rlvP.setDistanceFromStart(runningDistance);
						rlvP.setTime(startTime);
						interTime = startTime;

						rlvP.setLongitude(90);
						mergedPoints.add(rlvP);

						rlvP = rlvI.next();
						frame = rlvP.getTime();
						framesInRecord = frame - lastFrame;
						lastFrame = frame;
						startTime += framesInRecord * 1000 / frameRate;
						distance = rlvP.getDistanceFromStart() * framesInRecord;
						speed = (distance * 3.6 * frameRate) / framesInRecord;

						continue;
					}
				}
				// Normalize power
				p.setPower(p.getPower() * normPower);
				if (p.getPower() < recoveryPower) {
					// put a floor on minimum power
					p.setPower(recoveryPower);
				}

				p.setTime((long) (p.getDistanceFromStart() * 1000));
				interDistance = (speed * (p.getTime() - interTime)) / (3600);
				p.setDistanceFromStart(runningDistance + interDistance);
				p.setSpeed(speed);
				p.setLatitude(90);
				p.setElevation(0);
				mergedPoints.add(p);

				c1++;
			}

			totalDistance = mergedPoints.get(mergedPoints.size() - 1)
					.getDistanceFromStart();
			points = mergedPoints.toArray(new Point[mergedPoints.size()]);

			// Create Training Data from merged segments
			TrainingItem item = new TrainingItem();
			item.setPower(-1);
			for (Point p : points) {
				if (item.getPower() != ((int) p.getGradient())) {
					if (item.getPower() == -1) {
						item.setPower(p.getGradient());
					} else {
						// item.setDescription("To "
						// + String.format("%.2f",
						// p.getDistanceFromStart() / 1000)
						// + " km");
						item.setDistanceMeters(p.getDistanceFromStart());
						tData.addItem(item);
						item = new TrainingItem();
						item.setPower(p.getGradient());
					}
				}

			}
			if (tData.getTraining().size() > 0) {
				MessageBus.INSTANCE.send(Messages.TRAINING, tData);
			}
			break;
		case DISTANCE:

			logger.debug("RLV mode DISTANCE " + timeDist);
			int c1 = 0;
				
			Point tcxPoint = null;
			Point lastTcxPoint = null;
			int c3 = 0;

			// for each RLV Point
			for (int c2 = 0; c2 < rlvSegment.size(); c2++) {
				Point rlvPoint = rlvSegment.get(c2);
				// point.time contains the frame number
				frame = rlvPoint.getTime();

				framesInRecord = frame - lastFrame;
				distance = rlvPoint.getDistanceFromStart() * framesInRecord;

				speed = (distance * 3.6 * frameRate) / framesInRecord;
				// Merge with PGMF Points
				
				double d = 0;
				while (c1 < pgmfSegment.size()) {
					Point pgmfPoint = pgmfSegment.get(c1);
					if (pgmfPoint.getDistanceFromStart() > (runningDistance + distance)
							&& c2 != rlvSegment.size() - 1) {
						// if we are not the last point and we are passed the
						// current slope point distance
						break;
					}
					pgmfPoint.setSpeed(speed);
					// t = d / s
					d = pgmfPoint.getDistanceFromStart()
							- runningDistance;
					long time = (long) (d * 3600 / speed);

					time += startTime;
					pgmfPoint.setTime(time);

					//put position from tcx
					if(tcxSegment != null && tcxSegment.size() > 0){
						if(tcxPoint == null) tcxPoint = tcxSegment.get(0);
						while(tcxPoint.getDistanceFromStart() < pgmfPoint.getDistanceFromStart() && c3 < tcxSegment.size() -1 ){
							lastTcxPoint = tcxPoint;
							tcxPoint = tcxSegment.get(++c3);
						}
						if(lastTcxPoint == null || tcxPoint.getDistanceFromStart() == lastTcxPoint.getDistanceFromStart()){
							pgmfPoint.setLatitude(tcxPoint.getLatitude());
							pgmfPoint.setLongitude(tcxPoint.getLongitude());
						} else {
							double dist = tcxPoint.getDistanceFromStart() - lastTcxPoint.getDistanceFromStart();
						logger.debug("last: "+lastTcxPoint.getDistanceFromStart()+" pgmf: "+pgmfPoint.getDistanceFromStart()+" point:"+tcxPoint.getDistanceFromStart());
							pgmfPoint.setLatitude((tcxPoint.getLatitude()*(dist - (tcxPoint.getDistanceFromStart() - pgmfPoint.getDistanceFromStart())) + lastTcxPoint.getLatitude()*(dist -(pgmfPoint.getDistanceFromStart() - lastTcxPoint.getDistanceFromStart())) )/dist);
							pgmfPoint.setLongitude((tcxPoint.getLongitude()*(dist - (tcxPoint.getDistanceFromStart() - pgmfPoint.getDistanceFromStart())) + lastTcxPoint.getLongitude()*(dist-(pgmfPoint.getDistanceFromStart() - lastTcxPoint.getDistanceFromStart())) )/dist);
						}
					}
					


					logger.debug("c2:"+c2+" c1:"+c1+" frame: "+frame+" distanceFromstart: "+pgmfPoint.getDistanceFromStart()+" time: " + time + " distance (frameInRecord): " + distance+ " framesInRecord: "+framesInRecord+" speed: "+speed+ " lat:"+pgmfPoint.getLatitude()+ " lon:"+pgmfPoint.getLongitude());

					c1++;
				}// while

				runningDistance += distance;
				startTime = (long) ((float) (frame * 1000) / frameRate);
				lastFrame = frame;
			}
			points = pgmfSegment.toArray(new Point[pgmfSegment.size()]);
			break;
		}// switch
	}

	public void close() {
	}

	private final ArrayList<Point> readTCX(String fileName) {

		ArrayList<Point> pl = new ArrayList<Point>();

		try{
			TcxParser parser = new TcxParser();
			Trail trail = parser.parse(new File(fileName));
			logger.debug("tcx tracks: "+trail.getTracks().size());
			List<Track> tracks = trail.getTracks();
			Point last = null;
			for(Track track : tracks){
				List<TrackPoint> points = track.getPoints();
				for(TrackPoint point : points){
					Point p = new Point();
					p.setLatitude(point.getLatitude());
					p.setLongitude(point.getLongitude());
					p.setElevation(point.getAltitude());
					if(last != null){
						double leg = GPXReader.distance(p.getLatitude(), last.getLatitude(), p.getLongitude(),
                                                	last.getLongitude(), last.getElevation(), p.getElevation());
						p.setDistanceFromStart(last.getDistanceFromStart()+leg);
					} else {
						p.setDistanceFromStart(0);
					}
					pl.add(p);
					last = p;
				}
			}
		} catch (ParserException pe) {
			StringWriter sw = new StringWriter();
			try{
				pe.getCause().printStackTrace(new PrintWriter(sw));
			} catch(Exception e){}
			String exceptionAsString = sw.toString();
			logger.error("ParserException : " + pe + ":"+ exceptionAsString + " file:"+fileName);
		}
		return pl;
	}

	private final ArrayList<Point> readPGMF(String fileName) {

		ArrayList<Point> p = new ArrayList<Point>();
		maxPower = 0f;

		try {
			// create FileInputStream object
			FileInputStream fin = new FileInputStream(fileName);

			/*
			 * To create DataInputStream object, use DataInputStream(InputStream
			 * in) constructor.
			 */

			DataInputStream din = new DataInputStream(fin);

			/*
			 * To read a Java short primitive from file, use byte readShort()
			 * method of Java DataInputStream class.
			 * 
			 * This method reads 2 bytes and returns it as a short value.
			 */

			int v = readLEShort(din);
			if (v != PGMF_FINGERPRINT) {
				throw new RuntimeException("Not an PGMF file " + v);
			}

			v = readLEShort(din);

			// record size
			v = readLEShort(din);
			// number of records
			v = readLEShort(din);

			while (din.available() > 0) {

				int recordType = readLEShort(din);
				switch (recordType) {
				case PGMF_INFORMATION:
					readPGMFInfo(din);
					break;
				case PGMF_PROGRAM:
					List<Point> segment = readPGMFProgram(din);
					p.addAll(segment);
					break;

				default:
					logger.error("Wrong record type for offset " + recordType);
				}
			}

			din.close();
		} catch (FileNotFoundException fe) {
			logger.error("FileNotFoundException : " + fe);
		} catch (IOException ioe) {
			logger.error("IOException : " + ioe);
		}

		return p;
	}

	/*
	 * 12 byte record has 3 variables :
	 * 
	 * Slope (Integer) 0 = Watt program, 1 = Slope program, 2 = Pulse (HR)
	 * program
	 * 
	 * TimeDist (Integer) 0 = Time based program, 1 = distance based program
	 * 
	 * TotalTimeDist Double Total time (in seconds) or distance (in metres) of
	 * complete program
	 * 
	 * Note: for RLV runs, the TotalTimeDist is the distance for the entire RLV,
	 * not just the section that this run relates to.
	 * http://www.whitepeak.org/FortiusFile.aspx?file=.pgmf
	 */
	private void readPGMFInfo(DataInputStream din) throws IOException {
		int version = readLEShort(din);
		logger.debug("*** Read Course Info Version : " + version);

		long rib = readLEInt(din);
		logger.debug("Number of records in the block: " + rib);

		long sob = readLEInt(din);
		logger.debug("Size of Records in block: " + sob);

		for (int r = 0; r < rib; r++) {

			long cSum = readLEInt(din);

			courseName = readTacxString(din, 34);
			logger.debug("Course name " + courseName);

			programType = readLEInt(din);
			switch (programType) {
			case POWER:
				logger.debug("Watt Program");
				break;
			case SLOPE:
				logger.debug("Slope Program");
				break;
			case HEARTRATE:
				logger.debug("Pulse(HR) Program");
				break;
			default:
				logger.error("Error in PGFM Program Type: " + programType);
			}

			timeDist = readLEInt(din);
			double totalTimeDist = readLittleDouble(din);
			switch (timeDist) {
			case TIME:
				logger.debug("Time Based " + totalTimeDist);
				break;
			case DISTANCE:
				logger.debug("Distance Based " + totalTimeDist);
				break;
			default:
				logger.error("Error in PGFM Base Type: " + timeDist);
			}

			double energyCons = readLittleDouble(din);
			logger.debug("EnergyCons: " + energyCons);

			altitudeStart = readLittleFloat(din);
			logger.debug("AltitudeStart: " + altitudeStart);

			long brakeCategory = readLEInt(din);
			logger.debug("BrakeCategory: " + brakeCategory);

		}
	}

	/*
	 * @value DurationDistance Float Seconds or metres, depending on program
	 * type
	 * 
	 * @value PulseSlopeWatts Float Pulse, slope or watts data, depending on
	 * program type
	 * 
	 * @value RollingFriction Float
	 */
	private final List<Point> readPGMFProgram(DataInputStream din)
			throws IOException {
		int version = readLEShort(din);

		logger.debug("*** Read Course Info Version : " + version);

		long rib = readLEInt(din);
		long sob = readLEInt(din);

		float altitudeMin = 0;
		float altitude = 0.0f;
		ArrayList<Point> segment = new ArrayList<Point>();
		Point p = new Point();
		p.setElevation(altitudeStart);
		p.setDistanceFromStart(0);
		segment.add(p);
		series.add(0, altitudeStart);
		float totalClimbing = 0.0f;
		float totalDescent = 0.0f;

		for (int r = 0; r < rib; r++) {
			// distance or duration in seconds depending on the program
			float distanceMeters = readLittleFloat(din);

			// slope or power
			float slope = 0f;
			slope = readLittleFloat(din);

			switch (programType) {
			case SLOPE:
				// slope program
				totalDistance += distanceMeters;

				break;

			case POWER:
				// WATTS
				// time!!!!
				totalDistance += distanceMeters;

				if (slope > maxPower) {
					maxPower = slope;
				}

				break;
			}

			float rise = slope * (distanceMeters / 100.0f);
			altitude += rise;
			if (rise > 0) {
				totalClimbing += rise;
			} else {
				totalDescent += rise;
			}

			if (altitude < altitudeMin) {
				altitudeMin = altitude; // dunno what to do with this.
			}

			float rollingFriction = readLittleFloat(din);
			/*
			 * logger.debug("distanceMeters: " + distanceMeters + " slope: " +
			 * slope + " rise: " + rise + " altitude " + altitude +
			 * " RollingFriction: " + rollingFriction);
			 */
			series.add(totalDistance / 1000, altitudeStart + altitude);

			if (r == 0) {
				// first time thru' set up initial point
				p.setGradient(slope);
			}

			p = new Point();

			p.setGradient(slope);

			if (slope > maxSlope) {
				maxSlope = slope;
			}
			if (slope < minSlope) {
				minSlope = slope;
			}
			p.setElevation(altitudeStart + altitude);
			p.setDistanceFromStart(totalDistance);
			segment.add(p);
		}// for

		logger.info("PGFM Total Records " + rib + " Total Distance "
				+ totalDistance + " Start Altitude " + altitudeStart
				+ " Climb " + totalClimbing + " Descent " + totalDescent);
		return segment;
	}

	/*** Real Life Video ***/

	private final ArrayList<Point> readRLV(String file) {
		ArrayList<Point> p = new ArrayList<Point>();
		DataInputStream din = null;

		try {
			// create FileInputStream object
			FileInputStream fin = new FileInputStream(file);

			/*
			 * To create DataInputStream object, use DataInputStream(InputStream
			 * in) constructor.
			 */

			din = new DataInputStream(fin);

			/*
			 * To read a Java short primitive from file, use byte readShort()
			 * method of Java DataInputStream class.
			 * 
			 * This method reads 2 bytes and returns it as a short value.
			 */

			int v = readLEShort(din);

			if (v != RLV_FINGERPRINT) {
				throw new RuntimeException("Not an RLV file");
			}

			v = readLEShort(din);
			v = readLEShort(din);
			v = readLEShort(din);

			while (din.available() > 0) {

				int recordType = readLEShort(din);

				switch (recordType) {
				case RLV_VIDEO_INFO:
					readRLVInfo(din);
					break;
				case RLV_FRAME_DISTANCE_MAPPING:
					ArrayList<Point> segment = readFrameDistanceMapping(din);
					p.addAll(segment);
					break;
				case RLV_INFOBOX:
					readInfoBox(din);
					break;
				case RLV_COURSE_INFO:
					readCourseInfo(din);
					break;

				default:
					logger.error("something wrong here " + recordType);
				}
			}
		} catch (FileNotFoundException fe) {
			logger.error("FileNotFoundException : " + fe);
		} catch (IOException ioe) {
			logger.error("IOException : " + ioe);
		} finally {
			try {
				din.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return p;
	}

	private void readRLVInfo(DataInputStream din) throws IOException {

		int version = readLEShort(din);
		logger.info("Version : " + version);

		long rib = readLEInt(din);
		logger.info("Number of records in the block: " + rib);

		long sob = readLEInt(din);
		logger.info("Size of Records in block: " + sob);

		// descriptive name
		String fileName = readTacxString(din, 522);
		logger.info("filename " + fileName);

		frameRate = readLittleFloat(din);
		System.out.println("frame rate " + frameRate);
		logger.info("Frame rate: " + frameRate);

		orgRunWeight = readLittleFloat(din);
		logger.info("OrgRunWeight: " + orgRunWeight);

		frameOffset = readLEInt(din);
		logger.info("FrameOffset: " + frameOffset);
	}

	/*
	 * RLV Record contains: frame No and distance per Frame
	 */
	private final ArrayList<Point> readFrameDistanceMapping(DataInputStream din)
			throws IOException {
		int version = readLEShort(din);
		long rib = readLEInt(din);
		long sob = readLEInt(din);

		double distance = 0;
		long lastFrame = 0;
		ArrayList<Point> segment = new ArrayList<Point>();
		for (int r = 0; r < rib; r++) {
			long frame = readLEInt(din);
			float distancePerFrame = readLittleFloat(din);
			if (frame == 0) {
				continue; // no useful information

			}
			Point p = new Point();
			p.setDistanceFromStart(distancePerFrame);
			distance += (frame - lastFrame) * distancePerFrame;
			lastFrame = frame;
			p.setTime(frame);
			segment.add(p);

		}
		logger.debug("### RLV No of records " + rib + " version " + version
				+ " total distance " + distance);

		return segment;
	}

	/*
	 * RLV InfoBox info (2030)
	 * 
	 * This block looks like it may contain the infobox information, i.e. the
	 * frame number at which to popup the appropriate infobox from the
	 * CmdList.txt file.
	 * 
	 * Record format: Contents Version 100 Field uShort Number of Records in
	 * Block (uInt) Size of Records in Block (uInt)
	 * 
	 * [Frame Int32, Command Int32,...]
	 */
	private void readInfoBox(DataInputStream din) throws IOException {
		int version = readLEShort(din);
		logger.info("Read Info Box Version : " + version);

		long rib = readLEInt(din);
		logger.info("Number of records in the block: " + rib);

		long sob = readLEInt(din);
		logger.info("Size of Records in block: " + sob);

		for (int r = 0; r < rib; r++) {

			long frame = readLEInt(din);
			logger.info("Frame: " + frame);
			long command = readLEInt(din);
			logger.info("Cmd: " + command);

		}

	}

	/*
	 * Course information (2040)
	 * 
	 * Record format: Contents Version 100 Field uShort Number of Records in
	 * Block (uInt) Size of Records in Block (uInt)
	 * 
	 * Start (float) Start of route section (time (s) or distance (m), depending
	 * on program type) End (float) End of route section (time (s) or distance
	 * (m), depending on program type) CourseSegmentName 66 Char[] TextFile 522
	 * Char[]
	 */
	private void readCourseInfo(DataInputStream din) throws IOException {
		int version = readLEShort(din);
		logger.info("*** Read Course Info Version : " + version);

		long rib = readLEInt(din);
		logger.info("Number of records in the block: " + rib);

		long sob = readLEInt(din);
		logger.info("Size of Records in block: " + sob);

		for (int r = 0; r < rib; r++) {
			float start = readLittleFloat(din);
			float end = readLittleFloat(din);

			String courseSegmentName = readTacxString(din, 66);
			logger.info("name " + courseSegmentName);

			String textFile = readTacxString(din, 522);
			logger.info("textFile " + textFile);

		}

	}

	private int readLEShort(DataInputStream din) throws IOException {
		int b1 = din.readUnsignedByte();
		int b2 = din.readUnsignedByte();

		return (b2 << 8) + b1;
	}

	public static int readLEInt(DataInputStream dataInStream)
			throws IOException {
		byte[] byteBuffer = new byte[4];

		dataInStream.readFully(byteBuffer, 0, 4);
		return (byteBuffer[3]) << 24 | (byteBuffer[2] & 0xff) << 16
				| (byteBuffer[1] & 0xff) << 8 | (byteBuffer[0] & 0xff);
	}

	public float readLittleFloat(DataInputStream dis) throws IOException {
		return Float.intBitsToFloat(readLEInt(dis));
	}

	public long readLELong(DataInputStream dataInStream) throws IOException {
		byte[] byteBuffer = new byte[8];

		dataInStream.readFully(byteBuffer, 0, 8);
		return (byteBuffer[7]) << 56 | (byteBuffer[6] & 0xff) << 48
				| (byteBuffer[5] & 0xff) << 40 | (byteBuffer[4] & 0xff) << 32
				| (byteBuffer[3] & 0xff) << 24 | (byteBuffer[2] & 0xff) << 16
				| (byteBuffer[1] & 0xff) << 8 | (byteBuffer[0] & 0xff);
	}

	public double readLittleDouble(DataInputStream dis) throws IOException {
		return Double.longBitsToDouble(readLELong(dis));
	}

	/*
	 * Input is ISO-8859-1 in 2 bytes
	 */
	private String readTacxString(DataInputStream dis, int len)
			throws IOException {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len; i += 2) {
			int b1 = dis.readUnsignedByte();
			int b2 = dis.readUnsignedByte();
			if (b1 == 0 && b2 == 0) {
				continue;
			}
			ByteBuffer inputBuffer = ByteBuffer.wrap(new byte[] { (byte) b1,
					(byte) b2 });
			CharBuffer cb = iso88591charset.decode(inputBuffer);
			sb.append(cb.get(0));
		}
		return sb.toString();

	}
}

