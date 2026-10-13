package ai.foresignal.mqtt;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * The trust of {@code mqtts://} and {@code wss://} connections (prompt 060): the JVM's system trust store,
 * plus the CA certificates of the optional {@code caCertPem} secret. Hostname verification is never turned
 * off: the HiveMQ client sets the {@code HTTPS} endpoint identification algorithm whenever no custom
 * verifier is configured, and Foresignal configures none.
 */
public final class MqttTls {

    private MqttTls() {}

    /**
     * The trust managers for {@code caCertPem}: null when it is blank (the client's default, the system
     * trust store), else a factory over the system anchors plus those certificates.
     *
     * @throws IllegalArgumentException when the PEM holds no readable certificate
     */
    public static TrustManagerFactory trustManagers(String caCertPem) {
        if (caCertPem == null || caCertPem.isBlank()) {
            return null;
        }
        List<X509Certificate> extra = certificates(caCertPem);
        try {
            KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
            store.load(null, null);
            int i = 0;
            for (X509Certificate anchor : systemAnchors()) {
                store.setCertificateEntry("system-" + i++, anchor);
            }
            int j = 0;
            for (X509Certificate certificate : extra) {
                store.setCertificateEntry("ca-" + j++, certificate);
            }
            TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            factory.init(store);
            return factory;
        } catch (GeneralSecurityException | java.io.IOException e) {
            throw new IllegalStateException("could not build the MQTT trust store", e);
        }
    }

    /**
     * The certificates of a PEM text.
     *
     * @throws IllegalArgumentException when it holds none or cannot be read
     */
    public static List<X509Certificate> certificates(String pem) {
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            Collection<? extends Certificate> read = factory.generateCertificates(
                    new ByteArrayInputStream(pem.strip().getBytes(StandardCharsets.US_ASCII)));
            List<X509Certificate> out = new ArrayList<>();
            for (Certificate c : read) {
                if (c instanceof X509Certificate x) {
                    out.add(x);
                }
            }
            if (out.isEmpty()) {
                throw new IllegalArgumentException("holds no X.509 certificate");
            }
            return out;
        } catch (CertificateException e) {
            throw new IllegalArgumentException("is not a PEM certificate: " + e.getMessage());
        }
    }

    /** Why {@code pem} cannot be used, or null when it can (validation). */
    public static String problem(String pem) {
        if (pem == null || pem.isBlank()) {
            return null;
        }
        try {
            certificates(pem);
            return null;
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private static List<X509Certificate> systemAnchors() throws GeneralSecurityException {
        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init((KeyStore) null);
        List<X509Certificate> out = new ArrayList<>();
        for (TrustManager manager : factory.getTrustManagers()) {
            if (manager instanceof X509TrustManager x509) {
                out.addAll(List.of(x509.getAcceptedIssuers()));
            }
        }
        return out;
    }
}
