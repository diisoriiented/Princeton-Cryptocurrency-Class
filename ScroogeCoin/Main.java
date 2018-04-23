//Taken from: https://github.com/keskival/cryptocurrency-course-materials/blob/master/assignment1/Main.java

import java.math.BigInteger;
import java.security.*;


class Main{
	public static void main(String[] args) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		KeyPair pk_scrooge = KeyPairGenerator.getInstance("RSA").generateKeyPair();
		KeyPair pk_alice = KeyPairGenerator.getInstance("RSA").generateKeyPair();
		KeyPair pk_bob = KeyPairGenerator.getInstance("RSA").generateKeyPair();	

		Tx tx = new Tx();

		//Add coin of 10 to scrooge
		tx.addOutput(10, pk_scrooge.getPublic());

		//Random hash for the whole ledger
		byte[] initialHash = BigInteger.valueOf(0).toByteArray();
		tx.addInput(initialHash, 0);

		//Sign transaction
		tx.signTx(pk_scrooge.getPrivate(), 0);

		UTXOPool utxoPool = new UTXOPool();
		UTXO utxo = new UTXO(tx.getHash(), 0);
		UTXO utxo2 = new UTXO(tx.getHash(), 1);
		utxoPool.addUTXO(utxo, tx.getOutput(0));
		utxoPool.addUTXO(utxo2, tx.getOutput(0));

		Tx tx2 = new Tx();

        // The Transaction.Output of tx at position 0 has a value of 10
        // Input to second transaction is the coins being paid to Alice
        // These coins were created in the first transaction with index 0
        // 
        tx2.addInput(tx.getHash(), 0);

        // I split the coin of value 10 into 3 coins and send all of them for simplicity to
        // the same address (Alice)
        tx2.addOutput(5, pk_alice.getPublic());
        tx2.addOutput(3, pk_alice.getPublic());
		tx2.addOutput(2, pk_alice.getPublic());

		//Sign transaction with Scrooge's private key
		tx2.signTx(pk_scrooge.getPrivate(), 0);

		TxHandler txHandler = new TxHandler(utxoPool);
		//System.out.println("Does my functions work? Function returns: " + txHandler.checkInputs(tx2));
		//System.out.println("Is Scrooges signature valid?? Function returns: " + txHandler.checkSigs(tx2));
		System.out.println("Combined function calls returns: "+txHandler.isValidTx(tx2));

	}

	public static class Tx extends Transaction { 
	        public void signTx(PrivateKey sk, int input) throws SignatureException {
	            Signature sig = null;
	            try {
	                sig = Signature.getInstance("SHA256withRSA");
	                sig.initSign(sk);
	                sig.update(this.getRawDataToSign(input));
	            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
	                throw new RuntimeException(e);
	            }
	            this.addSignature(sig.sign(),input);
	            // Note that this method is incorrectly named, and should not in fact override the Java
	            // object finalize garbage collection related method.
	            this.finalize();
	        }
	}	
}
