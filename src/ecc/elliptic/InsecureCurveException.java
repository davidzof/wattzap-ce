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

public class InsecureCurveException extends Exception {

	public static final int NONPRIMEMODULUS = -1;
	public static final int SINGULAR = 0;
	public static final int SUPERSINGULAR = 1;
	public static final int ANOMALOUS = 2;
	public static final int TRACEONE = 3;

	private int error;

	private EllipticCurve sender;

	public InsecureCurveException(EllipticCurve sender) {
		error = SINGULAR;
		this.sender = sender;
	}

	public InsecureCurveException(int e, EllipticCurve sender) {
		error = e;
		this.sender = sender;
	}

	public int getError() {
		return error;
	}

	public String getErrorString() {
		if (error == SINGULAR)
			return "SINGULAR";
		else if (error == NONPRIMEMODULUS)
			return "NONPRIMEMODULUS";
		else if (error == SUPERSINGULAR)
			return "SUPERSINGULAR";
		else if (error == ANOMALOUS)
			return "ANOMALOUS";
		else if (error == TRACEONE)
			return "TRACEONE";
		else
			return "UNKNOWN ERROR";
	}

	public EllipticCurve getSender() {
		return sender;
	}
}
