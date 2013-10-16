package ecc.elliptic;

public class NotOnMotherException extends Exception {

	private ECPoint sender;

	public NotOnMotherException(ECPoint sender) {
		this.sender = sender;
	}

	public String getErrorString() {
		return "NotOnMother";
	}

	public ECPoint getSource() {
		return sender;
	}
}
