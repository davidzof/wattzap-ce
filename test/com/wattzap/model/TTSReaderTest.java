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
package com.wattzap.model;

import java.io.File;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.gpxcreator.gpxpanel.GPXFile;
import com.wattzap.model.dto.Point;

public class TTSReaderTest {
	TTSReader ttsReader;
	private static String currentFile;
	private static PrintStream out = System.out;
	private static int imageId;

	@Before
	public void setup() {
		ttsReader = new TTSReader();
	}

	@After
	public void tearDown() {

	}

	@Test
	public void getExtension() {
		Assert.assertEquals("tts", ttsReader.getExtension());
	}

	@Test
	public void routeType() {
		Assert.assertEquals(TTSReader.SLOPE, ttsReader.routeType());
	}

	// @Test
	public void loadMortiroloRoute() {

		// String[] files = new String[] { "./resources/test/tts/IT_Eroica.tts",
		// "./resources/test/tts/F_GrandAlps2011-Demo.tts",
		// "./resources/test/tts/FR_Monieux.tts",
		// "./resources/test/tts/dk_taulov_1.tts",
		// "./resources/test/tts/Malaucene-Ventoux.tts",
		// "./resources/test/tts/B_Flanders2007.tts",
		// "./resources/test/tts/FR_AlpineClassic.tts" };
		String[] files = new String[] { "./resources/test/tts/IT_Mortirolo08.tts" };
		for (String file : files) {
			currentFile = file;
			imageId = 0;

			if (out != System.out) {
				System.err.println("File " + file);
			}

			out.println("File " + file);
			ttsReader.load(file);

			out.println("");
		}
	}

	// @Test
	public void loadEroicaRoute() {

		String file = "./resources/test/tts/IT_Eroica.tts";
		out.println("File " + file);
		ttsReader.load(file);

		out.println("");
	}

	@Test
	public void loadSanSebRoute() {

		String file = "./resources/test/tts/E_SanSebastian2011-Demo.tts";
		out.println("File " + file);
		ttsReader.load(file);

		Point[] points = ttsReader.getPoints();
		for (Point p : points) {
			System.out.println(p);
		}

		System.out.println("max slope " + ttsReader.getMaxSlope());
		System.out.println("min slope " + ttsReader.getMinSlope());

		GPXFile gpx = ttsReader.getGpxFile();
		System.out.println("is gpx file " + gpx.isGPXFile() + " max meters "
				+ gpx.getEleMaxMeters());
		File f = new File("/home/david/tmp.gpx");
		gpx.saveToGPXFile(f);
	}

	// @Test
	public void loadMalaucene() {

		String file = "./resources/test/tts/Malaucene-Ventoux.tts";
		out.println("File " + file);
		ttsReader.load(file);

		out.println("");
		Point[] points = ttsReader.getPoints();
		for (Point p : points) {
			System.out.println(p);
		}
	}

}
