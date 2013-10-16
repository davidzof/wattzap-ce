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
