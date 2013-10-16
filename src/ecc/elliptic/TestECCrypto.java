package ecc.elliptic;

import java.util.Arrays;

import ecc.CryptoSystem;
import ecc.Key;

public class TestECCrypto {
    public static void main(String[] args) {
	try {
	    EllipticCurve ec = new EllipticCurve(new secp256r1());

	    CryptoSystem cs = new ECCryptoSystem(ec);

	    Key sk = (ECKey)cs.generateKey();
	    Key pk = sk.getPublic();

	    byte[] test1 = {1,2,3,4,5,6,7,8,9,0,11,12,13,14,15,16,17,18,19,10};
	    byte[] crypte = cs.encrypt(test1, test1.length, pk);
	    
	    System.out.println("test len " + crypte.length);
	    for (byte b : crypte) {
			System.out.print(String.format("%02x", b));
		}
	    System.out.println("\n");
	    
	    byte[] test2 = cs.decrypt(crypte, sk);
	    
	    for (byte b : test2) {
			System.out.print(String.format("%02x", b));
		}
	    System.out.println("\n");
	    if(Arrays.equals(test1, test1)) System.out.println("Testing...");
	    if(Arrays.equals(test1, test2)) {
		System.out.println("Succes");
	    } else {
		System.out.print("Fail\n{");
		for(int i = 0; i < 20; i++) {
		    System.out.print(test2[i]+",");
		}
		System.out.println("}");
	    }
	} catch (InsecureCurveException e) {
	    System.out.println("TestCryptoStreams: "+e);
	}
    }
}
