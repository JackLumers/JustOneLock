package janeelsmur.justonelock.utilities;

import android.util.Log;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 *
 * Класс с алгоритмами шифрования
 *
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 **/

public class EncryptionAlgorithms {

    private static final String LOG_TAG = "Encryption Logs";

    private static byte[] AESDecrypter(byte[] encryptedBytes, byte[] key) throws
            BadPaddingException, InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        byte[] initializationVector = initVectorGenerator(initVectorGenerator(key));

        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(initializationVector));
        return cipher.doFinal(encryptedBytes);
    }

    private static byte[] AESEncrypter(byte[] bytesToEncrypt, byte[] key) throws
            BadPaddingException, NoSuchPaddingException, IllegalBlockSizeException,
            InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec k = new SecretKeySpec(key, "AES");
        byte[] initializationVector = initVectorGenerator(initVectorGenerator(key));

        cipher.init(Cipher.ENCRYPT_MODE, k, new IvParameterSpec(initializationVector));
        return cipher.doFinal(bytesToEncrypt);
    }

    public static String DecryptInString(byte[] encryptedBytes, byte[] key) {
        try {
            return new String(AESDecrypter(encryptedBytes, key));
        } catch (Exception c) {
            Log.d(LOG_TAG, Log.getStackTraceString(c));
            return null;
        }
    }

    public static byte[] Encrypt(byte[] bytesToEncrypt, byte[] key) {
        try {
            return AESEncrypter(bytesToEncrypt, key);
        } catch (Exception e) {
            Log.d(LOG_TAG, Log.getStackTraceString(e));
            return null;
        }
    }

    public static byte[] SHA256(String string) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(string.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            Log.d(LOG_TAG, "SHA256: " + Log.getStackTraceString(e));
            return null;
        }
    }

    /** Генератор вектора инициализации. В релизной версии генерацию нужно изменить **/
    private static byte[] initVectorGenerator(byte[] key) {
        byte[] initializationVector = new byte[16];
        short i = 0, j = 0;
        while (i < initializationVector.length) {
            initializationVector[i] = key[j];
            j++;
            i++;
            if (j == key.length) j = 0;
        }
        return initializationVector;
    }
}
