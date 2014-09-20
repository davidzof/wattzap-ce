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
package ecc;

/**
 * (c) Mads Johan Jurik used with permission
 */
public class RIPEMD160 {

	private static final int[][] ArgArray = {
			{ 11, 14, 15, 12, 5, 8, 7, 9, 11, 13, 14, 15, 6, 7, 9, 8, 7, 6, 8,
					13, 11, 9, 7, 15, 7, 12, 15, 9, 11, 7, 13, 12, 11, 13, 6,
					7, 14, 9, 13, 15, 14, 8, 13, 6, 5, 12, 7, 5, 11, 12, 14,
					15, 14, 15, 9, 8, 9, 14, 5, 6, 8, 6, 5, 12, 9, 15, 5, 11,
					6, 8, 13, 12, 5, 12, 13, 14, 11, 8, 5, 6 },
			{ 8, 9, 9, 11, 13, 15, 15, 5, 7, 7, 8, 11, 14, 14, 12, 6, 9, 13,
					15, 7, 12, 8, 9, 11, 7, 7, 12, 7, 6, 15, 13, 11, 9, 7, 15,
					11, 8, 6, 6, 14, 12, 13, 5, 14, 13, 13, 7, 5, 15, 5, 8, 11,
					14, 14, 6, 14, 6, 9, 12, 9, 12, 5, 15, 8, 8, 5, 12, 9, 12,
					5, 14, 6, 8, 13, 6, 5, 15, 13, 11, 11 } };

	private static final int[][] IndexArray = {
			{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 7, 4, 13,
					1, 10, 6, 15, 3, 12, 0, 9, 5, 2, 14, 11, 8, 3, 10, 14, 4,
					9, 15, 8, 1, 2, 7, 0, 6, 13, 11, 5, 12, 1, 9, 11, 10, 0, 8,
					12, 4, 13, 3, 7, 15, 14, 5, 6, 2, 4, 0, 5, 9, 7, 12, 2, 10,
					14, 1, 3, 8, 11, 6, 15, 13 },
			{ 5, 14, 7, 0, 9, 2, 11, 4, 13, 6, 15, 8, 1, 10, 3, 12, 6, 11, 3,
					7, 0, 13, 5, 10, 14, 15, 8, 12, 4, 9, 1, 2, 15, 5, 1, 3, 7,
					14, 6, 9, 11, 8, 12, 2, 10, 0, 4, 13, 8, 6, 4, 1, 3, 11,
					15, 0, 5, 12, 2, 13, 9, 7, 10, 14, 12, 15, 10, 4, 1, 5, 8,
					7, 6, 2, 13, 14, 0, 3, 9, 11 } };

	private int[] MDbuf;

	public RIPEMD160() {
		MDbuf = new int[5];
		MDbuf[0] = 0x67452301;
		MDbuf[1] = 0xefcdab89;
		MDbuf[2] = 0x98badcfe;
		MDbuf[3] = 0x10325476;
		MDbuf[4] = 0xc3d2e1f0;
		working = new int[16];
		working_ptr = 0;
		msglen = 0;
	}

	public void reset() {
		MDbuf = new int[5];
		MDbuf[0] = 0x67452301;
		MDbuf[1] = 0xefcdab89;
		MDbuf[2] = 0x98badcfe;
		MDbuf[3] = 0x10325476;
		MDbuf[4] = 0xc3d2e1f0;
		working = new int[16];
		working_ptr = 0;
		msglen = 0;
	}

	private void compress(int[] X) {
		int index = 0;

		int a, b, c, d, e;
		int A, B, C, D, E;
		int temp, s;

		A = a = MDbuf[0];
		B = b = MDbuf[1];
		C = c = MDbuf[2];
		D = d = MDbuf[3];
		E = e = MDbuf[4];

		for (; index < 16; index++) {
			// The 16 FF functions - round 1 */
			temp = a + (b ^ c ^ d) + X[IndexArray[0][index]];
			a = e;
			e = d;
			d = (c << 10) | (c >>> 22);
			c = b;
			s = ArgArray[0][index];
			b = ((temp << s) | (temp >>> (32 - s))) + a;

			// The 16 JJJ functions - parallel round 1 */
			temp = A + (B ^ (C | ~D)) + X[IndexArray[1][index]] + 0x50a28be6;
			A = E;
			E = D;
			D = (C << 10) | (C >>> 22);
			C = B;
			s = ArgArray[1][index];
			B = ((temp << s) | (temp >>> (32 - s))) + A;
		}

		for (; index < 32; index++) {
			// The 16 GG functions - round 2 */
			temp = a + ((b & c) | (~b & d)) + X[IndexArray[0][index]]
					+ 0x5a827999;
			a = e;
			e = d;
			d = (c << 10) | (c >>> 22);
			c = b;
			s = ArgArray[0][index];
			b = ((temp << s) | (temp >>> (32 - s))) + a;

			// The 16 III functions - parallel round 2 */
			temp = A + ((B & D) | (C & ~D)) + X[IndexArray[1][index]]
					+ 0x5c4dd124;
			A = E;
			E = D;
			D = (C << 10) | (C >>> 22);
			C = B;
			s = ArgArray[1][index];
			B = ((temp << s) | (temp >>> (32 - s))) + A;
		}

		for (; index < 48; index++) {
			// The 16 HH functions - round 3 */
			temp = a + ((b | ~c) ^ d) + X[IndexArray[0][index]] + 0x6ed9eba1;
			a = e;
			e = d;
			d = (c << 10) | (c >>> 22);
			c = b;
			s = ArgArray[0][index];
			b = ((temp << s) | (temp >>> (32 - s))) + a;

			// The 16 HHH functions - parallel round 3 */
			temp = A + ((B | ~C) ^ D) + X[IndexArray[1][index]] + 0x6d703ef3;
			A = E;
			E = D;
			D = (C << 10) | (C >>> 22);
			C = B;
			s = ArgArray[1][index];
			B = ((temp << s) | (temp >>> (32 - s))) + A;
		}

		for (; index < 64; index++) {
			// The 16 II functions - round 4 */
			temp = a + ((b & d) | (c & ~d)) + X[IndexArray[0][index]]
					+ 0x8f1bbcdc;
			a = e;
			e = d;
			d = (c << 10) | (c >>> 22);
			c = b;
			s = ArgArray[0][index];
			b = ((temp << s) | (temp >>> (32 - s))) + a;

			// The 16 GGG functions - parallel round 4 */
			temp = A + ((B & C) | (~B & D)) + X[IndexArray[1][index]]
					+ 0x7a6d76e9;
			A = E;
			E = D;
			D = (C << 10) | (C >>> 22);
			C = B;
			s = ArgArray[1][index];
			B = ((temp << s) | (temp >>> (32 - s))) + A;
		}

		for (; index < 80; index++) {
			// The 16 JJ functions - round 5 */
			temp = a + (b ^ (c | ~d)) + X[IndexArray[0][index]] + 0xa953fd4e;
			a = e;
			e = d;
			d = (c << 10) | (c >>> 22);
			c = b;
			s = ArgArray[0][index];
			b = ((temp << s) | (temp >>> (32 - s))) + a;

			// The 16 FFF functions - parallel round 5 */
			temp = A + (B ^ C ^ D) + X[IndexArray[1][index]];
			A = E;
			E = D;
			D = (C << 10) | (C >>> 22);
			C = B;
			s = ArgArray[1][index];
			B = ((temp << s) | (temp >>> (32 - s))) + A;
		}

		/* combine results */
		D += c + MDbuf[1]; /* final result for MDbuf[0] */
		MDbuf[1] = MDbuf[2] + d + E;
		MDbuf[2] = MDbuf[3] + e + A;
		MDbuf[3] = MDbuf[4] + a + B;
		MDbuf[4] = MDbuf[0] + b + C;
		MDbuf[0] = D;
	}

	private void MDfinish(int[] array, int lswlen, int mswlen) {
		int[] X = array; /* message words */

		/* append the bit m_n == 1 */
		X[(lswlen >> 2) & 15] ^= 1 << (((lswlen & 3) << 3) + 7);

		if ((lswlen & 63) > 55) {
			/* length goes to next block */
			compress(X);
			for (int i = 0; i < 14; i++)
				X[i] = 0;
		}

		/* append length in bits */
		X[14] = lswlen << 3;
		X[15] = (lswlen >> 29) | (mswlen << 3);
		compress(X);
	}

	private int[] working;
	private int working_ptr;
	private int msglen;

	public void update(byte input) {
		working[working_ptr >> 2] ^= ((int) input) << ((working_ptr & 3) << 3);
		working_ptr++;
		if (working_ptr == 64) {
			compress(working);
			for (int j = 0; j < 16; j++)
				working[j] = 0;
			working_ptr = 0;
		}
		msglen++;
	}

	public void update(byte[] input) {
		for (int i = 0; i < input.length; i++) {
			working[working_ptr >> 2] ^= ((int) input[i]) << ((working_ptr & 3) << 3);
			working_ptr++;
			if (working_ptr == 64) {
				compress(working);
				for (int j = 0; j < 16; j++)
					working[j] = 0;
				working_ptr = 0;
			}
		}
		msglen += input.length;
	}

	public void update(byte[] input, int offset, int len) {
		if (offset + len >= input.length) {
			for (int i = offset; i < input.length; i++) {
				working[working_ptr >> 2] ^= ((int) input[i]) << ((working_ptr & 3) << 3);
				working_ptr++;
				if (working_ptr == 64) {
					compress(working);
					for (int j = 0; j < 16; j++)
						working[j] = 0;
					working_ptr = 0;
				}
			}
			msglen += input.length - offset;
		} else {
			for (int i = offset; i < offset + len; i++) {
				working[working_ptr >> 2] ^= ((int) input[i]) << ((working_ptr & 3) << 3);
				working_ptr++;
				if (working_ptr == 64) {
					compress(working);
					for (int j = 0; j < 16; j++)
						working[j] = 0;
					working_ptr = 0;
				}
			}
			msglen += len;
		}
	}

	public void update(String s) {
		byte[] bytearray = new byte[s.length()];
		for (int i = 0; i < bytearray.length; i++) {
			bytearray[i] = (byte) s.charAt(i);
		}
		update(bytearray);
	}

	public byte[] digest() {
		MDfinish(working, msglen, 0);
		byte[] res = new byte[20];
		for (int i = 0; i < 20; i++)
			res[i] = (byte) ((MDbuf[i >> 2] >>> ((i & 3) << 3)) & 0x000000FF);
		return res;
	}

	public byte[] digest(byte[] input) {
		update(input);
		return digest();
	}

	public byte[] digest(byte[] input, int offset, int len) {
		update(input, offset, len);
		return digest();
	}

	public int[] intdigest() {
		int[] res = new int[5];
		for (int i = 0; i < 5; i++)
			res[i] = MDbuf[i];
		return res;
	}
}

/*
 * Text RIPEMD160 RIPEMD128 "" 9c1185a5c5e9fc54612808977ee8f548b2258d31
 * cdf26213a150dc3ecb610f18f6b38b46 "a" 0bdc9d2d256b3ee9daae347be6f4dc835a467ffe
 * 86be7afa339d0fc7cfc785e72f578d33 "abc"
 * 8eb208f7e05d987a9b044a8e98c6b087f15a0bfc c14a12199c66e4ba84636b0f69144c77
 * "message digest" 5d0689ef49d2fae572b881b123a85ffa21595f36
 * 9e327b3d6e523062afc1132d7df9d1b8 1) f71c27109c692c1b56bbdceb5b9d2865b3708dbc
 * fd2aa607f71dc8f510714922b371834e 2) 12a053384a9c0c88e405a06c27dcf49ada62eb2b
 * a1aa0689d0fafa2ddc22e88b49133a06 3) b0e20b6e3116640286ed3a87a5713079b21f5189
 * d1e959eb179c911faea4624c60c5c702 8 x "1234567890"
 * 9b752e45573d4b39f4dbd3323cab82bf63326bfb 3f45ef194732c2dbb2c4a2c769795fa3 1
 * mio. x "a" 52783243c1697bdbe16d37f97f68f08325dc1528
 * 4a7f5723f954eba1216c9d8f6320431f
 * 
 * 1) "abcdefghijklmnopqrstuvwxyz" 2)
 * "abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq" 3)
 * "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
 */
