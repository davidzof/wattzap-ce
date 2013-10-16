package ecc.elliptic;

import java.math.BigInteger;
import java.security.MessageDigest;

import ecc.CryptoSystem;
import ecc.Key;
import ecc.Rand;

public class ECCryptoSystem implements CryptoSystem {
	MessageDigest hash;

	private EllipticCurve ec;

	public ECCryptoSystem(EllipticCurve ec) {
		this.ec = ec;
		try {
			hash = MessageDigest.getInstance("SHA-1");
		} catch (java.security.NoSuchAlgorithmException e) {
			System.out.println("RSACryptoSystem: THIS CANNOT HAPPEN\n" + e);
			System.exit(0);
		}
	}

	public int blockSize() {
		return 20;
	}

	public byte[] encrypt(byte[] input, int numbytes, Key key) {
		ECKey ek = (ECKey) key;

		byte[] res = new byte[ek.mother.getPCS() + numbytes];
		hash.reset();

		BigInteger rk = new BigInteger(ek.mother.getp().bitLength() + 17,
				Rand.om);
		if (ek.mother.getOrder() != null) {
			rk = rk.mod(ek.mother.getOrder());
		}
		ECPoint gamma = ek.mother.getGenerator().multiply(rk);
		ECPoint sec = ek.beta.multiply(rk);
		System.arraycopy(gamma.compress(), 0, res, 0, ek.mother.getPCS());
		hash.update(sec.getx().toByteArray());
		hash.update(sec.gety().toByteArray());
		byte[] digest = hash.digest();
		for (int j = 0; j < numbytes; j++) {
			res[j + ek.mother.getPCS()] = (byte) (input[j] ^ digest[j]);
		}
		return res;
	}

	public byte[] decrypt(byte[] input, Key key) {
		ECKey dk = (ECKey) key;
		byte[] res = new byte[input.length - dk.mother.getPCS()];
		byte[] gammacom = new byte[dk.mother.getPCS()];
		hash.reset();

		System.arraycopy(input, 0, gammacom, 0, dk.mother.getPCS());
		ECPoint gamma = new ECPoint(gammacom, dk.mother);
		ECPoint sec = gamma.multiply(dk.sk);
		if (sec.isZero()) {
			hash.update(BigInteger.ZERO.toByteArray());
			hash.update(BigInteger.ZERO.toByteArray());
		} else {
			hash.update(sec.getx().toByteArray());
			hash.update(sec.gety().toByteArray());
		}
		byte[] digest = hash.digest();
		for (int j = 0; j < input.length - dk.mother.getPCS(); j++) {
			res[j] = (byte) (input[j + dk.mother.getPCS()] ^ digest[j]);
		}
		return res;
	}

	/**
	 * This method generates a new key for the cryptosystem.
	 * 
	 * @return the new key generated
	 */
	public Key generateKey() {
		return new ECKey(ec);
	}

	public String toString() {
		return "ECC - " + ec.toString();
	}

}
