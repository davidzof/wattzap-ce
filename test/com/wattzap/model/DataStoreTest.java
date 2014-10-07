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

import java.util.Date;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.wattzap.model.dto.WorkoutData;

public class DataStoreTest {
	DataStore ds;

	@Before
	public void setup() {
		ds = new DataStore(".", "afghanistanbananastan");
	}

	@After
	public void tearDown() {
		ds.close();

	}

	@Test
	public void userProperty() {
		String user = System.getProperty("user.name");
		ds.insertProp(user, "weight", "69.0");
		String p = ds.getProp(user, "weight");
		Assert.assertEquals(p, "69.0");
	}

	@Test
	public void cryptProperty() {
		String user = System.getProperty("user.name");

		ds.insertPropCrypt(user, "length", "1001");
		String p = ds.getPropCrypt(user, "length");

		Assert.assertEquals(p, "1000");
	}

	@Test
	public void cryptPropertyFail() {
		String user = System.getProperty("user.name");

		ds.insertPropCrypt(user, "length", "999");
		String p = ds.getProp(user, "length");
		Assert.assertNotEquals(p, "999");
	}

	@Test
	public void saveWorkout() {
		String user = System.getProperty("user.name");
		WorkoutData data = new WorkoutData();
		data.setTcxFile("test.tcx");
		data.setDate((new Date()).getTime());

		ds.saveWorkOut(user, data);

	}

	@Test
	public void getWorkout() {
		String user = System.getProperty("user.name");
		WorkoutData data = ds.getWorkout(user, "test.tcx");
		Assert.assertEquals("test.tcx", data.getTcxFile());
	}
}
