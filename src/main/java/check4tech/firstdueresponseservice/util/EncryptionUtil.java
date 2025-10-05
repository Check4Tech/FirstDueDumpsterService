package check4tech.firstdueresponseservice.util;

import org.springframework.beans.factory.annotation.Value;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.stereotype.Component;

@Component
public class EncryptionUtil {

    private final StandardPBEStringEncryptor encryptor;

    public EncryptionUtil(@Value("${jasypt.encryptor.password}") String secret) {
        encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(secret);
    }

    public String encrypt(String input) {
        return input == null ? null : encryptor.encrypt(input);
    }

    public String decrypt(String input) {
        return input == null ? null : encryptor.decrypt(input);
    }
}
