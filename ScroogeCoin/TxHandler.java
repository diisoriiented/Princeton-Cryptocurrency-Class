import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/*
    ScroogeCoin Transaction Handler class
    CREATED AND CODED BY Diisoriiented 4/22/2018
    Questions? Email me at diis.cybersec@gmail.com

    Coded for: https://www.coursera.org/learn/cryptocurrency/home/
*/
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
        return (checkInputs(tx) && checkSigs(tx) && checkMultUTXO(tx) && checkValues(tx));
    }

    /**     
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        ArrayList<Transaction> acceptedTxs = new ArrayList<Transaction>();
        for (Transaction tx : possibleTxs){
            if(isValidTx(tx)){
                acceptedTxs.add(tx);
                byte[] hash = tx.getHash();
                ArrayList<UTXO> UTXOs = ledger.getAllUTXO();
                for (UTXO utxo : UTXOs){
                    boolean areEqual = false;
                    byte[] utxo_hash = utxo.getTxHash();
                    for (int i = 0; i<hash.length; i++){
                        if (hash[i] != utxo_hash[i]) break;
                        areEqual = true;
                    }
                    if (areEqual = true){
                        ledger.removeUTXO(utxo);
                    }
                }
            }
        }
        return acceptedTxs.toArray(new Transaction[acceptedTxs.size()]);
    }

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
        if (numSatisfiedInputs == tx.numInputs()) return true;
        return false;
    }

    /*
        Verifies the signatures of all inputs of tx by verifying signing the data with the public key of the output
    */
    private boolean checkSigs(Transaction tx){
        ArrayList<Transaction.Input> inputs = tx.getInputs();
        ArrayList<UTXO> UTXOs = ledger.getAllUTXO();
        int valid_sigs = 0;
        for (Transaction.Input input : inputs){
            byte[] sig = input.signature;
            byte[] message = tx.getRawDataToSign(input.outputIndex);
            for ( UTXO utxo : UTXOs){
                Transaction.Output out = ledger.getTxOutput(utxo);
                if( Crypto.verifySignature(out.address, message, sig) ){
                    valid_sigs++;
                    break;
                }
            }
        }
        if(valid_sigs == tx.numInputs()) return true;
        return false;
    }

    /*
        Iterates through inputs and the list of UTXOs and checks for multiple transaction values 
    */
    private boolean checkMultUTXO(Transaction tx){
        ArrayList<Transaction.Input> inputs = tx.getInputs();
        ArrayList<UTXO> UTXOs = ledger.getAllUTXO();
        int numTimes = 0;
        for(Transaction.Input input : inputs){
            byte[] hash = input.prevTxHash;
            for(UTXO utxo : UTXOs){
                boolean areEqual = false;
                byte[] utxo_hash = utxo.getTxHash();
                for (int i = 0; i<hash.length; i++){
                    if (hash[i] != utxo_hash[i]) break;
                    areEqual = true;
                }
                if (areEqual){
                    numTimes++;
                }
            }
            if (numTimes > 1) return false;
            numTimes = 0;
        }
        return true;
    }


    /*
        Iterates over TX outputs and corresponding UTXO outputs and sums their values to make sure they are identical.
    */
    private boolean checkValues(Transaction tx){
        ArrayList<Transaction.Input> inputs = tx.getInputs();
        ArrayList<UTXO> UTXOs = ledger.getAllUTXO();
        ArrayList<Transaction.Output> outs = tx.getOutputs();
        double input_sum = 0;
        double output_sum =0;

        for (Transaction.Output out : outs){
            if(out.value < 0) return false;
            output_sum += out.value;
        }

        UTXO matching_utxo;

        for(Transaction.Input input : inputs){
            byte[] hash = input.prevTxHash;
            boolean areEqual = true;

            for(UTXO utxo : UTXOs){
                byte[] utxo_hash = utxo.getTxHash();
                for (int i = 0; i<hash.length; i++){
                    if (hash[i] != utxo_hash[i]) break;
                }
                if (areEqual){
                matching_utxo = utxo;
                Transaction.Output matching_input = ledger.getTxOutput(matching_utxo);
                if (matching_input.value < 0) return false;
                input_sum += matching_input.value;
                }
            } 
        }
        //System.out.println("input sum: " + input_sum + " output sum: " + output_sum);
        if(input_sum >= output_sum) return true;
        return false;
    }
}
