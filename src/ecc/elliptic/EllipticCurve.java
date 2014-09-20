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

/**An implementation of an elliptic curve over a -finite- field.
 *
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;

public class EllipticCurve {

	private BigInteger a, b, p, order;
	private ECPoint generator;
	private BigInteger ppodbf;
	private int pointcmpsize;
	private String name;

	public static final BigInteger COEFA = new BigInteger("4");
	public static final BigInteger COEFB = new BigInteger("27");
	public static final int PRIMESECURITY = 500;

	/**
	 * Constructs an elliptic curve over the finite field of 'mod' elements. The
	 * equation of the curve is on the form : y^2 = x^3 + ax + b.
	 * 
	 * @param a
	 *            the value of 'a' where y^2 = x^3 + ax + b
	 * @param b
	 *            the value of 'b' where y^2 = x^3 + ax + b
	 * @param mod
	 *            The number of elements in the field. IMPORTANT: Must a prime
	 *            number!
	 * @exception InsecureCurveException
	 *                if the curve defined by a and b are singular,
	 *                supersingular, trace one/anomalous. This ensures well
	 *                defined operations and security.
	 */
	public EllipticCurve(BigInteger a, BigInteger b, BigInteger p)
			throws InsecureCurveException {

		this.a = a;
		this.b = b;
		this.p = p;
		if (!p.isProbablePrime(PRIMESECURITY)) {
			// System.out.println("THIS CANNOT HAPPEN!!! "+p+" is not a prime!");
			// throw new
			// InsecureCurveException(InsecureCurveException.NONPRIMEMODULUS,this);
		}
		if (isSingular())
			throw new InsecureCurveException(InsecureCurveException.SINGULAR,
					this);

		byte[] pb = p.toByteArray();
		if (pb[0] == 0)
			pointcmpsize = pb.length;
		else
			pointcmpsize = pb.length + 1;
		// ppodbf = (p.add(BigInteger.ONE)).shiftRight(2);
		name = "";

		// FIXME compute the order of the group
		// FIXME compute a generator for the group
	}

	public EllipticCurve(ECParameters ecp) throws InsecureCurveException {
		this(ecp.a(), ecp.b(), ecp.p());
		order = ecp.order();
		name = ecp.toString();
		try {
			generator = new ECPoint(this, ecp.generatorX(), ecp.generatorY());
			generator.fastCache();
		} catch (NotOnMotherException e) {
			System.out
					.println("Error defining EllipticCurve: generator not on mother!");
		}
	}

	public void writeCurve(DataOutputStream output) throws IOException {
		byte[] ab = a.toByteArray();
		output.writeInt(ab.length);
		output.write(ab);
		byte[] bb = b.toByteArray();
		output.writeInt(bb.length);
		output.write(bb);
		byte[] pb = p.toByteArray();
		output.writeInt(pb.length);
		output.write(pb);
		byte[] ob = order.toByteArray();
		output.writeInt(ob.length);
		output.write(ob);
		byte[] gb = generator.compress();
		output.writeInt(gb.length);
		output.write(gb);
		byte[] ppb = getPPODBF().toByteArray();
		output.writeInt(ppb.length);
		output.write(ppb);
		output.writeInt(pointcmpsize);
		output.writeUTF(name);
	}

	protected EllipticCurve(DataInputStream input) throws IOException {
		byte[] ab = new byte[input.readInt()];
		input.read(ab);
		a = new BigInteger(ab);
		byte[] bb = new byte[input.readInt()];
		input.read(bb);
		b = new BigInteger(bb);
		byte[] pb = new byte[input.readInt()];
		input.read(pb);
		p = new BigInteger(pb);
		byte[] ob = new byte[input.readInt()];
		input.read(ob);
		order = new BigInteger(ob);
		byte[] gb = new byte[input.readInt()];
		input.read(gb);
		generator = new ECPoint(gb, this);
		byte[] ppb = new byte[input.readInt()];
		input.read(ppb);
		ppodbf = new BigInteger(ppb);
		pointcmpsize = input.readInt();
		name = input.readUTF();
		generator.fastCache();
	}

	public boolean isSingular() {

		BigInteger aa = a.pow(3);
		BigInteger bb = b.pow(2);

		BigInteger result = ((aa.multiply(COEFA)).add(bb.multiply(COEFB)))
				.mod(p);

		if (result.compareTo(BigInteger.ZERO) == 0)
			return true;
		else
			return false;

	}

	// FIXME!!!!!!!!!!
	public BigInteger calculateOrder() {
		return null;
	}

	// FIXME!!!!!!!!
	public ECPoint calculateGenerator() {
		return null;
	}

	public boolean onCurve(ECPoint q) {

		if (q.isZero())
			return true;
		BigInteger y_square = (q.gety()).modPow(new BigInteger("2"), p);
		BigInteger x_cube = (q.getx()).modPow(new BigInteger("3"), p);
		BigInteger x = q.getx();

		BigInteger dum = ((x_cube.add(a.multiply(x))).add(b)).mod(p);

		if (y_square.compareTo(dum) == 0)
			return true;
		else
			return false;

	}

	/** Returns the order of the group */
	public BigInteger getOrder() {
		return order;
	}

	public ECPoint getZero() {
		return new ECPoint(this);
	}

	public BigInteger geta() {
		return a;
	}

	public BigInteger getb() {
		return b;
	}

	public BigInteger getp() {
		return p;
	}

	public int getPCS() {
		return pointcmpsize;
	}

	/** Returns a generator for this EllipticCurve. */
	public ECPoint getGenerator() {
		return generator;
	}

	public String toString() {
		if (name == null)
			return "y^2 = x^3 + " + a + "x + " + b + " ( mod " + p + " )";
		else if (name.equals(""))
			return "y^2 = x^3 + " + a + "x + " + b + " ( mod " + p + " )";
		else
			return name;
	}

	public BigInteger getPPODBF() {
		if (ppodbf == null) {
			ppodbf = p.add(BigInteger.ONE).shiftRight(2);
		}
		return ppodbf;
	}
}
