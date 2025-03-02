package com.github.upperbound.secret_santa.util;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaMiscPEMGenerator;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.io.pem.PemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HexFormat;

/**
 * <p> Simple crypto util that uses symmetric key to encode/decode any data. </p>
 * @author Vladislav Tsukanov
 */
@Slf4j
@Component
public class SimpleCryptoUtil {
    private static final String pepper = "\u01A4\u00CB\u0158\u00C1\u015C\u0162\u0158\u0104" +
            "\u00C1\u0110\u014C\u016C\u0158\u014C\u0181\u014C\u0158\u014C\u015A";
    private final ThreadLocal<Cipher> encryptor;
    private final ThreadLocal<Cipher> decryptor;

    public SimpleCryptoUtil(@Value("${app.crypto.ks-location}") String ksLocation,
                            @Value("${app.crypto.ks-password}") String ksPassword,
                            @Value("${app.crypto.ks-key-alias}") String ksKeyAlias,
                            @Value("${app.crypto.ks-key-password}") String ksKeyPassword,
                            @Value("${app.crypto.key-salt}") String salt)
            throws CryptoException
    {
        SecretKey key;
        if (ksPassword == null || ("default".equals(ksPassword) && !"any_value".equals(salt))) {
            String defaultAlgorithm = "AES";
            log.warn(
                    "SIMPLE '{}' KEY WITH 'app.crypto.key-salt' param IS BEING USED",
                    defaultAlgorithm
            );
            key = new SecretKeySpec(
                    Arrays.copyOf(
                            HexFormat.of().formatHex(
                                    ((salt == null ? "" : salt) + pepper).getBytes(StandardCharsets.UTF_8)
                            ).getBytes(),
                            32
                    ),
                    defaultAlgorithm
            );
        } else {
            key = getKeyByAlias(ksLocation, ksPassword, ksKeyAlias, ksKeyPassword);
            if ("default".equals(ksPassword)) {
                log.warn(
                        "DEFAULT '{}' KEY IS BEING USED",
                        key.getAlgorithm()
                );
            }
        }
        encryptor = ThreadLocal.withInitial(() -> {
            try {
                Cipher c = Cipher.getInstance(key.getAlgorithm());
                c.init(Cipher.ENCRYPT_MODE, key);
                return c;
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
                throw new CryptoException(e);
            }
        });
        decryptor = ThreadLocal.withInitial(() -> {
            try {
                Cipher c = Cipher.getInstance(key.getAlgorithm());
                c.init(Cipher.DECRYPT_MODE, key);
                return c;
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
                throw new CryptoException(e);
            }
        });
        encryptor.get();
        decryptor.get();
    }

    public byte[] encrypt(byte[] value) throws IllegalBlockSizeException, BadPaddingException {
        return encryptor.get().doFinal(value);
    }

    public byte[] decrypt(byte[] value) throws IllegalBlockSizeException, BadPaddingException {
        return decryptor.get().doFinal(value);
    }

    @SuppressWarnings("unchecked")
    public static <K extends java.security.Key> K getKeyByAlias(String keystoreLocation,
                                                                String keystorePassword,
                                                                String keyAlias,
                                                                String keyPassword)
            throws CryptoException
    {
        Object object = getObjectByAlias(keystoreLocation, keystorePassword, keyAlias, keyPassword, false);
        if (object != null && !(object instanceof Key)) {
            throw new CryptoException(object.getClass() + " cannot be cast to Key");
        }
        return (K) object;
    }

    @SuppressWarnings("unchecked")
    public static <K extends java.security.Key> K getKeyByPath(String keyPath)
            throws CryptoException
    {
        Object object = getObjectByPath(keyPath);
        if (object != null && !(object instanceof Key)) {
            throw new CryptoException(object.getClass() + " cannot be cast to Key");
        }
        return (K) object;
    }

    @SuppressWarnings("unchecked")
    public static <C extends java.security.cert.Certificate> C getCertByAlias(String keystoreLocation,
                                                                              String keystorePassword,
                                                                              String certAlias)
            throws CryptoException
    {
        Object object = getObjectByAlias(keystoreLocation, keystorePassword, certAlias, null, true);
        if (object != null && !(object instanceof Certificate)) {
            throw new CryptoException(object.getClass() + " cannot be cast to Certificate");
        }
        return (C) object;
    }

    @SuppressWarnings("unchecked")
    public static <C extends java.security.cert.Certificate> C getCertByPath(String certPath)
            throws CryptoException
    {
        Object object = getObjectByPath(certPath);
        if (object != null && !(object instanceof Certificate)) {
            throw new CryptoException(object.getClass() + " cannot be cast to Certificate");
        }
        return (C) object;
    }

    private static Object getObjectByAlias(String keystoreLocation,
                                           String keystorePassword,
                                           String objectAlias,
                                           String objectPassword,
                                           boolean isCert)
            throws CryptoException
    {
        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance("BCFKS", StaticContext.getBCProvider());
            keyStore.load(Files.newInputStream(
                    ResourceUtils.getFile(keystoreLocation).toPath()),
                    keystorePassword.toCharArray()
            );
        } catch (Exception e) {
            try {
                keyStore = KeyStore.getInstance("PKCS12", StaticContext.getBCProvider());
                keyStore.load(Files.newInputStream(
                        ResourceUtils.getFile(keystoreLocation).toPath()),
                        keystorePassword.toCharArray()
                );
            } catch (Exception ex) {
                throw new CryptoException(ex.getMessage(), ex);
            }
        }
        try {
            return isCert ?
                    keyStore.getCertificate(objectAlias) :
                    keyStore.getKey(objectAlias, objectPassword.toCharArray());
        } catch (Exception e) {
            throw new CryptoException(e.getMessage(), e);
        }
    }

    private static Object getObjectByPath(String path)
            throws CryptoException
    {
        try(PEMParser pemParser = new PEMParser(Files.newBufferedReader(ResourceUtils.getFile(path).toPath()))) {
            Object pemObject = pemParser.readObject();
            switch (pemObject) {
                case PEMKeyPair keyPair -> {
                    return getPrivateKey(keyPair.getPrivateKeyInfo());
                }
                case PrivateKeyInfo privateKeyInfo -> {
                    return getPrivateKey(privateKeyInfo);
                }
                case SubjectPublicKeyInfo publicKeyInfo -> {
                    return getPublicKey(publicKeyInfo);
                }
                case X509CertificateHolder certificateHolder -> {
                    return new JcaX509CertificateConverter()
                            .setProvider(StaticContext.getBCProvider())
                            .getCertificate(certificateHolder);
                }
                default -> throw new CryptoException("unexpected object type: " + pemObject.getClass());
            }
        } catch (CryptoException e) {
            throw e;
        } catch (Exception e) {
            throw new CryptoException(e.getMessage(), e);
        }
    }

    private static PrivateKey getPrivateKey(PrivateKeyInfo privateKeyInfo) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance(
                privateKeyInfo.getPrivateKeyAlgorithm().getAlgorithm().toString(),
                StaticContext.getBCProvider()
        );
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyInfo.getEncoded()));
    }

    private static PublicKey getPublicKey(SubjectPublicKeyInfo publicKeyInfo) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance(
                publicKeyInfo.getAlgorithm().getAlgorithm().toString(),
                StaticContext.getBCProvider()
        );
        return keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyInfo.getEncoded()));
    }

    private static void generateAndSaveSecret(String algorithm,
                                              int keySize,
                                              Path keystorePath,
                                              String keystorePassword,
                                              String keyAlias,
                                              String keyPassword)
            throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException
    {
        KeyGenerator generator = KeyGenerator.getInstance(algorithm, StaticContext.getBCProvider());
        generator.init(keySize);
        SecretKey secretKey = generator.generateKey();
        KeyStore keyStore = KeyStore.getInstance("BCFKS", StaticContext.getBCProvider());
        keyStore.load(!keystorePath.toFile().exists() ? null : Files.newInputStream(keystorePath), keystorePassword.toCharArray());
        keyStore.setEntry(keyAlias, new KeyStore.SecretKeyEntry(secretKey), new KeyStore.PasswordProtection(keyPassword.toCharArray()));
        keyStore.store(Files.newOutputStream(keystorePath), keystorePassword.toCharArray());
    }

    private static Certificate generateCertificate(PrivateKey privateKey,
                                                   PublicKey publicKey,
                                                   String signatureAlgorithm,
                                                   String cn,
                                                   Instant notBefore,
                                                   Instant notAfter)
            throws Exception
    {
        X500Name x500Name = new X500Name("CN=" + cn);
        SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(publicKey.getEncoded());
        DigestCalculator digCalc = new BcDigestCalculatorProvider()
                .get(new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1));
        org.bouncycastle.cert.X509v3CertificateBuilder certBuilder =
                new JcaX509v3CertificateBuilder(
                        x500Name,
                        BigInteger.valueOf(notBefore.toEpochMilli()),
                        Date.from(notBefore),
                        Date.from(notAfter),
                        x500Name,
                        publicKey
                );
        certBuilder.addExtension(
                        Extension.subjectKeyIdentifier,
                        false,
                        new JcaX509ExtensionUtils(digCalc).createSubjectKeyIdentifier(publicKeyInfo)
                )
                .addExtension(
                        Extension.authorityKeyIdentifier,
                        false,
                        new JcaX509ExtensionUtils(digCalc).createAuthorityKeyIdentifier(publicKeyInfo)
                )
                .addExtension(
                        Extension.basicConstraints,
                        true,
                        new BasicConstraints(true)
                );

        return new JcaX509CertificateConverter()
                .setProvider(StaticContext.getBCProvider())
                .getCertificate(
                        certBuilder.build(
                                new JcaContentSignerBuilder(signatureAlgorithm)
                                        .build(privateKey)
                        )
                );
    }

    private static void savePemObject(Path path, Object object) throws Exception {
        try (PemWriter pemWriter = new PemWriter(Files.newBufferedWriter(path))) {
            pemWriter.writeObject(new JcaMiscPEMGenerator(object));
        }
    }

    private static void generateAndSaveRSA(int keySize,
                                           Path privateKeyPath,
                                           Path publicKeyPath,
                                           Path certPath,
                                           String signatureAlgorithm,
                                           String cn,
                                           Instant notBefore,
                                           Instant notAfter)
            throws Exception
    {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", StaticContext.getBCProvider());
        generator.initialize(keySize);
        KeyPair keyPair = generator.generateKeyPair();
        Certificate certificate = generateCertificate(
                keyPair.getPrivate(),
                keyPair.getPublic(),
                signatureAlgorithm,
                cn,
                notBefore,
                notAfter
        );
        savePemObject(privateKeyPath, keyPair.getPrivate());
        savePemObject(publicKeyPath, keyPair.getPublic());
        savePemObject(certPath, certificate);
    }

    private static void generateAndSaveEC(String curve,
                                          Path privateKeyPath,
                                          Path publicKeyPath,
                                          Path certPath,
                                          String signatureAlgorithm,
                                          String cn,
                                          Instant notBefore,
                                          Instant notAfter)
            throws Exception
    {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC", StaticContext.getBCProvider());
        ECGenParameterSpec m = new ECGenParameterSpec(curve);
        generator.initialize(m);
        KeyPair keyPair = generator.generateKeyPair();
        Certificate certificate = generateCertificate(
                keyPair.getPrivate(),
                keyPair.getPublic(),
                signatureAlgorithm,
                cn,
                notBefore,
                notAfter
        );
        savePemObject(privateKeyPath, keyPair.getPrivate());
        savePemObject(publicKeyPath, keyPair.getPublic());
        savePemObject(certPath, certificate);
    }

    public static void main(String[] args) throws Exception {
//        generateAndSaveSecret(
//                "AES",
//                256,
//                Path.of("keystore-secret.jks"),
//                "default",
//                "key-secret",
//                "default"
//        );
//        generateAndSaveRSA(
//                2048,
//                Path.of("private-rsa-key.pem"),
//                Path.of("public-rsa-key.pem"),
//                Path.of("certificate-rsa.crt"),
//                "SHA256withRSA",
//                "github.com/upperbound",
//                Instant.parse("2024-01-01T00:00:00.00Z"),
//                Instant.parse("2042-12-31T23:59:59.99Z")
//        );
//        generateAndSaveEC(
//                "secp256r1",
//                Path.of("private-ec-key.pem"),
//                Path.of("public-ec-key.pem"),
//                Path.of("certificate-ec.crt"),
//                "SHA256withECDSA",
//                "github.com/upperbound",
//                Instant.parse("2024-01-01T00:00:00.00Z"),
//                Instant.parse("2042-12-31T23:59:59.99Z")
//        );
    }
}
