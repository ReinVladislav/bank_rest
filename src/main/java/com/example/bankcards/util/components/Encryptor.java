package com.example.bankcards.util.components;

import com.example.bankcards.exception.InvalidCardNumberException;
import com.example.bankcards.util.constants.ErrorMessages;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class Encryptor {
    private static final String ALGORITHM = "AES";
    private final SecretKeySpec secretKey;

    public Encryptor(@Value("${card-number.encrypt.key}") String key) {
        this.secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
    }

    public String encrypt(String cardNumber) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(cardNumber.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting card number", e);
        }
    }

    public String decrypt(String encryptedCardNumber) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decoded = Base64.getDecoder().decode(encryptedCardNumber);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting card number", e);
        }
    }

}
