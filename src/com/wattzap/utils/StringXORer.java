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

import java.io.IOException;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


/**
 * @author David George
 * @date 2nd September 2013
 */
public class StringXORer {

	public static String encode(String s, String key) {
		return base64Encode(xorWithKey(s.getBytes(), key.getBytes()));
	}

	public static String decode(String s, String key) {
		return new String(xorWithKey(base64Decode(s), key.getBytes()));
	}

	private static byte[] xorWithKey(byte[] a, byte[] key) {
		byte[] out = new byte[a.length];
		for (int i = 0; i < a.length; i++) {
			out[i] = (byte) (a[i] ^ key[i % key.length]);
		}
		return out;
	}

	private static byte[] base64Decode(String s) {
		try {
			BASE64Decoder d = new BASE64Decoder();
			return d.decodeBuffer(s);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String base64Encode(byte[] bytes) {
		BASE64Encoder enc = new BASE64Encoder();
		return enc.encode(bytes).replaceAll("\\s", "");

	}
}