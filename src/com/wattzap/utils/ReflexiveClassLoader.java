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
package com.wattzap.utils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Loads classes from package with specified annotation class.
 * 
 * @author David George
 * 
 *         (c) 19th September, 2014; David George / Wattzap.com
 * 
 */
public class ReflexiveClassLoader {
	private static Logger logger = LogManager.getLogger("ReflexiveClassLoader");

	public static List<Class> getClassNamesFromPackage(String packageName,
			Class annotationType) throws IOException, URISyntaxException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException {

		List<Class> classes = new ArrayList<Class>();

		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		URL packageURL;

		packageName = packageName.replace(".", "/");
		packageURL = classLoader.getResource(packageName);

		if (packageURL.getProtocol().equals("jar")) {
			logger.debug("Scanning Jar");
			String jarFileName;
			JarFile jf;
			Enumeration<JarEntry> jarEntries;
			String entryName;

			// build jar file name, then loop through zipped entries
			jarFileName = URLDecoder.decode(packageURL.getFile(), "UTF-8");
			jarFileName = jarFileName.substring(5, jarFileName.indexOf("!"));
			jf = new JarFile(jarFileName);
			jarEntries = jf.entries();
			while (jarEntries.hasMoreElements()) {
				entryName = jarEntries.nextElement().getName();

				// only check class names in current package
				if (entryName.startsWith(packageName)
						&& entryName.indexOf('/', packageName.length() + 1) == -1
						&& entryName.lastIndexOf('.') != -1) {
					entryName = entryName.substring(0,
							entryName.lastIndexOf('.'));

					entryName = entryName.replace("/", ".");
					Class c = Class.forName(entryName);

					Annotation[] annotations = c.getAnnotations();
					for (Annotation a : annotations) {
						if (a.annotationType() == annotationType) {
							classes.add(c);
						}
					}// for
				}
			}// while
			jf.close();

			// loop through files in classpath
		} else {
			logger.debug("Scanning classpath");
			URI uri = new URI(packageURL.toString());
			File folder = new File(uri.getPath());
			// won't work with path which contains blank (%20)
			// File folder = new File(packageURL.getFile());
			File[] contenuti = folder.listFiles();
			String entryName;
			for (File actual : contenuti) {
				entryName = actual.getName();
				if (entryName.endsWith(".class")) {
					entryName = packageName
							+ "."
							+ entryName
									.substring(0, entryName.lastIndexOf('.'));
					entryName = entryName.replace("/", ".");

					Class c = Class.forName(entryName);
					Annotation[] annotations = c.getAnnotations();
					for (Annotation a : annotations) {
						if (a.annotationType() == annotationType) {
							classes.add(c);
						}
					}// for

				}
			}
		}
		return classes;
	}
}
