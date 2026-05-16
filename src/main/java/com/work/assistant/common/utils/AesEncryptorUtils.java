package com.work.assistant.common.utils;


import io.micrometer.common.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class AesEncryptorUtils {
    private static final String PASSWORD = "6c96439b-ad24-4428-81ff-184d386afe28";

    private static final String KEY_ALGORITHM = "AES";

    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";

    public static String encrypt(String content) {

        if (StringUtils.isBlank(content)) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            byte[] byteContent = content.getBytes(UTF_8);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
            byte[] result = cipher.doFinal(byteContent);
            return Base64.getEncoder().encodeToString(result);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String content) {

        if (StringUtils.isBlank(content)) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey());
            byte[] result = cipher.doFinal(Base64.getDecoder().decode(content));
            return new String(result, UTF_8);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static SecretKeySpec getSecretKey() {

        KeyGenerator kg;
        try {
            kg = KeyGenerator.getInstance(KEY_ALGORITHM);
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(PASSWORD.getBytes());
            kg.init(128, random);
            SecretKey secretKey = kg.generateKey();
            // 转换为AES专用密钥
            return new SecretKeySpec(secretKey.getEncoded(), KEY_ALGORITHM);
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }

        return null;
    }
}
