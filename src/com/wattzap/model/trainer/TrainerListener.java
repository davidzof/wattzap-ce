/*
 * WattzAp external trainer input.
 *
 * Receives one JSON object per line over a localhost TCP connection,
 * converts it to a Telemetry object and publishes it on the WattzAp
 * MessageBus.
 */
package com.wattzap.model.trainer;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.wattzap.controller.MessageBus;
import com.wattzap.controller.MessageCallback;
import com.wattzap.controller.Messages;
import com.wattzap.model.RouteReader;
import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.Point;
import com.wattzap.model.dto.Telemetry;
import com.wattzap.model.power.Power;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TrainerListener implements MessageCallback {
    private final static Logger logger = LogManager.getLogger("SpeedListener");
    private final UserPreferences userPrefs = UserPreferences.INSTANCE;

    private double distance = 0.0;
    private double mass;
    private double wheelSize = userPrefs.getWheelSizeCM();
    private int resistance = userPrefs.getResistance();
    private Power power = userPrefs.getPowerProfile();
    RouteReader routeData;
    private boolean simulSpeed;
    private boolean isStarted;
    private long lastTime = 0;

    private final int port;
    private final Gson gson = new Gson();

    private volatile boolean running;
    private ServerSocket serverSocket;

    public TrainerListener(int port) {
        this.port = port;
        isStarted = false;

        MessageBus.INSTANCE.register(Messages.START, this);
        MessageBus.INSTANCE.register(Messages.STOP, this);
        MessageBus.INSTANCE.register(Messages.STARTPOS, this);
        MessageBus.INSTANCE.register(Messages.GPXLOAD, this);
    }

    public synchronized void start() {
        if (running) {
            return;
        }

        running = true;

        Thread listenerThread = new Thread(this::listen, "WattzAp-TrainerListener");

        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public synchronized void stop() {
        running = false;
        System.out.println("stop");

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void listen() {
        try {
            serverSocket = new ServerSocket();

            InetAddress localhost = InetAddress.getByName("127.0.0.1");
            serverSocket.bind(new InetSocketAddress(localhost, port));

            System.out.println("Trainer listener waiting on 127.0.0.1:" + port);

            while (running) {

                try (Socket socket = serverSocket.accept()) {
                    System.out.println("Trainer source connected: "
                            + socket.getRemoteSocketAddress());

                    readMessages(socket);

                } catch (IOException e) {
                    if (running) {
                        System.err.println("Trainer listener error: "
                                + e.getMessage());
                    }
                }
            }

        } catch (IOException e) {
            if (running) {
                System.err.println("Unable to start trainer listener: "
                        + e.getMessage());
            }
        } finally {
            running = false;

            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private void readMessages(Socket socket) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

            String line;

            while (running && (line = reader.readLine()) != null) {
                line = line.trim();

                System.out.println(line);
                if (!line.isEmpty()) {
                    processMessage(line);
                }
            }
        }

        System.out.println("Trainer source disconnected");
    }

    private void processMessage(String json) {
        final TrainerData data;

        try {
            data = gson.fromJson(json, TrainerData.class);
        } catch (JsonSyntaxException e) {
            System.err.println("Invalid trainer JSON: " + json);
            return;
        }

        if (data == null || data.isEmpty()) {
            System.err.println("Trainer message contains no recognised data: "
                    + json);
            return;
        }

        if (!isStarted) {
            return;
        }

        /*
         * Speed remains special. The external source provides raw rotations
         * and elapsed time; WattzAp should continue to apply its own wheel
         * circumference / trainer calculations.
         */
        if (data.hasSpeedData()) {
            if (data.getRotations() >= 0
                    && data.getElapsedMs() > 0) {

                Telemetry telemetry = calculateSpeed(
                        data.getRotations(),
                        data.getElapsedMs());

                MessageBus.INSTANCE.send(Messages.TELEMETRY, telemetry);
            } else {
                logger.error("Invalid trainer speed data: " + data);
            }
        } else if (data.hasPower()) {
            int power = data.getPower();

            if (power >= 0) {
                Telemetry t = calculateSpeed(power);
                MessageBus.INSTANCE.send(Messages.TELEMETRY, t);
            } else {
                System.err.println("Invalid power value: " + power);
            }
        } else if (data.getRotations() != null || data.getElapsedMs() != null) {
            logger.error(
                    "Incomplete trainer speed data (need rotations and elapsedMs): "
                            + data);
        }

        if (data.hasCadence()) {
            int cadence = data.getCadence();

            if (cadence >= 0 && cadence <= 250) {
                MessageBus.INSTANCE.send(Messages.CADENCE, cadence);
            } else {
                logger.error("Invalid cadence value: " + cadence);
            }
        }

        if (data.hasHeartRate()) {
            int heartRate = data.getHeartRate();

            if (heartRate >= 0 && heartRate <= 255) {
                MessageBus.INSTANCE.send(Messages.HEARTRATE, heartRate);
            } else {
                logger.error("Invalid heart-rate value: " + heartRate);
            }
        }

    }

    protected Telemetry calculateSpeed(int power) {
        long currentTime = System.currentTimeMillis();
        long tDiff = currentTime - lastTime;
        System.out.println("current " + currentTime + " lastTime " + lastTime + " tdiff " + tDiff);
        lastTime = currentTime;
        double distanceKM = 0.0;

        Telemetry t = new Telemetry();
        t.setPower(power);
        if (routeData != null) {
            Point p = routeData.getPoint(distance);
            if (p == null) {
                // end of the road
                distance = 0.0;
                return null;
            }
            double speed = (Power.getRealSpeed(mass,
                    p.getGradient() / 100, power)) * 3.6;
            distanceKM = (speed * tDiff) / 3600000;
            t.setElevation(p.getElevation());
            t.setGradient(p.getGradient());
            t.setLatitude(p.getLatitude());
            t.setLongitude(p.getLongitude());
            t.setSpeed(speed);
        }

        t.setTime(currentTime);
        distance += distanceKM;
        t.setDistanceMeters(distance * 1000);


        return t;
    }

    /**
     * Hook your extracted WattzAp speed calculation in here.
     * Return null if no speed value should be published yet.
     */
    protected Telemetry calculateSpeed(int rotations, long elapsedMs) {
        System.out.println("Trainer speed sample: rotations=" + rotations
                + ", elapsedMs=" + elapsedMs);

        Telemetry t = new Telemetry();

        double distanceKM = (rotations * wheelSize) / 100000.0;
        double speed = distanceKM * 3_600_000.0 / elapsedMs;
        System.out.println("speed " + speed);
        int powerWatts = power.getPower(speed, resistance);
        t.setPower(powerWatts);

        t.setDistanceMeters(distance * 1000);
        if (routeData != null) {
            Point p = routeData.getPoint(distance);
            if (p == null) {
                // end of the road
                distance = 0.0;
                return null;
            }
            if (powerWatts > 0) {
                // only works when power is positive, this is most of
                // the time on a turbo
                double realSpeed = (power.getRealSpeed(mass,
                        p.getGradient() / 100, powerWatts)) * 3.6;

                if (distanceKM > 0) {
                    distanceKM = (realSpeed / speed) * distanceKM;
                } else {
                    distanceKM = (realSpeed / 3600) * rotations;
                }
                speed = realSpeed;
            } else {
                /*
                 * Power Profile: speed is the ratio of our trainer power to
                 * the expected power, we also apply a bit of smoothing
                 */
                double ratio = powerWatts / (double) p.getPower();


            }


            t.setElevation(p.getElevation());
            t.setGradient(p.getGradient());
            t.setLatitude(p.getLatitude());
            t.setLongitude(p.getLongitude());
        }
        t.setSpeed(speed);

        t.setTime(System.currentTimeMillis()); // use realtime

        distance += distanceKM;

        logger.debug("sending " + t);

        return t;
    }

    public void callback(Messages message, Object o) {
        switch (message) {
            case START:
                // get up to date values
                mass = userPrefs.getTotalWeight();
                wheelSize = userPrefs.getWheelSizeCM();
                resistance = userPrefs.getResistance();
                power = userPrefs.getPowerProfile();
                simulSpeed = userPrefs.isVirtualPower();
                lastTime = System.currentTimeMillis();
                isStarted = true;

                break;
            case STOP:
                isStarted = false;
                break;
            case STARTPOS:
                distance = (Double) o;
                break;
            case GPXLOAD:
                this.routeData = (RouteReader) o;
                distance = 0.0;
                break;
        }
    }
}