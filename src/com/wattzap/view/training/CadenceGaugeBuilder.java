package com.wattzap.view.training;

import eu.hansolo.steelseries.gauges.Radial;
import eu.hansolo.steelseries.tools.*;

import java.awt.*;

public class CadenceGaugeBuilder extends GaugeBuilder {

    public static final double MIN_RPM = 0;
    public static final int MAX_RPM = 180;

    @Override
    void buildSpecific() {
        radial.setTitle("Cadence");
        radial.setUnitString("RPM");
        radial.setMajorTickSpacing(10);
        radial.setMinorTickSpacing(5);
        radial.setMinValue(MIN_RPM);
        radial.setMaxValue(MAX_RPM);

        radial.addSection(new Section(50, 80, Color.blue.darker()));
        radial.addSection(new Section(80, 100, Color.green.darker()));
        radial.addSection(new Section(100, 130, Color.orange.darker()));
    }
}