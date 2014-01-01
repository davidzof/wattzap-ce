package com.wattzap.view;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Rectangle;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.DefaultFullScreenStrategy;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.FullScreenStrategy;

import com.wattzap.controller.MessageBus;
import com.wattzap.controller.MessageCallback;
import com.wattzap.controller.Messages;
import com.wattzap.model.RLVReader;
import com.wattzap.model.RouteReader;
import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.Point;
import com.wattzap.model.dto.Telemetry;
import com.wattzap.utils.Rolling;

/**
 * (c) 2013 David George / TrainingLoops.com
 * 
 * Speed and Cadence ANT+ processor.
 * 
 * @author David George
 * @date 11 June 2013
 */
public class VideoPlayer extends JFrame implements /* ChangeListener, */
MessageCallback {
	private static final long serialVersionUID = 8813937587710441310L;
	private EmbeddedMediaPlayer mPlayer;
	private MediaPlayerFactory mediaPlayerFactory;
	private Canvas canvas;

	long startTime = 0;
	long mapStartTime;
	long lastMapTime;
	long lastVideoTime;
	RouteReader routeData;
	long len;
	float fps;
	Odometer odo;
	JFrame mainFrame;
	boolean videoLoaded;
	Rolling rSpeed;
	float diff;
	float lastRate;
	float lastCent;

	private static Logger logger = LogManager.getLogger("Video Player");

	public VideoPlayer(JFrame main, Odometer odo) {
		super();

		this.odo = odo;
		this.mainFrame = main;

		setTitle("Video - www.WattzAp.com");
		ImageIcon img = new ImageIcon("icons/video.jpg");
		setIconImage(img.getImage());

		/* Messages we are interested in */
		MessageBus.INSTANCE.register(Messages.SPEEDCADENCE, this);
		MessageBus.INSTANCE.register(Messages.STARTPOS, this);
		MessageBus.INSTANCE.register(Messages.STOP, this);
		MessageBus.INSTANCE.register(Messages.CLOSE, this);
		MessageBus.INSTANCE.register(Messages.GPXLOAD, this);
	}

	public void init() {
		mediaPlayerFactory = new MediaPlayerFactory();
		canvas = new java.awt.Canvas();
		canvas.setBackground(Color.BLACK);
		this.add(canvas, java.awt.BorderLayout.CENTER);
		mediaPlayerFactory.newVideoSurface(canvas);

		FullScreenStrategy fullScreenStrategy = new DefaultFullScreenStrategy(
				this);
		mPlayer = mediaPlayerFactory.newEmbeddedMediaPlayer(fullScreenStrategy);
		mPlayer.setVideoSurface(mediaPlayerFactory.newVideoSurface(canvas));

		setBounds(UserPreferences.INSTANCE.getVideoBounds());

		setVisible(false);
	}

	private void setSpeed(Telemetry t) {

		Point p = routeData.getPoint(t.getDistance());

		if (startTime == 0) {
			// first time through, start video
			startTime = System.currentTimeMillis();
			mPlayer.start();
			mPlayer.enableOverlay(true);
			mPlayer.mute();
			fps = mPlayer.getFps();
			len = mPlayer.getLength();
			diff = 1.0f;
			float time = len / fps;
			// s = d / t
			// we have the average speed of clip and our current speed, set
			// initial rate
			logger.debug("FPS " + fps + " length " + len
					+ " milliSeconds time " + time);

			t.getSpeed();
			p.getSpeed();
			rSpeed.add(p.getSpeed());
			double rate = t.getSpeed() / p.getSpeed();
			if (rate > 1.0) {
				rate = 1.0f;
			}

			// sets position to between 0 - 1 (end)
			if (mapStartTime > 0) {
				float pos = (float) mapStartTime / len;
				System.out.println("start time " + mapStartTime
						+ " total time " + len + " pos " + pos);
				mPlayer.setPosition(pos);

			}
			mPlayer.setRate((float) rate); // initial rate
			mapStartTime -= p.getTime();
			lastMapTime = 0 + mapStartTime;
			lastCent = 1.0f;
			return;
		}
		if (mPlayer.isPlaying() == false) {
			mPlayer.start();
		}
		if (t.getSpeed() == 0.0) {
			mPlayer.pause();
		}

		long mapTime = p.getTime() - mapStartTime;

		long videoTime = (int) (len * mPlayer.getPosition());
		if (videoTime == 0) {
			return;
		}

		if (mapTime != lastMapTime) {
			// position update
			rSpeed.add(p.getSpeed());
			float perCent = 1.0f;
			if (mapTime > videoTime + 250) {
				perCent = ((float) videoTime / mapTime);
				// map too fast
				System.out.println("map too fast, speed up video");
				if (perCent > lastCent) {
					System.out.println("rate of change decreasing");
					diff -= 0.01f;
				} else {
					System.out.println("rate of change increasing");
					// rate of change is increasing
					if (perCent < 0.9 || perCent > 1.1) {
						diff += 0.06f;
					} else {
						diff += 0.03f;
					}
				}
			} else if (videoTime > mapTime + 250) {
				perCent = ((float) mapTime / videoTime);

				System.out.println("video too fast, speed up map");
				if (perCent < lastCent) {
					System.out.println("rate of change increasing");
					if (perCent < 0.9 || perCent > 1.1) {
						diff -= 0.06f;
					} else {
						diff -= 0.03f;
					}
				} else {
					System.out.println("rate of change decreasing");

					diff += 0.01f;
				}
			}

			if (diff > 1.2) {
				diff = 1.2f;
			} else if (diff < 0.8) {
				diff = 0.8f;
			}

			lastMapTime = mapTime;
			logger.debug(String
					.format("Map Speed  %.2f, Smoothed Map Speed %.2f, Turbo Speed %.2f MapTime %d VideoTime %d, perCent %.3f, lastCent %.3f",
							p.getSpeed(), rSpeed.getAverage(), t.getSpeed(),
							mapTime, videoTime, perCent, lastCent));
			lastCent = perCent;
		}

		double rate = 1.0;
		if (rSpeed.getAverage() > 10.0) {
			rate = t.getSpeed() / rSpeed.getAverage();
		}
		rate *= diff;

		BigDecimal bd = new BigDecimal(rate).setScale(2, RoundingMode.HALF_UP);
		if (lastRate != bd.floatValue()) {
			lastRate = bd.floatValue();
			logger.debug(String.format(
					"Position %f, Diff %.2f Rate %.2f Video-rate %.2f ",
					mPlayer.getPosition(), diff, lastRate, mPlayer.getRate()));
			mPlayer.setRate(lastRate);
		}
	}

	/*
	 * @Override public void stateChanged(ChangeEvent e) { if (!videoLoaded ||
	 * gpxData == null) { return; }
	 * 
	 * Telemetry t = (Telemetry) e.getSource();
	 */
	@Override
	public void callback(Messages message, Object o) {
		switch (message) {
		case SPEEDCADENCE:
			if (videoLoaded && routeData != null) {
				Telemetry t = (Telemetry) o;
				setSpeed(t);
			}
			break;
		case STOP:
			if (mPlayer != null) {
				// mPlayer.stop();
				logger.debug("Pausing video player");
				mPlayer.pause();

				// mPlayer.setRate(0.0f);
			}
			break;
		case START:
			break;
		case STARTPOS:
			double startDistance = (Double) o;
			if (routeData == null) {
				return;
			}
			Point p = routeData.getPoint(startDistance);
			mapStartTime = p.getTime();
			System.out
					.println("Map start time " + mapStartTime + " Point " + p);
			break;
		case CLOSE:
			// by default add to telemetry frame
			remove(odo);
			mainFrame.add(odo, "cell 0 2, grow");
			
			Rectangle r = getBounds();
			UserPreferences.INSTANCE.setVideoBounds(r);
			revalidate();
			mainFrame.revalidate();
			setVisible(false);
			mapStartTime = 0;
			break;
		case GPXLOAD:
			routeData = (RouteReader) o;
			startTime = 0;
			if (routeData.routeType() == RLVReader.SLOPE) {
				// smooth GPX type courses
				rSpeed = new Rolling(10);
			} else {
				rSpeed = new Rolling(1);
			}

			String videoFile = routeData.getFilename();
			// videoFile = videoFile.substring(0, videoFile.lastIndexOf('.'));
			// mPlayer = mediaPlayerComponent.getMediaPlayer();
			videoFile += ".avi";

			System.out.println("video file " + videoFile);

			if ((new File(videoFile)).exists()) {
				mainFrame.remove(odo);
				mainFrame.revalidate();
				mPlayer.enableOverlay(false);
				mPlayer.prepareMedia(videoFile);

				add(odo, java.awt.BorderLayout.SOUTH);
				videoLoaded = true;
				revalidate();
				setVisible(true);
			} else {
				videoLoaded = false;
				remove(odo);
				mainFrame.add(odo, "cell 0 2, grow");
				revalidate();
				mainFrame.revalidate();
				// mPlayer = null;
				setVisible(false);
			}

			break;
		}
	}
}