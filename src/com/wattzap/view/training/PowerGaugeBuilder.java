package com.wattzap.view.training;

import com.wattzap.model.UserPreferences;
import eu.hansolo.steelseries.tools.Section;

import java.awt.*;

public class PowerGaugeBuilder extends GaugeBuilder {

    public static final int MIN_POWER = 25;
    private static double MAX_POWER; // ftp * 2

    // color gradient thanks to : http://www.perbang.dk/rgbgradient/
    // setup : HSV Gradient (Inverse), 7 steps from 72BE23 to B9222A
    public static final Color COLOR_POWER_ZONE_1 = new Color(113, 190, 34);
    public static final Color COLOR_POWER_ZONE_2 = new Color(153, 189, 34);
    public static final Color COLOR_POWER_ZONE_3 = new Color(188, 184, 34);
    public static final Color COLOR_POWER_ZONE_4 = new Color(187, 144, 34);
    public static final Color COLOR_POWER_ZONE_5 = new Color(186, 104, 34);
    public static final Color COLOR_POWER_ZONE_6 = new Color(185, 65, 34);
    public static final Color COLOR_POWER_ZONE_7 = new Color(185, 34, 42);

    @Override
    void buildSpecific() {
        double ftp = UserPreferences.INSTANCE.getMaxPower();
        MAX_POWER = ftp * 2;

        radial.setTitle("Power");
        radial.setUnitString("Watts");
        radial.setMinValue(0);
        radial.setMaxValue(MAX_POWER);

        // sections
        radial.addSection(new Section(MIN_POWER, ftp * 0.55, COLOR_POWER_ZONE_1));
        radial.addSection(new Section(ftp * 0.55, ftp * 0.75, COLOR_POWER_ZONE_2));
        radial.addSection(new Section(ftp * 0.75, ftp * 0.90, COLOR_POWER_ZONE_3));
        radial.addSection(new Section(ftp * 0.90, ftp * 1.05, COLOR_POWER_ZONE_4));
        radial.addSection(new Section(ftp * 1.05, ftp * 1.2, COLOR_POWER_ZONE_5));
        radial.addSection(new Section(ftp * 1.2, ftp * 1.5, COLOR_POWER_ZONE_6));
        radial.addSection(new Section(ftp * 1.5, MAX_POWER, COLOR_POWER_ZONE_7));

        // ticks
        radial.setMajorTickSpacing(50);
        radial.setMinorTickSpacing(10);
    }

}