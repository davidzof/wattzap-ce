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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wattzap.model.dto.Point;

public class RLVReaderTest {
	RLVReader reader;

	@Before
	public void setup() {
		reader = new RLVReader();
	}

	@After
	public void tearDown() {

	}

	@Test
	public void loadRLVRoute() {

		String strFilePath = "FR_Aube-Demo.rlv";
		// String strFilePath = "/home/david/torrents/FR_AlpineClassic/FR_AlpineClassic.rlv";
		// String strFilePath = "/home/david/Dropbox/test.rlv";
		// String strFilePath =
		// "/home/david/torrents/ES_Morcuera/ES_Morcuera.rlv";
		// String strFilePath = "/home/david/torrents/ES_Monte_Perdido.rlv";
		reader.load(strFilePath);

		for (Point p : reader.getPoints()) {
			System.out.println(p);
		}
		/*
		 * Point p = reader.getPoint(5.2); System.out.println(p);
		 * 
		 * p = reader.getPoint(5.3); System.out.println(p);
		 */
	}

}
