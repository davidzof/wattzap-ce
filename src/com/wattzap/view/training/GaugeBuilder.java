package com.wattzap.view.training;

import eu.hansolo.steelseries.gauges.Radial;
import eu.hansolo.steelseries.tools.*;

import java.awt.*;

/**
 * Created by nicolas on 29/05/2015.
 */
public abstract class GaugeBuilder {

    private static final int GAUGE_MINIMUM_SIZE = 250;
    protected Radial radial;

    public Radial build() {
        radial = new Radial();
        buildGeneric();
        buildSpecific();
        return radial;
    }

    abstract void buildSpecific();

    public void buildGeneric() {
        radial = new Radial();
        radial.setMinimumSize(new Dimension(GAUGE_MINIMUM_SIZE, GAUGE_MINIMUM_SIZE));
        radial.setBackgroundColor(BackgroundColor.CARBON);
        radial.setForegroundType(ForegroundType.FG_TYPE4);
        radial.setFrameDesign(FrameDesign.BLACK_METAL);
        radial.setLedVisible(false);
        radial.setLcdColor(LcdColor.STANDARD_LCD);
        radial.setTicklabelOrientation(TicklabelOrientation.HORIZONTAL);
        radial.setSection3DEffectVisible(true);
        radial.setSectionsVisible(true);
        radial.setGlowColor(Color.cyan);
    }
}
