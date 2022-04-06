package com.groupproject.blockchain.utils;

import org.bouncycastle.util.encoders.Base64;

import java.security.*;
import java.security.spec.X509EncodedKeySpec;

public class RSAUtils {

    /**
     * 生成签名,Java版本
     *
     * @param algorithm  : 算法
     * @param privateKey : 私钥
     * @param data       : 原文
     * @return : 签名
     */
    public static byte[] getSignature(String algorithm, PrivateKey privateKey, String data) {
        try {
            // 获取签名对象
            Signature signature = Signature.getInstance(algorithm);
            // 传入私钥
            signature.initSign(privateKey);
            // 传入原文
            signature.update(data.getBytes());
            // 签名
            return signature.sign();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 校验签名,Java版本
     *
     * @param algorithm     : 算法
     * @param publicKey     : 公钥
     * @param data          : 原文
     * @param signatureData : 签名
     * @return : 签名是否正确
     */
    public static boolean verifySignature(String algorithm, String publicKey, String data, byte[] signatureData) {
        try {
            // 获取签名对象
            Signature signature = Signature.getInstance(algorithm);
            PublicKey publicKeyObject = getKeyFromString(publicKey);

            // 传入公钥
            signature.initVerify(publicKeyObject);

            //传入原文
            signature.update(data.getBytes());
            // 校验签名
            return signature.verify(signatureData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public static String getStringFromKey(Key key) {
        return java.util.Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static PublicKey getKeyFromString(String key){
        try{
            byte[] byteKey = Base64.decode(key.getBytes());
            X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
            KeyFactory kf = KeyFactory.getInstance("ECDSA");

            return kf.generatePublic(X509publicKey);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }


}
