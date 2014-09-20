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
package com.wattzap.model.power;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.wattzap.utils.ReflexiveClassLoader;

/**
 * Load all available power profiles
 * 
 * @author David George
 * 
 *         (c) 2013 David George / Wattzap.com
 * 
 */
public enum PowerProfiles {
	INSTANCE;
	List<Power> profiles;
	private Logger logger = LogManager.getLogger("PowerProfiles");

	PowerProfiles() {
		String packageName = this.getClass().getPackage().getName();

		profiles = new ArrayList<Power>();
		try {
			getClassNamesFromPackage(packageName);

		} catch (Exception e1) {
			logger.error(e1.getLocalizedMessage());
		}
	}

	public List<Power> getProfiles() {
		return profiles;
	}

	public Power getProfile(String description) {
		for (Power p : profiles) {
			if (description.equals(p.description())) {

				return p;
			}
		}
		return null;
	}

	public void getClassNamesFromPackage(String packageName)
			throws IOException, URISyntaxException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {
try {
		List<Class> classes = ReflexiveClassLoader.getClassNamesFromPackage(
				packageName, PowerAnnotation.class);

		for (Class c : classes) {
			Power p = (Power) c.newInstance();
			logger.info("adding power " + p.description());
			profiles.add(p);
		}
} catch (Exception e) {
	e.printStackTrace();
}
	}
}
