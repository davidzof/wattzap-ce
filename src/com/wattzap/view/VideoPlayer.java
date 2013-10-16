package com.wattzap.view;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.DefaultFullScreenStrategy;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.FullScreenStrategy;

import com.wattzap.model.GPXData;
import com.wattzap.model.dto.Point;
import com.wattzap.model.dto.Telemetry;
import com.wattzap.utils.Rolling;

public class VideoPlayer extends JFrame implements ChangeListener,
		ActionListener {
	private static final long serialVersionUID = 8813937587710441310L;
	private EmbeddedMediaPlayer mPlayer;
	private MediaPlayerFactory mediaPlayerFactory;
	private Canvas canvas;

	long startTime = 0;
	long mapStartTime;
	long lastMapTime;
	long lastVideoTime;
	GPXData gpxData;
	long len;
	float fps;
	Odometer odo;
	JFrame mainFrame;
	boolean videoLoaded;
	Rolling rSpeed;

	private static Logger logger = LogManager.getLogger(VideoPlayer.class
			.getName());

	public VideoPlayer(JFrame main, Odometer odo) {
		super();

		this.odo = odo;
		this.mainFrame = main;

		setTitle("Video");
		ImageIcon img = new ImageIcon("icons/video.jpg");
		setIconImage(img.getImage());

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

		setLocation(100, 100);
		setSize(800, 600);
		setVisible(false);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (!videoLoaded || gpxData == null) {
			return;
		}

		Telemetry t = (Telemetry) e.getSource();
		if (startTime == 0) {
			// first time through, start video
			startTime = System.currentTimeMillis();
			mPlayer.start();
			mPlayer.enableOverlay(true);
			mPlayer.mute();
			fps = mPlayer.getFps();
			len = mPlayer.getLength();

			float time = len / fps;
			// s = d / t
			// we have the average speed of clip and our current speed, set
			// initial rate
			logger.debug("FPS " + fps + " length " + len
					+ " milliSeconds time " + time);

			mPlayer.setRate(1.0f); // initial rate

			Point p = gpxData.getCoords(t.getDistance());
			mapStartTime = p.getTime();
			lastMapTime = 0;
			lastVideoTime = (int) (len * mPlayer.getPosition());
			return;
		}
		if (mPlayer.isPlaying() == false) {
			mPlayer.start();
		}
		if (t.getSpeed() == 0.0) {
			mPlayer.pause();
		}

		Point p = gpxData.getCoords(t.getDistance());

		long mapTime = p.getTime() - mapStartTime;

		if (mapTime != lastMapTime) {
			// position update
			rSpeed.add(p.getSpeed());

			logger.debug(String.format(
					"real speed  %.2f smooth speed %.2f position %f rate %.2f",
					p.getSpeed(), rSpeed.getAverage(), mPlayer.getPosition(),
					mPlayer.getRate()));

			// double rate = t.getSpeed() / p.getSpeed();
			double rate = t.getSpeed() / rSpeed.getAverage();

			long videoTime = (int) (len * mPlayer.getPosition());
			if (videoTime == 0) {
				return;
			}

			if (mapTime > (videoTime + 250)) {
				// this is an accelerator
				float diff = ((float) mapTime - videoTime) / 10000.0f;
				// maptime is increasing faster than video time, up video rate

				rate += diff;
				if (videoTime < 10000) {
					if (rate > 1.5) {
						rate = 1.5;
					}
				}
				BigDecimal bd = new BigDecimal(rate).setScale(2,
						RoundingMode.HALF_UP);

				logger.debug(String
						.format("Video too slow mapTime %d videoTime %d rate %.2f diff %f tspeed %.2f pspeed %.2f",
								mapTime, videoTime, rate, diff, t.getSpeed(),
								rSpeed.getAverage()));
				logger.debug("set rate " + bd.floatValue());
				mPlayer.setRate(bd.floatValue());
			} else if (videoTime > (mapTime + 250)) {
				/*
				 * We are behind the video time so we need to slow the video
				 * down but not too drastically.
				 */
				//if (rate > 1.0) {
					//rate = 1.0;
				//}
				float diff = ((float) videoTime - mapTime) / 10000.0f;
				rate -= diff;
				if (rate < 0.0) {
					rate = 0.1;
				}
				BigDecimal bd = new BigDecimal(rate).setScale(2,
						RoundingMode.HALF_UP);

				logger.debug(String
						.format("Video too fast mapTime %d videoTime %d rate %.2f diff %f tspeed %.2f pspeed %.2f",
								mapTime, videoTime, rate, diff, t.getSpeed(),
								rSpeed.getAverage()));

				mPlayer.setRate(bd.floatValue());
			}

			lastMapTime = mapTime;
			lastVideoTime = videoTime;
		}

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		logger.debug("Video Player " + command);
		if ("stop".equals(command)) {
			if (mPlayer != null) {
				// mPlayer.stop();
				logger.debug("Pausing video player");
				mPlayer.pause();

				// mPlayer.setRate(0.0f);
			}
		} else if ("start".equals(command)) {

		} else if ("gpxload".equals(command)) {
			gpxData = (GPXData) e.getSource();
			startTime = 0;
			rSpeed = new Rolling(10);
			String videoFile = gpxData.getFilename();

			videoFile = videoFile.substring(0, videoFile.lastIndexOf('.'));

			// mPlayer = mediaPlayerComponent.getMediaPlayer();
			videoFile += ".avi";

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
		}
	}
}
