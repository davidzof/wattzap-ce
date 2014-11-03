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

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Rectangle;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import uk.co.caprica.vlcj.binding.internal.libvlc_marquee_position_e;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.DefaultFullScreenStrategy;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.FullScreenStrategy;

import com.wattzap.controller.MessageBus;
import com.wattzap.controller.MessageCallback;
import com.wattzap.controller.Messages;
import com.wattzap.model.RouteReader;
import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.Point;
import com.wattzap.model.dto.Telemetry;
import com.wattzap.utils.Rolling;

/**
 * (c) 2013-2014 David George / TrainingLoops.com
 * 
 * Video Player.
 * 
 * Synchronizes video playback to road speed. This is done using an SDK to the
 * cross platform VLC player. We can't set the speed frame by frame because it
 * would be too expensive in terms of CPU cycles. What we do is compare the
 * speed from the rider with the speed the video was recorded at and set the
 * playback speed according to this ratio. For example is the rider is doing
 * 10kph and the video was recorded at 20kpg we set the playback speed to 50%.
 * 
 * It sounds easy but both the rider speed and video record speed are constantly
 * varying so adjustments have to be made continuously. It is very easy to
 * overshoot hence the different offsets used.
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
	JPanel odo;
	JFrame mainFrame;
	boolean videoLoaded;
	Rolling rSpeed;
	Rolling rRate;
	float diff;
	float lastRate;
	float lastCent;

	private static Logger logger = LogManager.getLogger("Video Player");

	public VideoPlayer(JFrame main, JPanel odo) {
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
		Point p = routeData.getPoint(t.getDistanceKM());

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

			rSpeed.add(p.getSpeed());
			double rate = t.getSpeedKMH() / p.getSpeed();
			if (rate > 1.0) {
				rate = 1.0f;
			}

			// sets position to between 0 - 1 (end)
			if (mapStartTime > 0) {
				float pos = (float) mapStartTime / len;
				mPlayer.setPosition(pos);
			}

			mPlayer.setRate((float) rate); // initial rate
			// mapStartTime -= p.getTime();
			// mapStartTime = 0;
			lastMapTime = 0 + mapStartTime;
			lastCent = 1.0f;
			return;
		}
		if (mPlayer.isPlaying() == false) {
			mPlayer.start();
		}
		if (t.getSpeedKMH() == 0.0) {
			mPlayer.pause();
			return;
		}
		
		 //mPlayer.setMarqueeText("hello world");
         //mPlayer.setMarqueeOpacity(127);
         //mPlayer.setMarqueeColour(Color.RED);
         //mPlayer.setMarqueePosition(libvlc_marquee_position_e.centre);
         //mPlayer.enableMarquee(true);
         
		long mapTime = p.getTime();
		long videoTime = (int) (len * mPlayer.getPosition());
		if (videoTime == 0) {
			return;
		}

		if (mapTime != lastMapTime) {
			// position has changed
			rSpeed.add(p.getSpeed());
			float perCent = 1.0f;
			if (mapTime > videoTime + 250) {
				perCent = ((float) videoTime / mapTime);
				// map too fast
				//System.out.println("map too fast, speed up video");
				if (perCent > lastCent) {
					//System.out.println("rate of change decreasing");
					diff -= 0.01f;
				} else {
					//System.out.println("rate of change increasing");
					// rate of change is increasing
					if (perCent < 0.9 || perCent > 1.1) {
						diff += 0.06f;
					} else {
						diff += 0.03f;
					}
				}
			} else if (videoTime > mapTime + 250) {
				// video too fast
				perCent = ((float) mapTime / videoTime);

				//System.out.println("video too fast, speed up map");
				if (perCent < lastCent) {
					//System.out.println("rate of change increasing");
					if (perCent < 0.9 || perCent > 1.1) {
						// 10% out, use bigger changes
						diff -= 0.06f;
					} else {
						diff -= 0.03f;
					}
				} else {
					// rate of change is decreasing
					diff += 0.01f;
					//System.out.println("rate of change decreasing");
				}
			}

			if (diff > 1.25) {
				diff = 1.25f;
			} else if (diff < 0.8) {
				diff = 0.8f;
			}

			lastMapTime = mapTime;
			logger.debug(String
					.format("Map Speed  %.2f, Smoothed Map Speed %.2f, Turbo Speed %.2f MapTime %d VideoTime %d, perCent %.3f, lastCent %.3f",
							p.getSpeed(), rSpeed.getAverage(), t.getSpeedKMH(),
							mapTime, videoTime, perCent, lastCent));
			lastCent = perCent;
		}

		double rate = 1.0;
		if (rSpeed.getAverage() > 10.0) {
			rate = t.getSpeedKMH() / rSpeed.getAverage();
		}
		rate *= diff;

		BigDecimal bd = new BigDecimal(rate).setScale(2, RoundingMode.HALF_UP);
		if (lastRate != bd.floatValue()) {
			lastRate = bd.floatValue();
			rRate.add(lastRate);
			lastRate = (float) rRate.getAverage();
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

				if (mPlayer.isPlaying()) {
					logger.debug("Pausing video player");
					mPlayer.pause();
				}
			}
			break;
		case START:
			break;
		case STARTPOS:
			double startDistance = (Double) o;
			if (routeData == null) {
				return;
			}

			Point p = routeData.getAbsolutePoint(startDistance);
			if (p != null) {
				mapStartTime = p.getTime();
				if (mapStartTime > 0 && len > 0) {
					float pos = (float) mapStartTime / len;
					mPlayer.setPosition(pos);
				}
			}
			break;
		case CLOSE:
			// by default add to telemetry frame
			remove(odo);
			mainFrame.add(odo, "cell 0 2, grow");

			Rectangle r = getBounds();
			UserPreferences.INSTANCE.setVideoBounds(r);
			// revalidate();
			// mainFrame.revalidate();
			revalidate(this);
			revalidate(mainFrame);
			setVisible(false);
			mapStartTime = 0;
			if (routeData != null) {
				routeData.close();
				len = 0;
			}
			break;
		case GPXLOAD:
			routeData = (RouteReader) o;
			startTime = 0;
			if (routeData.routeType() == RouteReader.SLOPE) {
				// smooth GPX type courses
				rSpeed = new Rolling(10);
			} else {
				rSpeed = new Rolling(1);
			}
			rRate = new Rolling(5);

			String videoFile = routeData.getFilename();
			videoLoaded = false;
			String[] fileTypes = new String[] { ".avi", ".mp4", ".flv" };
			for (String ext : fileTypes) {
				if ((new File(videoFile + ext)).exists()) {
					mainFrame.remove(odo);
					mainFrame.repaint();
					mPlayer.enableOverlay(false);
					mPlayer.prepareMedia(videoFile + ext);

					add(odo, java.awt.BorderLayout.SOUTH);
					videoLoaded = true;
					// revalidate(); Java 1.7 code
					revalidate(this);
					setVisible(true);
					break;
				}
			}

			if (videoLoaded == false) {
				remove(odo);
				// mainFrame.revalidate(); Java 1.7 code
				revalidate(mainFrame);
				mainFrame.add(odo, "cell 0 2, grow");

				// revalidate();
				// mainFrame.revalidate();
				revalidate(this);
				revalidate(mainFrame);
				// mPlayer = null;
				setVisible(false);
			}

			break;
		}
	}

	private void revalidate(JFrame frame) {
		// frame.invalidate();
		frame.validate();
	}

}