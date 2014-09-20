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

/**
 * Using JCE (Java Cryptography Entension) which is integerated
 * with the SDK since (J2SE) version 1.4.0.
 *
 * This is an example of encryption and decryption of a string
 * using symmetric keys. In this case, the same key is used
 * for both encryption and decryption.
 *
 * The algorithm used to generate the key is a variant of DES
 * (The Data Encryption Standard). DES is a popular symmetric
 * block algorithm for data encryption, but because the the key
 * space is relatively small, brute force attacks are possible.
 *
 * Since the DES algorithm is out of date, DESede, a triple DES
 * variant, can be used which increases the key space and helps
 * prevent brute force attacks.
 *
 * Note: AES (The Advanced Encryption Standard) has now replaced
 * DES.   The National Institute of Standards and Technology (NIST)
 * chose "Rijndael" as AES's implementing algorithm. AES well be
 * seen later when examining another "provider".
 */

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.xml.bind.DatatypeConverter;

/**
 * 
 * @author David George
 * @date 22nd September 2013
 */
public class IncryptDecrypt {
	// private static SecretKey key = null; // in java.security
	private static Cipher cipher = null; // in javax.crypto

	/*
	 * Lots of exceptions can be thrown here. I've chosen not to deal with them
	 * and just let the application fail. These exceptions are:
	 * javax.crypto.IllegalBlockException javax.crypto.BadPaddingException
	 * java.securty.InvalidKeyException
	 */

	public static void main(String[] args) throws Exception {
		// This statement is not needed since the SunJCE is
		// (statically) installed as of SDK 1.4. Do this to
		// dynamically install another provider.

		// Security.addProvider(new com.sun.crypto.provider.SunJCE());

		// Generate a secret key for a symmetric algorithm and
		// create a Cipher instance. DESede key size is always
		// 168 bits. Other algorithms, like "blowfish", allow
		// for variable lenght keys.

		// KeyGenerator keyGenerator =
		// KeyGenerator.getInstance("DESede");

		SecretKeyFactory keyGenerator = SecretKeyFactory.getInstance("DES");
		// keyGenerator.init(168);
		DESKeySpec keySpec = new DESKeySpec(
				"afghanistanbananstan".getBytes("UTF8"));
		SecretKey secretKey = keyGenerator.generateSecret(keySpec);

		cipher = Cipher.getInstance("DES");

		// Store the string as an array of bytes. You should
		// specify the encoding method for consistent encoding
		// and decoding across different platforms.

		String clearText = "480";
		byte[] clearTextBytes = clearText.getBytes("UTF8");

		// Initialize the cipher and encrypt this byte array

		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		byte[] cipherBytes = cipher.doFinal(clearTextBytes);

		String cipherText = toHexString(cipherBytes);
		
		cipherBytes = toByteArray(cipherText);
		// Reinitialize the cipher an decrypt the byte array

		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		byte[] decryptedBytes = cipher.doFinal(cipherBytes);
		String decryptedText = new String(decryptedBytes, "UTF8");

		System.out.println("Before encryption: " + clearText);
		System.out.println("After encryption: " + cipherText.toString());
		System.out.println("After decryption: " + decryptedText);
	}

	public static String toHexString(byte[] array) {
	    return DatatypeConverter.printHexBinary(array);
	}

	public static byte[] toByteArray(String s) {
	    return DatatypeConverter.parseHexBinary(s);
	}
}