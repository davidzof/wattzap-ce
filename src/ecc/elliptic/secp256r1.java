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
package ecc.elliptic;

import java.math.BigInteger;

public class secp256r1 implements ECParameters {
	private static final BigInteger p = new BigInteger("FFFFFFFF" + "00000001"
			+ "00000000" + "00000000" + "00000000" + "FFFFFFFF" + "FFFFFFFF"
			+ "FFFFFFFF", 16);

	private static final BigInteger a = new BigInteger("FFFFFFFF" + "00000001"
			+ "00000000" + "00000000" + "00000000" + "FFFFFFFF" + "FFFFFFFF"
			+ "FFFFFFFC", 16);

	private static final BigInteger b = new BigInteger("5AC635D8" + "AA3A93E7"
			+ "B3EBBD55" + "769886BC" + "651D06B0" + "CC53B0F6" + "3BCE3C3E"
			+ "27D2604B", 16);

	private static final BigInteger S = new BigInteger("C49D3608" + "86E70493"
			+ "6A6678E1" + "139D26B7" + "819F7E90", 16);

	private static final BigInteger gx = new BigInteger("6B17D1F2" + "E12C4247"
			+ "F8BCE6E5" + "63A440F2" + "77037D81" + "2DEB33A0" + "F4A13945"
			+ "D898C296", 16);

	private static final BigInteger gy = new BigInteger("4FE342E2" + "FE1A7F9B"
			+ "8EE7EB4A" + "7C0F9E16" + "2BCE3357" + "6B315ECE" + "CBB64068"
			+ "37BF51F5", 16);

	private static final BigInteger n = new BigInteger("FFFFFFFF" + "00000000"
			+ "FFFFFFFF" + "FFFFFFFF" + "BCE6FAAD" + "A7179E84" + "F3B9CAC2"
			+ "FC632551", 16);

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
		return "secp256r1";
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
