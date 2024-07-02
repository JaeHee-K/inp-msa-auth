package com.inp.msa.inpmsaauth.config;

import lombok.RequiredArgsConstructor;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@RequiredArgsConstructor
@Configuration
@PropertySource("classpath:application-key.yml")
public class JasyptConfig {

    @Value("${ENCRYPT_KEY}")
    private String KEY;
    @Value("${ENCRYPT_ALGO}")
    private String ALGO;

    @Bean("jasyptStringEncryptor")
    public StringEncryptor jasyptStringEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(KEY);
        config.setAlgorithm(ALGO);
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setStringOutputType("base64");
        config.setIvGeneratorClassName("org.jasypt.iv.NoIvGenerator");
        encryptor.setConfig(config);
        return encryptor;
    }
}
