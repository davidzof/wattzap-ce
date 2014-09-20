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
