package com.github.upperbound.secret_santa.config;

import com.github.upperbound.secret_santa.util.CryptoException;
import com.github.upperbound.secret_santa.util.SimpleCryptoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.server.Ssl;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;
import java.security.Key;
import java.security.cert.Certificate;
import java.security.interfaces.DSAKey;
import java.security.interfaces.ECKey;
import java.security.interfaces.RSAKey;
import java.util.Arrays;

/**
 * <p> Custom configuration of SSL context for tomcat server </p>
 * @author Vladislav Tsukanov
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class TomcatConfig implements TomcatConnectorCustomizer {
    private final ServerProperties serverProperties;

    @Override
    public void customize(Connector connector) {
        if (!connector.getSecure() || serverProperties.getSsl() == null || !serverProperties.getSsl().isEnabled())
            return;
        SSLHostConfig sslHostConfig = Arrays.stream(connector.getProtocolHandler().findSslHostConfigs())
                .findFirst()
                .orElseGet(() -> {
                    SSLHostConfig conf = new SSLHostConfig();
                    connector.getProtocolHandler().addSslHostConfig(conf, true);
                    return conf;
                });
        sslHostConfig.getCertificates().clear();
        Ssl ssl = serverProperties.getSsl();
        sslHostConfig.setEnabledProtocols(ssl.getEnabledProtocols());
        if (ssl.getCiphers() != null) {
            sslHostConfig.setCiphers(String.join(",", ssl.getCiphers()));
        }
        if (serverProperties.getAddress() != null) {
            sslHostConfig.setHostName(serverProperties.getAddress().getHostName());
        } else {
            sslHostConfig.setHostName("localhost");
        }
        SSLHostConfigCertificate certificate = new SSLHostConfigCertificate(sslHostConfig, getCertificateType());
        if (ssl.getKeyStore() != null) {
            certificate.setCertificateKeystoreFile(ssl.getKeyStore());
        }
        if (ssl.getKeyStorePassword() != null) {
            certificate.setCertificateKeystorePassword(ssl.getKeyStorePassword());
        }
        if (ssl.getKeyAlias() != null) {
            certificate.setCertificateKeyAlias(ssl.getKeyAlias());
        }
        if (ssl.getKeyPassword() != null) {
            certificate.setCertificateKeyPassword(ssl.getKeyPassword());
        }
        if (ssl.getCertificatePrivateKey() != null) {
            certificate.setCertificateKeyFile(ssl.getCertificatePrivateKey());
        }
        if (ssl.getCertificate() != null) {
            certificate.setCertificateFile(ssl.getCertificate());
        }
        sslHostConfig.addCertificate(certificate);
    }

    private SSLHostConfigCertificate.Type getCertificateType() {
        if (serverProperties.getSsl() == null)
            return SSLHostConfigCertificate.Type.UNDEFINED;
        String resourcePath;
        Ssl ssl = serverProperties.getSsl();
        resourcePath = ssl.getKeyStore();
        if (resourcePath != null) {
            try {
                ResourceUtils.getFile(resourcePath);
                Key key = SimpleCryptoUtil.getKeyByAlias(
                        resourcePath,
                        ssl.getKeyStorePassword(),
                        ssl.getKeyAlias(),
                        ssl.getKeyPassword()
                );
                return getCertificateType(key);
            } catch (FileNotFoundException e) {
                log.warn("'{}' keystore is not found: {}", resourcePath, e.getMessage());
            } catch (CryptoException e) {
                log.warn("unable to retrieve '{}' key from '{}' keystore: {}", ssl.getKeyAlias(), resourcePath, e.getMessage());
            }
        }
        resourcePath = ssl.getCertificatePrivateKey();
        if (resourcePath != null) {
            try {
                ResourceUtils.getFile(resourcePath);
                Key key = SimpleCryptoUtil.getKeyByPath(resourcePath);
                return getCertificateType(key);
            } catch (FileNotFoundException e) {
                log.warn("'{}' key file is not found: {}", resourcePath, e.getMessage());
            } catch (CryptoException e) {
                log.warn("unable to retrieve '{}' key: {}", resourcePath, e.getMessage());
            }
        }
        resourcePath = ssl.getCertificate();
        if (resourcePath != null) {
            try {
                ResourceUtils.getFile(resourcePath);
                Certificate certificate = SimpleCryptoUtil.getCertByPath(resourcePath);
                return getCertificateType(certificate.getPublicKey());
            } catch (FileNotFoundException e) {
                log.warn("'{}' certificate file is not found: {}", resourcePath, e.getMessage());
            } catch (CryptoException e) {
                log.warn("unable to retrieve '{}' certificate: {}", resourcePath, e.getMessage());
            }
        }
        return SSLHostConfigCertificate.Type.UNDEFINED;
    }

    private SSLHostConfigCertificate.Type getCertificateType(Key key) {
        return switch (key) {
            case DSAKey ignored -> SSLHostConfigCertificate.Type.DSA;
            case RSAKey ignored -> SSLHostConfigCertificate.Type.RSA;
            case ECKey ignored -> SSLHostConfigCertificate.Type.EC;
            default -> SSLHostConfigCertificate.Type.UNDEFINED;
        };
    }
}
