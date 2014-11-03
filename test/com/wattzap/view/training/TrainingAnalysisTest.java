/* This file is part of Wattzap Community Edition.
 *
 * Wattzap Community Edtion is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Wattzap Community Edition is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Wattzap.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wattzap.view.training;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import com.wattzap.model.dto.Telemetry;
import com.wattzap.model.dto.WorkoutData;

public class TrainingAnalysisTest {
	@Test
	public void ImportGPX() {
		try {
			ArrayList<Telemetry> gpxData = new ArrayList<Telemetry>();
			for (int i = 100; i < 200; i++) {
				Telemetry point = new Telemetry();
				point.setDistanceMeters(i - 100);
				point.setPower(i);
				point.setCadence(i);
				point.setHeartRate(i);
				point.setTime(i * 20000);
				gpxData.add(point);
			}

			WorkoutData d = TrainingAnalysis.analyze(gpxData);
			d.setFtp(250);
			Assert.assertEquals(d.getMaxPower(), 199);
			Assert.assertEquals(d.getFiveSecondPwr(), 199);
			Assert.assertEquals(d.getAvePower(), 150);
			Assert.assertEquals(d.getDistanceMeters(), 99, 1.0);
			System.out.println(d);

		} catch (Exception e) {
			e.printStackTrace();

		}
	}
}
