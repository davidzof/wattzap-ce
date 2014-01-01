package com.wattzap.utils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import ecc.CryptoSystem;
import ecc.Key;
import ecc.elliptic.ECCryptoSystem;
import ecc.elliptic.ECKey;
import ecc.elliptic.EllipticCurve;
import ecc.elliptic.InsecureCurveException;
import ecc.elliptic.secp112r1;

/**
 * Code to handle registrations. Process is as follows.
 * 
 * Client submits "serial" that has been hashed using something like MD5 or SHA.
 * We encrypt this with our private key, turn it into a human readable 40 byte
 * hex string and return it to the client. In reality this process would take
 * place on a remote server, or by email.
 * 
 * Client enter key into his software. The software decrypts it using the public
 * key and compares it with a Hash of the software serial number.
 * 
 * The serial number is unique for the client, it could be created at install
 * time, from a MAC network card address but in our case we use a UUID which we
 * store in a central repository.
 * 
 * We use Elliptic Curve assymetric encryption for encrypting the key. The
 * registration key is personal and tied to a particular install so can't simply
 * be passed around. It is hard for a third party to generate keys. As the code
 * itself can be decompiled and patched out by a skilled user the process
 * doesn't benefit from too much security. It is just good enough to deter naive
 * users.
 * 
 * Cryptography uses: Elliptic Curve Cryptography in Java
 * 
 * https://sourceforge.net/projects/jecc/
 * 
 * @author David George (c) 2013
 * 
 */
public class Registration {
	private static final byte publicKey[] = { 0, 0, 0, 15, 0, -37, 124, 42,
			-65, 98, -29, 94, 102, -128, 118, -66, -83, 32, -120, 0, 0, 0, 14,
			101, -98, -8, -70, 4, 57, 22, -18, -34, -119, 17, 112, 43, 34, 0,
			0, 0, 15, 0, -37, 124, 42, -65, 98, -29, 94, 102, -128, 118, -66,
			-83, 32, -117, 0, 0, 0, 15, 0, -37, 124, 42, -65, 98, -29, 94, 118,
			40, -33, -84, 101, 97, -59, 0, 0, 0, 15, 0, 9, 72, 114, 57, -103,
			90, 94, -25, 107, 85, -7, -62, -16, -104, 0, 0, 0, 14, 54, -33, 10,
			-81, -40, -72, -41, -103, -96, 29, -81, -85, 72, 35, 0, 0, 0, 15,
			0, 9, 115, 101, 99, 112, 49, 49, 50, 114, 49, 1, 0, 0, 0, 15, 0,
			-84, 13, -25, 109, -88, 5, 105, -52, -25, 120, 72, 9, 12, -12, 0,
			0, 0, 15, 1, -48, 59, -57, 109, 85, 80, 29, 87, -12, 82, -55, 87,
			-82, 64 };

	private static Logger logger = LogManager.getLogger("Registration");

	public static boolean register(String key, String serial)
			throws IOException, InsecureCurveException,
			NoSuchAlgorithmException {

		EllipticCurve ec = new EllipticCurve(new secp112r1());
		CryptoSystem cs = new ECCryptoSystem(ec);

		ByteArrayInputStream bais = new ByteArrayInputStream(publicKey);
		DataInputStream dis = new DataInputStream(bais);
		Key pk = new ECKey();
		pk = pk.readKey(dis); // public

		byte[] digest1 = DatatypeConverter
				.parseHexBinary(key.toString().trim());

		// turn the key back into a byte array
		digest1 = cs.decrypt(digest1, pk);

		byte[] digest2 = DatatypeConverter.parseHexBinary(serial);

		if (Arrays.equals(digest1, digest2)) {
			logger.info("Registration keys match");
			return true;

		}

		return false;
	}
}
