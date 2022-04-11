package com.groupproject.blockchain.bean;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

import static com.groupproject.blockchain.bean.PoW.getDifficulty;

public class BlockChain {
    //store block in arraylist
    public static ArrayList<Block> blockChain = new ArrayList<Block>();
    public static HashMap<String, TxOut> UTXOs = new HashMap<String, TxOut>();

    public static float minimumTransaction = 0.1f;

    //Config for the Blockchain
    public static final int blockGenerationInterval = 10; // we expect that evey 10 seconds we find a block
    public static final int  diffAdjustInterval= 1; // defines how often the difficulty should be adjusted with the increasing or decreasing network hashrate.

    public static void main(String[] args) {
        //Setup Bouncy castle as a Security Provider
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        //Create wallets:
        Wallet walletA = new Wallet();
        Wallet walletB = new Wallet();
        //generate genesis block
        Block genesisBlock = new Block("0", 0, 1);
        genesisBlock.addCoinbaseTx(walletA);
        genesisBlock.mineBlock();
        blockChain.add(genesisBlock);

        //testing
        System.out.println("\n1 WalletA's balance is: " + walletA.getBalance() + ",WalletB's balance is: " + walletB.getBalance()); //50,0
        System.out.println("\n2 WalletA is Attempting to send funds (20 & 10) to WalletB...");
        Block block1 = generateNextBlock();
        block1.addCoinbaseTx(walletA);
        System.out.println("\n3 WalletA's balance is: " + walletA.getBalance() + ",WalletB's balance is: " + walletB.getBalance()); //100,0
        block1.addTransaction(walletA.sendFunds(walletB.publicKey, 20f));
        System.out.println("\n4 WalletA's balance is: " + walletA.getBalance() + ",WalletB's balance is: " + walletB.getBalance()); //80,20
        block1.addTransaction(walletA.sendFunds(walletB.publicKey, 10f));
        System.out.println("\n5 WalletA's balance is: " + walletA.getBalance() + ",WalletB's balance is: " + walletB.getBalance()); //70,30
        block1.mineBlock();
        blockChain.add(block1);
        System.out.println("\n6 WalletA's balance is: " + walletA.getBalance() + ",WalletB's balance is: " + walletB.getBalance()); //70,30
//        System.out.println("WalletB's balance is: " + walletB.getBalance()); //30

        System.out.println("\nWalletA Attempting to send more funds (1000) than it has...");
        Block block2 = generateNextBlock();
        block2.addCoinbaseTx(walletA);
        System.out.println("\n7 WalletA's balance is: " + walletA.getBalance() + ",WalletB's balance is: " + walletB.getBalance()); //120,30
        block2.addTransaction(walletA.sendFunds(walletB.publicKey, 1000f));
        System.out.println("\n8 WalletA's balance is: " + walletA.getBalance() + ",WalletB's balance is: " + walletB.getBalance()); //120,30
        block2.mineBlock();
        blockChain.add(block2);
        System.out.println("\n9 WalletA's balance is: " + walletA.getBalance() + ",WalletB's balance is: " + walletB.getBalance()); //120,30

        System.out.println("\nWalletB is Attempting to send funds (20) to WalletA...");
        Block block3 = generateNextBlock();
        block3.addTransaction(walletB.sendFunds( walletA.publicKey, 20)); //fail cuz no coinbase Tx
        block3.mineBlock();
        blockChain.add(block3);
        System.out.println("\n10 WalletA's balance is: " + walletA.getBalance() + ",WalletB's balance is: " + walletB.getBalance()); //120,30

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

        //generate the new difficulty for the block -> store the difficulty in the block

        int difficulty;
        if (nextIndex > 2){
            Block prePreviousBlock = blockChain.get(blockChain.size()-2);
            difficulty = getDifficulty(prePreviousBlock.timeStamp,previousBlock.timeStamp,previousBlock.difficulty,previousBlock.index,blockGenerationInterval,diffAdjustInterval);
        } else{
            difficulty = 1;

        };

        //Here: you have to set the difficulty based on the blocks
        Block newBlock = new Block(previousHash, nextIndex, difficulty);
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
