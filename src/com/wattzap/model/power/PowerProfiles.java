package com.wattzap.model.power;

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

import com.wattzap.model.DataStore;

/**
 * Load all available power profiles
 * 
 * @author David George
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
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		URL packageURL;

		packageName = packageName.replace(".", "/");
		logger.info("Package Name " + packageName);

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
				// only check class names
				if (entryName.startsWith(packageName)
						&& entryName.length() > (packageName.length() + 5)) {

					entryName = entryName.substring(0,
							entryName.lastIndexOf('.'));
					entryName = entryName.replace("/", ".");

					Class c = Class.forName(entryName);
					
					Annotation[] annotations = c.getAnnotations();
					for (Annotation a : annotations) {
						if (a instanceof PowerAnnotation) {
							Power p = (Power) c.newInstance();
							logger.info("adding power " + p.description());
							profiles.add(p);
						}
					}
				}
			}

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
				// if (entryName.endsWith("Profile.class")) {
				entryName = packageName + "."
						+ entryName.substring(0, entryName.lastIndexOf('.'));
				entryName = entryName.replace("/", ".");

				Class c = Class.forName(entryName);
				Annotation[] annotations = c.getAnnotations();
				for (Annotation a : annotations) {
					if (a instanceof PowerAnnotation) {
						Power p = (Power) c.newInstance();
						profiles.add(p);

					}
				}

				// }
			}
		}

	}
}
