package com.github.upperbound.secret_santa.model.converters;

import com.github.upperbound.secret_santa.util.CryptoException;
import com.github.upperbound.secret_santa.util.SimpleCryptoUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * <p> Used to encrypt/decrypt string data before store/get it from datasource. </p>
 * @author Vladislav Tsukanov
 * @see SimpleCryptoUtil
 */
@Component
@Converter
public class StringCryptoConverter implements AttributeConverter<String, String>  {
    private static SimpleCryptoUtil cryptoConverter;

    @Autowired
    public void init(SimpleCryptoUtil cryptoConverter) {
        StringCryptoConverter.cryptoConverter = cryptoConverter;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isEmpty())
            return attribute;
        try {
            return Base64.getEncoder().encodeToString(cryptoConverter.encrypt(attribute.getBytes(StandardCharsets.UTF_8)));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new CryptoException(e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty())
            return dbData;
        try {
            return new String(cryptoConverter.decrypt(Base64.getDecoder().decode(dbData)), StandardCharsets.UTF_8);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new CryptoException(e);
        }
    }
}
