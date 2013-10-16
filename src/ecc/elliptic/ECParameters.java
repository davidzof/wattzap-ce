package ecc.elliptic;

/** Specifications completely defining an elliptic curve. Used to define an
 *elliptic curve by EllipticCurve.define(ECParamters ecp).
 *NOTE: This is designed for an elliptic curve on the form:
 *      y^2 = x^3 + ax + b (mod p)
 *--with fixed generator and precomputed order.
 */

import java.math.BigInteger;

public interface ECParameters {

	public BigInteger a();

	public BigInteger b();

	public BigInteger p();

	/** returns the x value of the generator */
	public BigInteger generatorX();

	/** returns the y value of the generator */
	public BigInteger generatorY();

	public BigInteger order();

	public String toString();
}
