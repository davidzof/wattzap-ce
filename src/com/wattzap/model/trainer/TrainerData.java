/*
 * WattzAp external trainer data.
 *
 * All fields are boxed deliberately: a null value means that the field was
 * not supplied in this JSON message.
 */
package com.wattzap.model.trainer;

public class TrainerData {

    private Integer rotations;
    private Long elapsedMs;
    private Integer cadence;
    private Integer heartRate;
    private Integer power;

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

    public boolean isEmpty() {
        return rotations == null
                && elapsedMs == null
                && cadence == null
                && heartRate == null
                && power == null;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("TrainerData{");
        boolean first = true;

        if (rotations != null) {
            b.append("rotations=").append(rotations);
            first = false;
        }

        if (elapsedMs != null) {
            if (!first) b.append(", ");
            b.append("elapsedMs=").append(elapsedMs);
            first = false;
        }

        if (cadence != null) {
            if (!first) b.append(", ");
            b.append("cadence=").append(cadence);
            first = false;
        }

        if (heartRate != null) {
            if (!first) b.append(", ");
            b.append("heartRate=").append(heartRate);
            first = false;
        }

        if (power != null) {
            if (!first) b.append(", ");
            b.append("power=").append(power);
        }

        b.append('}');
        return b.toString();
    }
}
