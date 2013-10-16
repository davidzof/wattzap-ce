package ecc.elliptic;

import java.math.BigInteger;

public class secp112r1 implements ECParameters {
	private static final BigInteger p = new BigInteger("DB7C" + "2ABF62E3"
			+ "5E668076" + "BEAD208B", 16);

	private static final BigInteger a = new BigInteger("DB7C" + "2ABF62E3"
			+ "5E668076" + "BEAD2088", 16);

	private static final BigInteger b = new BigInteger("659E" + "F8BA0439"
			+ "16EEDE89" + "11702B22", 16);

	private static final BigInteger S = new BigInteger("00F50B02" + "8E4D696E"
			+ "67687561" + "51752904" + "72783FB1", 16);

	private static final BigInteger gx = new BigInteger("09487239" + "995A5EE7"
			+ "6B55F9C2" + "F098", 16);

	private static final BigInteger gy = new BigInteger("A89C" + "E5AF8724"
			+ "C0A23E0E" + "0FF77500", 16);

	private static final BigInteger n = new BigInteger("DB7C" + "2ABF62E3"
			+ "5E7628DF" + "AC6561C5", 16);

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
		return "secp112r1";
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
