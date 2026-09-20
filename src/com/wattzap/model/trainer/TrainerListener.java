/*
 * WattzAp external trainer input.
 *
 * Receives one JSON object per line over a localhost TCP connection,
 * converts trainer data into WattzAp events and sends trainer-control
 * messages back over the same connection.
 */
package com.wattzap.model.trainer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.wattzap.controller.MessageBus;
import com.wattzap.controller.MessageCallback;
import com.wattzap.controller.Messages;
import com.wattzap.model.RouteReader;
import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.Point;
import com.wattzap.model.dto.Telemetry;
import com.wattzap.model.power.Power;
import com.wattzap.utils.Rolling;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Process messages from external sources and dispatches the onto the Event Bus
 */
public class TrainerListener implements MessageCallback {
    private static final Logger logger = LogManager.getLogger("TrainerListener");

    private static final double GRADIENT_SCALE = 0.5;

    private final UserPreferences userPrefs = UserPreferences.INSTANCE;
    private final int port;
    private final Gson gson = new Gson();

    private double distance = 0.0;
    private double mass;
    private double wheelSize = userPrefs.getWheelSizeCM();
    private int resistance = userPrefs.getResistance();
    private Power power = userPrefs.getPowerProfile();
    private RouteReader routeData;
    private Rolling powerRatio;
    private boolean isStarted;
    private long lastTime;

    private volatile boolean running;
    private ServerSocket serverSocket;

    public TrainerListener(int port) {
        this.port = port;

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

        Thread listenerThread =
                new Thread(this::listen, "WattzAp-TrainerListener");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public synchronized void stop() {
        running = false;

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

            logger.info("Trainer listener waiting on 127.0.0.1:" + port);

            while (running) {
                try (Socket socket = serverSocket.accept()) {
                    logger.info("Trainer source connected: "
                            + socket.getRemoteSocketAddress());

                    readMessages(socket);
                } catch (IOException e) {
                    if (running) {
                        logger.error("Trainer listener error", e);
                    }
                }
            }
        } catch (IOException e) {
            if (running) {
                logger.error("Unable to start trainer listener", e);
            }
        } finally {
            running = false;
            closeServerSocket();
        }
    }

    private void closeServerSocket() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void readMessages(Socket socket) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                     new InputStreamReader(
                             socket.getInputStream(),
                             StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(
                     new OutputStreamWriter(
                             socket.getOutputStream(),
                             StandardCharsets.UTF_8))) {

            String line;

            while (running && (line = reader.readLine()) != null) {
                line = line.trim();

                if (!line.isEmpty()) {
                    processMessage(line, writer);
                }
            }
        }

        logger.info("Trainer source disconnected");
    }

    private void processMessage(String json, BufferedWriter writer) {
        final TrainerData data;

        try {
            data = gson.fromJson(json, TrainerData.class);
        } catch (JsonSyntaxException e) {
            logger.error("Invalid trainer JSON: " + json);
            return;
        }

        if (data == null || data.isEmpty()) {
            logger.error("Trainer message contains no recognised data: " + json);
            return;
        }

        if (!isStarted) {
            return;
        }

        /*
         * Measured power takes precedence over wheel speed. This is especially
         * useful for direct-drive smart trainers and power meters.
         */
        Telemetry telemetry = null;

        if (data.hasPower()) {
            int watts = data.getPower();

            if (watts >= 0) {
                telemetry = calculateFromPower(watts);
            } else {
                logger.error("Invalid power value: " + watts);
            }

        } else if (data.hasSpeedData()) {
            if (data.getRotations() >= 0 && data.getElapsedMs() > 0) {
                telemetry = calculateFromWheelSpeed(
                        data.getRotations(),
                        data.getElapsedMs());
            } else {
                logger.error("Invalid trainer speed data: " + data);
            }

        } else if (data.getRotations() != null
                || data.getElapsedMs() != null) {
            logger.error(
                    "Incomplete trainer speed data "
                    + "(need rotations and elapsedMs): " + data);
        }

        if (telemetry != null) {
            MessageBus.INSTANCE.send(Messages.TELEMETRY, telemetry);
            sendToBridge(writer, telemetry);
        }

        if (data.hasCadence()) {
            int cadence = data.getCadence();

            if (cadence >= 0 && cadence < 250) {
                MessageBus.INSTANCE.send(Messages.CADENCE, cadence);
            } else {
                logger.error("Invalid cadence value: " + cadence);
            }
        }

        if (data.hasHeartRate()) {
            int heartRate = data.getHeartRate();

            if (heartRate > 25 && heartRate < 255) {
                MessageBus.INSTANCE.send(Messages.HEARTRATE, heartRate);
            } else {
                logger.error("Invalid heart-rate value: " + heartRate);
            }
        }
    }

    /**
     * Send the effective trainer gradient back to the connected bridge.
     * The route/physics calculations continue to use the real GPX gradient.
     * Only the trainer-control value is scaled.
     */
    private void sendToBridge(BufferedWriter writer, Telemetry telemetry) {
        if (routeData == null) {
            return;
        }

        JsonObject control = new JsonObject();
        if (routeData.routeType() == RouteReader.SLOPE) {

            control.addProperty("type", "control");
            control.addProperty("gradient", telemetry.getGradient() * GRADIENT_SCALE);
        } else {
            control.addProperty("type", "control");
            control.addProperty("targetPower", telemetry.getTargetPower());

        }
        try {
            writer.write(gson.toJson(control));
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            logger.error("Unable to send trainer gradient", e);
        }
    }

    /*
     * Power sensor, for example a Smart Trainer
     */
    protected Telemetry calculateFromPower(int watts) {
        long currentTime = System.currentTimeMillis();
        long elapsedMs = currentTime - lastTime;
        lastTime = currentTime;
        Telemetry telemetry;

        if (elapsedMs <= 0) {
            return null;
        }

        double speed;
        double distanceKM;
        if (routeData != null) {
            System.out.println("distance " + distance);
            Point point = routeData.getPoint(distance);
            if (point == null) {
                // end of the road
                distance = 0.0;
                return null;
            }
            telemetry = createRouteTelemetry(point);

            if (routeData.routeType() == RouteReader.SLOPE) {
                speed = Power.getRealSpeed(
                        mass,
                        point.getGradient() / 100.0,
                        watts) * 3.6;
            } else {
                // power profile, speed is the ratio of our trainer power to
                // the expected power
                // TODO: in erg mode we need to send target power to bridge, what about training mode?
                // check this
                telemetry.setTargetPower(point.getPower());
                double ratio = ((double) watts / point.getPower());
                // speed is video speed * power ratio
                speed = point.getSpeed() * ratio;
            }
        } else {
            telemetry = new Telemetry();
            speed = Power.getRealSpeed(mass, 0, watts);
        }
        distanceKM = speed * elapsedMs / 3_600_000.0;
        distance += distanceKM;

        telemetry.setSpeed(speed);
        telemetry.setPower(watts);
        telemetry.setTime(currentTime);
        telemetry.setDistanceMeters(distance * 1000.0);

        logger.debug("sending " + telemetry);

        return telemetry;
    }

    /*
     * External speed or speed+cadence sensor
     */
    protected Telemetry calculateFromWheelSpeed(int rotations, long elapsedMs) {
        if (elapsedMs <= 0) {
            return null;
        }

        double distanceKM = rotations * wheelSize / 100000.0;
        double speed = distanceKM * 3_600_000.0 / elapsedMs;

        int powerWatts = power.getPower(speed, resistance);

        Telemetry telemetry = new Telemetry();
        telemetry.setPower(powerWatts);


        if (routeData != null) {
            // we are cycling a gps route, so calculate speed based on gradient
            Point point = routeData.getPoint(distance);

            if (routeData.routeType() == RouteReader.SLOPE) {
                if (point == null) {
                    distance = 0.0;
                    return null;
                }

                if (powerWatts > 0) {
                    double realSpeed = Power.getRealSpeed(
                            mass,
                            point.getGradient() / 100.0,
                            powerWatts) * 3.6;

                    if (distanceKM > 0) {
                        distanceKM = (realSpeed / speed) * distanceKM;
                    } else {
                        double timeSeconds = elapsedMs / 1000.0;
                        distanceKM = realSpeed * timeSeconds / 3600.0;
                    }

                    speed = realSpeed;
                }
            } else {
                /*
                 * Power Profile: speed is the ratio of our trainer power to
                 * the expected power, we also apply a bit of smoothing
                 */
                telemetry.setTargetPower(point.getPower());
                double ratio = powerRatio.add(powerWatts / (double)point.getPower());

                double timeSeconds = elapsedMs / 1000.0;
                // speed is video speed * power ratio
                speed = point.getSpeed() * ratio;
                distanceKM = (speed / 3600) * timeSeconds;
            }

            populateRouteTelemetry(telemetry, point);
        }

        distance += distanceKM;
        telemetry.setSpeed(speed);
        telemetry.setTime(System.currentTimeMillis());
        telemetry.setDistanceMeters(distance * 1000.0);

        logger.debug("sending " + telemetry);
        return telemetry;
    }

    private Telemetry createRouteTelemetry(Point point) {
        Telemetry telemetry = new Telemetry();
        populateRouteTelemetry(telemetry, point);
        return telemetry;
    }

    private void populateRouteTelemetry(Telemetry telemetry, Point point) {
        telemetry.setElevation(point.getElevation());
        telemetry.setGradient(point.getGradient());
        telemetry.setLatitude(point.getLatitude());
        telemetry.setLongitude(point.getLongitude());
    }

    @Override
    public void callback(Messages message, Object value) {
        switch (message) {
            case START:
                mass = userPrefs.getTotalWeight();
                wheelSize = userPrefs.getWheelSizeCM();
                resistance = userPrefs.getResistance();
                power = userPrefs.getPowerProfile();
                lastTime = System.currentTimeMillis();
                powerRatio = new Rolling(10);
                isStarted = true;
                break;

            case STOP:
                isStarted = false;
                break;

            case STARTPOS:
                distance = (Double) value;
                break;

            case GPXLOAD:
                routeData = (RouteReader) value;
                distance = 0.0;
                break;

            default:
                break;
        }
    }
}
