import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public UTXOPool ledger;
    public UTXOPool ledger_copy;

    public TxHandler(UTXOPool utxoPool) {
        this.ledger_copy = new UTXOPool(utxoPool);
        this.ledger = utxoPool;
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        return (checkInputs(tx));
    }

    /**     
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    /*public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
    }*/

    /*
        Verfies that all inputs to TX are in the UTXO Pool
    */
    private boolean checkInputs(Transaction tx){
        ArrayList<Transaction.Input> inputs = tx.getInputs();
        ArrayList<UTXO> UTXOs = ledger.getAllUTXO();
        int numSatisfiedInputs = 0;
        for ( Transaction.Input input : inputs){
            byte[] hash = input.prevTxHash;
            for (UTXO utxo : UTXOs){
                boolean areEqual = false;
                byte[] utxo_hash = utxo.getTxHash();
                for (int i = 0; i<hash.length; i++){
                    if (hash[i] != utxo_hash[i]) break;
                    areEqual = true;
                }
                if (areEqual){
                    numSatisfiedInputs++;
                    break;
                } 
            }
        }
        if (numSatisfiedInputs == tx.numInputs()) {
            return true;
        }
        return false;
    }
}
