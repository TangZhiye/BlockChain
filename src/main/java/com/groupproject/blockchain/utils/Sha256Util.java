package com.groupproject.blockchain.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

// Algo SHA-256
public class Sha256Util{
    public static String applySha256(String input){  //调用sha256算法
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                hexString.append(hex);
            }
            return hexString.toString();
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
