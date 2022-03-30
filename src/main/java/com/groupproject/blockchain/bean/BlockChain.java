package com.groupproject.blockchain.bean;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

public class BlockChain {
    //store block in arraylist
    public static ArrayList<Block> blockChain = new ArrayList<Block>();
    public static HashMap<String, TxOut> UTXOs = new HashMap<String, TxOut>();

    public static int difficulty = 3;
    public static float minimumTransaction = 0.1f;

    public static void main(String[] args) {
        //Setup Bouncy castle as a Security Provider
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        //Create wallets:
        Wallet walletA = new Wallet();
        Wallet walletB = new Wallet();
        //generate genesis block
        Block genesisBlock = new Block("0", 0);
        genesisBlock.addCoinbaseTx(walletA);
//        genesisBlock.addTransaction(genesisTransaction);
        genesisBlock.mineBlock(difficulty);
        blockChain.add(genesisBlock);

        //testing
        System.out.println("\nWalletA's balance is: " + walletA.getBalance()); //50
        System.out.println("\nWalletA is Attempting to send funds (40) to WalletB...");
        Block block1 = generateNextBlock();
        block1.addCoinbaseTx(walletA);
        block1.addTransaction(walletA.sendFunds(walletB.publicKey, 20f));
        block1.addTransaction(walletA.sendFunds(walletB.publicKey, 10f));
        block1.mineBlock(difficulty);
        blockChain.add(block1);
        System.out.println("\nWalletA's balance is: " + walletA.getBalance()); //20
        System.out.println("WalletB's balance is: " + walletB.getBalance()); //30

        System.out.println("\nWalletA Attempting to send more funds (1000) than it has...");
        Block block2 = generateNextBlock();
        block2.addCoinbaseTx(walletA);
        block2.addTransaction(walletA.sendFunds(walletB.publicKey, 1000f));
        block2.mineBlock(difficulty);
        blockChain.add(block2);
        System.out.println("\nWalletA's balance is: " + walletA.getBalance()); //70
        System.out.println("WalletB's balance is: " + walletB.getBalance()); //30

        System.out.println("\nWalletB is Attempting to send funds (20) to WalletA...");
        Block block3 = generateNextBlock();
        block3.addTransaction(walletB.sendFunds( walletA.publicKey, 20)); //fail cuz no coinbase Tx
        block3.mineBlock(difficulty);
        blockChain.add(block3);
        System.out.println("\nWalletA's balance is: " + walletA.getBalance()); //70
        System.out.println("WalletB's balance is: " + walletB.getBalance()); //30

        //Validation
        if(isValidNewBlock()){
            System.out.println("Blockchain validation successful!\n");
        }

        //print the block info via gson package
//        String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockChain);
//        System.out.println("\nThe block chain: ");
//        System.out.println(blockchainJson);
    }


    //Generating A Block
    public static Block generateNextBlock(){
        Block previousBlock = blockChain.get(blockChain.size()-1);
        int nextIndex = previousBlock.index+1;
        String previousHash = blockChain.get(blockChain.size()-1).hash;
        Block newBlock = new Block(previousHash, nextIndex);
        return newBlock;
    }

    //Block Integrity Validation
    public static boolean isValidNewBlock(){
        Block previousBlock;
        Block newBlock;
        //a temporary working list of unspent transactions at a given block state.
//        HashMap<String, TxOut> tempUTXOs = new HashMap<String, TxOut>();
//        tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        for(int i=1; i< blockChain.size(); i++){
            previousBlock = blockChain.get(i-1);
            newBlock = blockChain.get(i);
            String testIndex = Integer.toString(newBlock.index);
            String testTimestamp = Long.toString(newBlock.timeStamp);
            String testPre = newBlock.previousHash;
            String testData = newBlock.data;
            //index verification
            if(previousBlock.index!=newBlock.index-1){
                System.out.println("Index error!");
                return false;
            }
            //Hash linkage verification
            if(!(previousBlock.hash).equals(newBlock.previousHash)){
                System.out.println("Hash linkage verification fails!");
                System.out.println(newBlock.index);
                return false;
            }

            //loop thru blockchains regular transactions:
//            TxOut tempOutput;
//            for(int t=0; t <newBlock.transactions.size(); t++) {
//                Transaction currentTransaction = newBlock.transactions.get(t);
//                if(!currentTransaction.isCoinbaseTx){
//                    if(!currentTransaction.verifySignature()) {
//                        System.out.println("#Signature on Transaction(" + t + ") is Invalid");
//                        return false;
//                    }
//                    if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
//                        System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
//                        return false;
//                    }
//
//                    for(TxIn input: currentTransaction.inputs) {
//                        tempOutput = tempUTXOs.get(input.transactionOutputId);
//
//                        if(tempOutput == null) {
//                            System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
//                            return false;
//                        }
//
//                        if(input.UTXO.value != tempOutput.value) {
//                            System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
//                            return false;
//                        }
//
//                        tempUTXOs.remove(input.transactionOutputId);
//                    }
//
//                    for(TxOut output: currentTransaction.outputs) {
//                        tempUTXOs.put(output.id, output);
//                    }
//
//                    if( currentTransaction.outputs.get(0).recipient != currentTransaction.recipient) {
//                        System.out.println("#Transaction(" + t + ") output recipient is not who it should be");
//                        return false;
//                    }
//                    if( currentTransaction.outputs.get(1).recipient != currentTransaction.sender) {
//                        System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
//                        return false;
//                    }
//                }
//
//
//
//            }
        }
        return true;
    }





}
