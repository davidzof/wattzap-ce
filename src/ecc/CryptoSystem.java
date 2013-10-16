package ecc;

/**
 * This interface is used to model a modern cryptosystem. It contains methods to
 * encrypt and decrypt and methods to generate keys for the specific
 * cryptosystem. In an actual implementation it would be a good idea to
 * initialize a key inside the constructor method.
 */
public interface CryptoSystem {

	/**
	 * Encrypts the string p.
	 * 
	 * @param plain
	 *            the plaintext to be encrypted.
	 * @param ek
	 *            The (public) key to use for encryption.
	 * @return the input string encrypted with the current key.
	 */
	public byte[] encrypt(byte[] plain, int numbytes, Key ek);

	/**
	 * Decrypts the string c.
	 * 
	 * @param cipher
	 *            the ciphertext to be decrypted.
	 * @param sk
	 *            the (secret) key to use for decryption.
	 * @return the input string decrypted with the current key.
	 */
	public byte[] decrypt(byte[] cipher, Key dk);

	/**
	 * This method generates a new key for the cryptosystem.
	 * 
	 * @return the new key generated
	 */
	public Key generateKey();

	/**
	 * This method returns the maximum size of blocks it can encrypt.
	 * 
	 * @return the maximum block size the system can encrypt.
	 */
	public int blockSize();

	/** Returns a String describing this CryptoSystem */
	public String toString();
}
