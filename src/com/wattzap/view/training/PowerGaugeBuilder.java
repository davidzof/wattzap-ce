package com.wattzap.view.training;

import com.wattzap.model.UserPreferences;
import eu.hansolo.steelseries.tools.Section;

import java.awt.*;

public class PowerGaugeBuilder extends GaugeBuilder {

    public static final int MIN_POWER = 0;
    private static double MAX_POWER; // ftp * 2
    
    @Override
    void buildSpecific() {
        double ftp = UserPreferences.INSTANCE.getMaxPower();
        MAX_POWER = ftp * 2;

        radial.setTitle("Power");
        radial.setUnitString("Watts");
        radial.setMinValue(MIN_POWER);
        radial.setMaxValue(MAX_POWER);

        // sections
        radial.addSection(new Section(MIN_POWER, ftp * 0.55, Color.blue.brighter()));
        radial.addSection(new Section(ftp * 0.55, ftp * 0.75, Color.green));
        radial.addSection(new Section(ftp * 0.75, ftp * 0.90, Color.yellow));
        radial.addSection(new Section(ftp * 0.90, ftp * 1.05, Color.orange));
        radial.addSection(new Section(ftp * 1.05, ftp * 1.2, Color.red));
        radial.addSection(new Section(ftp * 1.2, ftp * 1.5, Color.magenta));
        radial.addSection(new Section(ftp * 1.5, MAX_POWER, Color.darkGray));

        // ticks
        radial.setMajorTickSpacing(50);
        radial.setMinorTickSpacing(10);
    }

}