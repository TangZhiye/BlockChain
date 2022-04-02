package com.groupproject.blockchain.Tools;

import java.io.Serializable;

public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    private int tid;
    private String outPurAddress;
    public Transaction(){

    }
    public Transaction(int tid, String outPurAddress) {
        this.tid = tid;
        this.outPurAddress = outPurAddress;
    }

    public int getTid() {
        return tid;
    }

    public void setTid(int tid) {
        this.tid = tid;
    }

    public String getOutPurAddress() {
        return outPurAddress;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "tid=" + tid +
                ", outPurAddress='" + outPurAddress + '\'' +
                '}';
    }

    public void setOutPurAddress(String outPurAddress) {
        this.outPurAddress = outPurAddress;
    }
}
