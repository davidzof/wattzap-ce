package com.wattzap.model.dto;

/**
 * Created by nicolas on 26/05/2015.
 */
public class TrainingRangeView {

    private long low;
    private long high;

    public TrainingRangeView(long low, long high, long minLow, long maxHigh) {
//        if (low == 0 && high == 0) {
//            this.low = minLow;
//            this.high = maxHigh;
//        } else {
            this.low = low;
            this.high = high;
//        }
    }

    public boolean hasZone() {
        return this.low != 0 || this.high != 0;
    }

    public long getLow() {
        return low;
    }

    public void setLow(long low) {
        this.low = low;
    }

    public long getHigh() {

        return high;
    }

    public void setHigh(long high) {
        this.high = high;
    }
}
