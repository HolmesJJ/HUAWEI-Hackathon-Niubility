package edu.nyp.deafapp.Utils;

import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public class RSAUtils {

    private static final String KEY_ALGORITHM = "RSA";
    private static final String CIPHER_TYPE = "RSA/ECB/PKCS1Padding"; // Android端加密算法
    private static final int MAX_ENCRYPT_BLOCK = 117; // RSA最大加密明文大小

    public static String encryptByPublicKey(String data, String pubKey) throws Exception {

        // 对密钥解密
        byte[] keyBytes = decryptBASE64(pubKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PublicKey publicKey = keyFactory.generatePublic(spec);

        // 对数据加密(分段)
        Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] dataByteArray = data.getBytes();
        int inputLen = dataByteArray.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(dataByteArray, offSet, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(dataByteArray, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();

        return encryptBASE64(encryptedData);
    }

    private static byte[] decryptBASE64(String key) {
        return Base64.decode(key, Base64.NO_WRAP);
    }

    private static String encryptBASE64(byte[] dataBytes) {
        return Base64.encodeToString(dataBytes, Base64.NO_WRAP);
    }
}
