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
package com.wattzap.model.ant;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wattzap.controller.Messages;
import com.wattzap.model.RouteReader;
import com.wattzap.model.TTSReader;
import com.wattzap.model.dto.Telemetry;

public class SpeedListenerTest {
	SpeedListener listener;
	RouteReader ttsReader;

	@Before
	public void setup() {
		listener = new SpeedListener();
		ttsReader = new TTSReader();
		String file = "./resources/test/tts/E_SanSebastian2011-Demo.tts";
		System.out.println("File " + file);
		ttsReader.load(file);

		// load gps here
		listener.callback(Messages.GPXLOAD, ttsReader);
		listener.callback(Messages.START, null);
	}

	@After
	public void tearDown() {

	}

	@Test
	public void testBackwardsCount() {
		Telemetry t;

		t = listener.getTelemetry(0, 0);
		System.out.println(t);
		t = listener.getTelemetry(1000, 1);
		System.out.println(t);
		t = listener.getTelemetry(2000, 2);
		System.out.println(t);
		t = listener.getTelemetry(3000, 3);
		System.out.println(t);
		t = listener.getTelemetry(4000, 3);
		System.out.println(t);
		t = listener.getTelemetry(1000, 4);
		System.out.println(t);
		t = listener.getTelemetry(2000, 5);
		System.out.println(t);
		t = listener.getTelemetry(4000, 3);
		System.out.println(t);
	}

	@Test
	public void testZeroSpeed() {
		Telemetry t;

		t = listener.getTelemetry(0, 0);
		System.out.println(t);
		t = listener.getTelemetry(1000, 1);
		System.out.println(t);
		t = listener.getTelemetry(2000, 2);
		System.out.println(t);
		t = listener.getTelemetry(3000, 3);
		System.out.println(t);
		t = listener.getTelemetry(4000, 4);
		System.out.println(t);
		t = listener.getTelemetry(5000, 5);
		System.out.println(t);
		t = listener.getTelemetry(6000, 6);
		System.out.println(t);
		t = listener.getTelemetry(7000, 6);
		System.out.println(t);
		t = listener.getTelemetry(8000, 6);
		System.out.println(t);
		t = listener.getTelemetry(9000, 6); // 3
		System.out.println(t);
		t = listener.getTelemetry(9001, 6);
		System.out.println(t);
		t = listener.getTelemetry(9002, 6);
		System.out.println(t);
		t = listener.getTelemetry(9003, 6);
		System.out.println(t);
		t = listener.getTelemetry(9004, 6);
		System.out.println(t);
		t = listener.getTelemetry(14000, 6);
		System.out.println(t);
		t = listener.getTelemetry(15000, 6);
		System.out.println(t);
		t = listener.getTelemetry(16000, 6);
		System.out.println(t);
		t = listener.getTelemetry(17000, 6);
		System.out.println(t);
		t = listener.getTelemetry(18000, 6);
		System.out.println(t);
		t = listener.getTelemetry(19000, 6);
		System.out.println(t);
		t = listener.getTelemetry(20000, 6);
		
		System.out.println(t);
		t = listener.getTelemetry(21000, 6);
		
		System.out.println(t);
		t = listener.getTelemetry(22000, 6);
		
		System.out.println(t);
		t = listener.getTelemetry(23000, 6);
		
		System.out.println(t);
		t = listener.getTelemetry(7000, 7);
		System.out.println(t);

	}

}
