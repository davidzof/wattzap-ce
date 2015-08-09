package com.wattzap.view.training;

import eu.hansolo.steelseries.gauges.Radial;
import eu.hansolo.steelseries.tools.*;

import java.awt.*;

public class CadenceGaugeBuilder extends GaugeBuilder {

    public static final double MIN_RPM = 0;
    public static final int MAX_RPM = 180;

    // color gradient thanks to : http://www.perbang.dk/rgbgradient/
    // setup : HSV Gradient (Inverse), 3 steps from 72BE23 to 9CA790
    public static final Color COLOR_CADENCE_IDEAL = new Color(113, 190, 34);
    public static final Color COLOR_CADENCE_TRANSITIONING = new Color(137, 178, 93);
    public static final Color COLOR_CADENCE_POOR_OR_DRILL = new Color(156, 167, 144);

    @Override
    void buildSpecific() {
        radial.setTitle("Cadence");
        radial.setUnitString("RPM");
        radial.setMajorTickSpacing(10);
        radial.setMinorTickSpacing(5);
        radial.setMinValue(MIN_RPM);
        radial.setMaxValue(MAX_RPM);

        radial.addSection(new Section(50, 65, COLOR_CADENCE_POOR_OR_DRILL));
        radial.addSection(new Section(65, 80, COLOR_CADENCE_TRANSITIONING));
        radial.addSection(new Section(80, 100, COLOR_CADENCE_IDEAL));
        radial.addSection(new Section(100, 115, COLOR_CADENCE_TRANSITIONING));
        radial.addSection(new Section(115, 130, COLOR_CADENCE_POOR_OR_DRILL));
    }
}