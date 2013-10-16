package ecc.elliptic;

import java.math.BigInteger;

public class secp160r1 implements ECParameters {
	private static final BigInteger p = new BigInteger("FFFFFFFF" + "FFFFFFFF"
			+ "FFFFFFFF" + "FFFFFFFF" + "7FFFFFFF", 16);

	private static final BigInteger a = new BigInteger("FFFFFFFF" + "FFFFFFFF"
			+ "FFFFFFFF" + "FFFFFFFF" + "7FFFFFFC", 16);

	private static final BigInteger b = new BigInteger("1C97BEFC" + "54BD7A8B"
			+ "65ACF89F" + "81D4D4AD" + "C565FA45", 16);

	private static final BigInteger S = new BigInteger("1053CDE4" + "2C14D696"
			+ "E6768756" + "1517533B" + "F3F83345", 16);

	private static final BigInteger gx = new BigInteger("4A96B568" + "8EF57328"
			+ "46646989" + "68C38BB9" + "13CBFC82", 16);

	private static final BigInteger gy = new BigInteger("23A62855" + "3168947D"
			+ "59DCC912" + "04235137" + "7AC5FB32", 16);

	private static final BigInteger n = new BigInteger("01" + "00000000"
			+ "00000000" + "0001F4C8" + "F927AED3" + "CA752257", 16);

	public BigInteger a() {
		return a;
	}

	public BigInteger b() {
		return b;
	}

	public BigInteger p() {
		return p;
	}

	public BigInteger generatorX() {
		return gx;
	}

	public BigInteger generatorY() {
		return gy;
	}

	public BigInteger order() {
		return n;
	}

	public String toString() {
		return "secp160r1";
	}

	public static void main(String[] args) {
		System.out.println("a:  " + a);
		System.out.println("b:  " + b);
		System.out.println("p:  " + p);
		System.out.println("gx: " + gx);
		System.out.println("gy: " + gy);
		System.out.println("n:  " + n);
		System.out.println("p.toByteArray().length: " + p.toByteArray().length);
	}
}
