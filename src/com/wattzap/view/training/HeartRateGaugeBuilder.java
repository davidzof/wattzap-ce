package com.wattzap.view.training;

import com.wattzap.model.UserPreferences;
import eu.hansolo.steelseries.gauges.Radial;
import eu.hansolo.steelseries.tools.Section;

import java.awt.*;

public class HeartRateGaugeBuilder extends GaugeBuilder {

    public static final int MAX_HR = 220;
    public static final int MIN_HR = 30;

    // color gradient thanks to : http://www.perbang.dk/rgbgradient/
    // setup : HSV Gradient (Inverse), 5 steps from 72BE23 to B9222A
    public static final Color COLOR_POWER_HR_1 = new Color(113, 190, 34);
    public static final Color COLOR_POWER_HR_2 = new Color(172, 188, 34);
    public static final Color COLOR_POWER_HR_3 = new Color(187, 144, 34);
    public static final Color COLOR_POWER_HR_4 = new Color(186, 84, 34);
    public static final Color COLOR_POWER_HR_5 = new Color(185, 34, 42);
    
    @Override
    void buildSpecific() {
        radial.setMinValue(MIN_HR);
        radial.setMaxValue(MAX_HR);
        radial.setUnitString("BPM");
        radial.setTitle("Heart Rate");
        radial.setMajorTickSpacing(10);
        radial.setMinorTickSpacing(5);

        double fhr = UserPreferences.INSTANCE.getMaxHR();

        // TODO retrieve colors from userpreferences
        radial.addSection(new Section(MIN_HR, fhr * 0.68, COLOR_POWER_HR_1));  // active recovery < 68%
        radial.addSection(new Section(fhr * 0.68, fhr * 0.83, COLOR_POWER_HR_2)); // Endurance 68 - 84%
        radial.addSection(new Section(fhr * 0.83, fhr * 0.94, COLOR_POWER_HR_3)); // Tempo 84 - 94%
        radial.addSection(new Section(fhr * 0.94, fhr * 1.05, COLOR_POWER_HR_4)); // Lactate Threshold 95-105%
        radial.addSection(new Section(fhr * 1.05, fhr * 1.15, COLOR_POWER_HR_5)); // > 106%, VO2Max
    }

}