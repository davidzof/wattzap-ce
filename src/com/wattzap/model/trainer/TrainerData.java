/*
 * WattzAp external trainer data.
 *
 * All fields are boxed deliberately: a null value means that the field was
 * not supplied in this JSON message.
 */
package com.wattzap.model.trainer;

/**
 * @author David George
 * Copyright (c) 2026
 */
public class TrainerData {
    private Integer rotations;
    private Long elapsedMs;
    private Integer cadence;
    private Integer heartRate;
    private Integer power;
    private Integer gear;
    private Integer gearCount;

    public Integer getGear() {
        return gear;
    }

    public Integer getGearCount() {
        return gearCount;
    }

    public Integer getRotations() {
        return rotations;
    }

    public Long getElapsedMs() {
        return elapsedMs;
    }

    public Integer getCadence() {
        return cadence;
    }

    public Integer getHeartRate() {
        return heartRate;
    }

    public Integer getPower() {
        return power;
    }

    public boolean hasSpeedData() {
        return rotations != null && elapsedMs != null;
    }

    public boolean hasCadence() {
        return cadence != null;
    }

    public boolean hasHeartRate() {
        return heartRate != null;
    }

    public boolean hasPower() {
        return power != null;
    }

    public boolean hasGear() {
        return gear != null;
    }

    public boolean hasGearCount() {
        return gearCount != null;
    }

    public boolean isEmpty() {
        return rotations == null
                && elapsedMs == null
                && cadence == null
                && heartRate == null
                && power == null
                && gear == null
                && gearCount == null;
    }

    @Override
    public String toString() {
        return "TrainerData{" +
                "rotations=" + rotations +
                ", elapsedMs=" + elapsedMs +
                ", cadence=" + cadence +
                ", heartRate=" + heartRate +
                ", power=" + power +
                ", gear=" + gear +
                ", gearCount=" + gearCount +
                '}';
    }

}
