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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.wattzap.utils.ReflexiveClassLoader;

/**
 * Load all available route readers
 * 
 * @author David George
 * 
 *         (c) 19 September 2014, David George / Wattzap.com
 * 
 */
public enum Readers {
	INSTANCE;
	List<RouteReader> readers;
	private Logger logger = LogManager.getLogger("Readers");

	Readers() {
		String packageName = this.getClass().getPackage().getName();

		readers = new ArrayList<RouteReader>();
		try {
			getClassNamesFromPackage(packageName);

		} catch (Exception e1) {
			logger.error(e1.getLocalizedMessage());
		}
	}

	public List<RouteReader> getReaders() {
		return readers;
	}

	public RouteReader getReader(String ext) {
		for (RouteReader r : readers) {
			if (ext.equals(r.getExtension())) {

				return r;
			}
		}
		return null;
	}

	public void getClassNamesFromPackage(String packageName)
			throws IOException, URISyntaxException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {

		List<Class> classes = ReflexiveClassLoader.getClassNamesFromPackage(
				packageName, RouteAnnotation.class);
		for (Class c : classes) {
			RouteReader p = (RouteReader) c.newInstance();
			readers.add(p);
		}

	}
}