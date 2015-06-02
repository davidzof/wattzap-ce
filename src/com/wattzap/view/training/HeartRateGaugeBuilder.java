package com.wattzap.view.training;

import com.wattzap.model.UserPreferences;
import eu.hansolo.steelseries.gauges.Radial;
import eu.hansolo.steelseries.tools.Section;

import java.awt.*;

public class HeartRateGaugeBuilder extends GaugeBuilder {

    public static final int MAX_HR = 220;
    public static final int MIN_HR = 30;
    
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
        radial.addSection(new Section(MIN_HR, fhr * 0.68, Color.blue.brighter().brighter()));  // active recovery < 68%
        radial.addSection(new Section(fhr * 0.68, fhr * 0.83, Color.green)); // Endurance 69 - 83%
        radial.addSection(new Section(fhr * 0.83, fhr * 0.94, Color.yellow.brighter().brighter())); // Tempo 84 - 94%
        radial.addSection(new Section(fhr * 0.94, fhr * 1.05, Color.orange.darker())); // Lactate Threshold 95-105%
        radial.addSection(new Section(fhr * 1.05, fhr * 1.15, Color.red.darker())); // VO2Max
    }

}