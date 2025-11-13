package com.example.bankcards.util.components;

import com.example.bankcards.exception.InvalidCardNumberException;
import com.example.bankcards.util.constants.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class CardNumberUtil {

    private final RandomNumberGenerator randomNumberGenerator;
    private final Encryptor encryptor;

    public String getRandomEncryptedCardNumber() {
        return encryptor.encrypt(generateCodeNumber());
    }

    public String decryptAndFormat(String encryptedCardNumber) {
        return format(
                encryptor.decrypt(encryptedCardNumber)
        );
    }

    public String getCardNumberWithMask(String encryptedCardNumber) {
        String cardNumber = encryptor.decrypt(encryptedCardNumber);
        return cardNumber.replaceFirst("^.{12}", "**** **** **** ");
    }

    private String format(String cardNumber) {
        if (!cardNumberIsCorrect(cardNumber)) {
            throw new InvalidCardNumberException(ErrorMessages.INVALID_CARD_NUMBER);
        }
        StringBuilder sb = new StringBuilder();
        //разделяем пробелом каждые 4 символа карты
        for (int i = 0; i < cardNumber.length(); i += 4) {
            int end = Math.min(i + 4, cardNumber.length());
            sb.append(cardNumber, i, end);
            if (end < cardNumber.length()) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private String generateCodeNumber(){
        return randomNumberGenerator.generateNumericString(16);//16 - кол-во цифр в номере карты
    }

    private boolean cardNumberIsCorrect(String cardNumber) {
        if (cardNumber == null) {
            return false;
        }
        String digits = cardNumber.replaceAll("\\D", "");
        return digits.length() == 16;
    }

}
