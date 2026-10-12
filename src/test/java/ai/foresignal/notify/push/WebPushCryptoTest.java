package ai.foresignal.notify.push;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.HexFormat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * RFC 8291 encryption, RFC 5869 HKDF and RFC 8292 VAPID against the RFCs' published values
 * (prompt 031). Every key here is the RFCs' published example key or generated at test time.
 */
class WebPushCryptoTest {

    // RFC 8291 §5 and Appendix A.
    static final String PLAINTEXT = "When I grow up, I want to be a watermelon";
    static final String AS_PUBLIC = "BP4z9KsN6nGRTbVYI_c7VJSPQTBtkgcy27mlmlMoZIIgDll6e3vCYLocInmYWAmS6TlzAC8wEqKK6PBru3jl7A8";
    static final String AS_PRIVATE = "yfWPiYE-n46HLnH0KqZOF1fJJU3MYrct3AELtAQ-oRw";
    static final String UA_PUBLIC = "BCVxsr7N_eNgVRqvHtD0zTZsEc6-VV-JvLexhqUzORcxaOzi6-AYWXvTBHm4bjyPjs7Vd8pZGH6SRpkNtoIAiw4";
    static final String UA_PRIVATE = "q1dXpw3UpT5VOmu_cf_v6ih07Aems3njxI-JWgLcM94";
    static final String SALT = "DGv6ra1nlYgDCS1FRnbzlw";
    static final String AUTH = "BTBZMqHH6r4Tts7J_aSIgg";
    static final String BODY = "DGv6ra1nlYgDCS1FRnbzlwAAEABBBP4z9KsN6nGRTbVYI_c7VJSPQTBtkgcy27ml"
            + "mlMoZIIgDll6e3vCYLocInmYWAmS6TlzAC8wEqKK6PBru3jl7A_yl95bQpu6cVPT"
            + "pK4Mqgkf1CXztLVBSt2Ks3oZwbuwXPXLWyouBWLVWGNWQexSgSxsj_Qulcy4a-fN";

    static byte[] hex(String text) {
        return HexFormat.of().parseHex(text.replaceAll("\\s", ""));
    }

    static KeyPair rfcServerKeys() {
        return new KeyPair(EcKeys.decodePoint(EcKeys.unb64(AS_PUBLIC)), EcKeys.privateFromScalar(EcKeys.unb64(AS_PRIVATE)));
    }

    @Test
    @DisplayName("RFC 8291 Appendix A: the message encrypts byte for byte to the published body")
    void rfc8291Vector() {
        byte[] body = WebPushCrypto.encrypt(PLAINTEXT.getBytes(StandardCharsets.US_ASCII), EcKeys.unb64(UA_PUBLIC),
                EcKeys.unb64(AUTH), rfcServerKeys(), EcKeys.unb64(SALT));
        assertThat(EcKeys.b64(body)).isEqualTo(BODY);
        assertThat(body).hasSize(144);
        assertThat(EcKeys.b64(java.util.Arrays.copyOf(body, WebPushCrypto.HEADER_LENGTH))).isEqualTo(
                "DGv6ra1nlYgDCS1FRnbzlwAAEABBBP4z9KsN6nGRTbVYI_c7VJSPQTBtkgcy27mlmlMoZIIgDll6e3vCYLocInmYWAmS6TlzAC8wEqKK6PBru3jl7A8");
    }

    @Test
    @DisplayName("RFC 8291 Appendix A: ecdh_secret, PRK_key, IKM, PRK, CEK and NONCE")
    void rfc8291Intermediates() {
        byte[] ecdh = EcKeys.ecdh(EcKeys.privateFromScalar(EcKeys.unb64(AS_PRIVATE)), EcKeys.decodePoint(EcKeys.unb64(UA_PUBLIC)));
        assertThat(EcKeys.b64(ecdh)).isEqualTo("kyrL1jIIOHEzg3sM2ZWRHDRB62YACZhhSlknJ672kSs");
        // The receiver computes the same secret.
        assertThat(EcKeys.ecdh(EcKeys.privateFromScalar(EcKeys.unb64(UA_PRIVATE)), EcKeys.decodePoint(EcKeys.unb64(AS_PUBLIC))))
                .isEqualTo(ecdh);
        WebPushCrypto.Keys keys = WebPushCrypto.keys(ecdh, EcKeys.unb64(AUTH), EcKeys.unb64(UA_PUBLIC),
                EcKeys.unb64(AS_PUBLIC), EcKeys.unb64(SALT));
        assertThat(EcKeys.b64(keys.prkKey())).isEqualTo("Snr3JMxaHVDXHWJn5wdC52WjpCtd2EIEGBykDcZW32k");
        assertThat(EcKeys.b64(keys.ikm())).isEqualTo("S4lYMb_L0FxCeq0WhDx813KgSYqU26kOyzWUdsXYyrg");
        assertThat(EcKeys.b64(keys.prk())).isEqualTo("09_eUZGrsvxChDCGRCdkLiDXrReGOEVeSCdCcPBSJSc");
        assertThat(EcKeys.b64(keys.cek())).isEqualTo("oIhVW04MRdy2XN9CiKLxTg");
        assertThat(EcKeys.b64(keys.nonce())).isEqualTo("4h_95klXJ5E_qnoN");
    }

    @Test
    @DisplayName("the published body decrypts with the receiver's key; random encryptions round-trip")
    void decrypt() {
        ECPrivateKey ua = EcKeys.privateFromScalar(EcKeys.unb64(UA_PRIVATE));
        byte[] plain = WebPushCrypto.decrypt(EcKeys.unb64(BODY), ua, EcKeys.unb64(UA_PUBLIC), EcKeys.unb64(AUTH));
        assertThat(new String(plain, StandardCharsets.US_ASCII)).isEqualTo(PLAINTEXT);

        byte[] max = new byte[WebPushCrypto.MAX_PLAINTEXT];
        java.util.Arrays.fill(max, (byte) 'x');
        byte[] body = WebPushCrypto.encrypt(max, EcKeys.unb64(UA_PUBLIC), EcKeys.unb64(AUTH));
        assertThat(body).hasSize(WebPushCrypto.RECORD_SIZE);
        assertThat(WebPushCrypto.decrypt(body, ua, EcKeys.unb64(UA_PUBLIC), EcKeys.unb64(AUTH))).isEqualTo(max);
        // A fresh key and salt every time.
        assertThat(WebPushCrypto.encrypt(max, EcKeys.unb64(UA_PUBLIC), EcKeys.unb64(AUTH))).isNotEqualTo(body);
    }

    @Test
    @DisplayName("a plaintext over 3993 bytes, a bad auth secret and an off-curve key are refused")
    void refusals() {
        assertThat(WebPushCrypto.MAX_PLAINTEXT).isEqualTo(3993);
        assertThatThrownBy(() -> WebPushCrypto.encrypt(new byte[3994], EcKeys.unb64(UA_PUBLIC), EcKeys.unb64(AUTH)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> WebPushCrypto.encrypt(new byte[1], EcKeys.unb64(UA_PUBLIC), new byte[15]))
                .isInstanceOf(IllegalArgumentException.class);
        byte[] offCurve = EcKeys.unb64(UA_PUBLIC);
        offCurve[64] ^= 1;
        assertThatThrownBy(() -> EcKeys.decodePoint(offCurve)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> EcKeys.decodePoint(new byte[33])).isInstanceOf(IllegalArgumentException.class);
        byte[] tampered = EcKeys.unb64(BODY);
        tampered[tampered.length - 1] ^= 1;
        assertThatThrownBy(() -> WebPushCrypto.decrypt(tampered, EcKeys.privateFromScalar(EcKeys.unb64(UA_PRIVATE)),
                EcKeys.unb64(UA_PUBLIC), EcKeys.unb64(AUTH))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("RFC 5869 test cases 1, 2 and 3 (SHA-256)")
    void hkdfVectors() {
        byte[] ikm1 = hex("0b".repeat(22));
        assertThat(Hkdf.extract(hex("000102030405060708090a0b0c"), ikm1))
                .isEqualTo(hex("077709362c2e32df0ddc3f0dc47bba6390b6c73bb50f9c3122ec844ad7c2b3e5"));
        assertThat(Hkdf.derive(hex("000102030405060708090a0b0c"), ikm1, hex("f0f1f2f3f4f5f6f7f8f9"), 42)).isEqualTo(hex(
                "3cb25f25faacd57a90434f64d0362f2a2d2d0a90cf1a5a4c5db02d56ecc4c5bf34007208d5b887185865"));

        StringBuilder ikm = new StringBuilder();
        StringBuilder salt = new StringBuilder();
        StringBuilder info = new StringBuilder();
        for (int i = 0; i < 80; i++) {
            ikm.append(String.format("%02x", i));
            salt.append(String.format("%02x", 0x60 + i));
            info.append(String.format("%02x", 0xb0 + i));
        }
        assertThat(Hkdf.extract(hex(salt.toString()), hex(ikm.toString())))
                .isEqualTo(hex("06a6b88c5853361a06104c9ceb35b45cef760014904671014a193f40c15fc244"));
        assertThat(Hkdf.derive(hex(salt.toString()), hex(ikm.toString()), hex(info.toString()), 82)).isEqualTo(hex(
                "b11e398dc80327a1c8e7f78c596a49344f012eda2d4efad8a050cc4c19afa97c59045a99cac7827271cb41c65e590e09"
                        + "da3275600c2f09b8367793a9aca3db71cc30c58179ec3e87c14c01d5c1f3434f1d87"));

        assertThat(Hkdf.extract(new byte[0], ikm1))
                .isEqualTo(hex("19ef24a32c717b167f33a91d6f648bdf96596776afdb6377ac434c1c293ccb04"));
        assertThat(Hkdf.derive(null, ikm1, null, 42)).isEqualTo(hex(
                "8da4e775a563c18f715f802a063c5a31b8a11f5c5ee1879ec3454e5f3c738d2d9d201395faa4b61a96c8"));
        assertThatThrownBy(() -> Hkdf.expand(new byte[32], null, 255 * 32 + 1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("RFC 8292 Figure 1: the published token verifies with its key, and our claims encode identically")
    void rfc8292Example() {
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJhdWQiOiJodHRwczovL3B1c2guZXhhbXBsZS5uZXQiLCJleHAiOjE0NTM1MjM3NjgsInN1YiI6Im1haWx0bzpwdXNoQGV4YW1wbGUuY29tIn0.i3CYb7t4xfxCDquptFOepC9GAu_HLGkMlMuCGSK2rpiUfnK9ojFwDXb1JrErtmysazNjjvW2L9OkSSHzvoD1oA";
        ECPublicKey key = EcKeys.decodePoint(EcKeys.unb64(
                "BA1Hxzyi1RUM1b5wjxsn7nGxAszw2u61m164i3MrAIxHF6YK5h4SDYic-dRuU_RCPCfA5aq9ojSwk5Y2EmClBPs"));
        assertThat(Vapid.verify(token, key)).isTrue();
        KeyPair ours = EcKeys.generate();
        String jwt = Vapid.jwt((ECPrivateKey) ours.getPrivate(), "https://push.example.net", 1453523768L,
                "mailto:push@example.com");
        assertThat(jwt.split("\\.")[0]).isEqualTo(token.split("\\.")[0]);
        assertThat(jwt.split("\\.")[1]).isEqualTo(token.split("\\.")[1]);
        assertThat(Vapid.verify(jwt, (ECPublicKey) ours.getPublic())).isTrue();
        assertThat(Vapid.verify(jwt, key)).isFalse();
        assertThat(Vapid.verify(jwt.substring(0, jwt.length() - 2) + "AA", (ECPublicKey) ours.getPublic())).isFalse();
        assertThat(Vapid.verify("not.a", key)).isFalse();
        assertThat(Vapid.authorization("t1", "k1")).isEqualTo("vapid t=t1, k=k1");
    }

    @Test
    @DisplayName("the origin of an endpoint drops path, query and default ports")
    void origin() {
        assertThat(Vapid.origin(java.net.URI.create("https://Push.Example.net/p/abc?x=1"))).isEqualTo("https://push.example.net");
        assertThat(Vapid.origin(java.net.URI.create("https://push.example.net:443/p"))).isEqualTo("https://push.example.net");
        assertThat(Vapid.origin(java.net.URI.create("https://push.example.net:8443/p"))).isEqualTo("https://push.example.net:8443");
    }

    @Test
    @DisplayName("DER and raw R‖S convert both ways and interoperate with the JDK's P1363 format")
    void derRaw() throws Exception {
        KeyPair pair = EcKeys.generate();
        for (int i = 0; i < 50; i++) {
            byte[] data = ("message " + i).getBytes(StandardCharsets.UTF_8);
            Signature der = Signature.getInstance("SHA256withECDSA");
            der.initSign(pair.getPrivate());
            der.update(data);
            byte[] derSig = der.sign();
            byte[] raw = Vapid.derToRaw(derSig);
            assertThat(raw).hasSize(64);
            assertThat(Vapid.rawToDer(raw)).isEqualTo(derSig);

            Signature p1363 = Signature.getInstance("SHA256withECDSAinP1363Format");
            p1363.initVerify(pair.getPublic());
            p1363.update(data);
            assertThat(p1363.verify(raw)).isTrue();
        }
        // Small R and S (leading zeros) still pad to 32 bytes each.
        byte[] small = new byte[64];
        small[31] = 1;
        small[63] = (byte) 0x80;
        assertThat(Vapid.derToRaw(Vapid.rawToDer(small))).isEqualTo(small);
        assertThatThrownBy(() -> Vapid.derToRaw(new byte[] {0x31, 0, 0, 0, 0, 0, 0, 0})).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Vapid.rawToDer(new byte[63])).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("points and scalars encode as 65 and 32 bytes; base64url accepts padding and standard characters")
    void encodings() {
        KeyPair pair = EcKeys.generate();
        byte[] point = EcKeys.encodePoint((ECPublicKey) pair.getPublic());
        assertThat(point).hasSize(65);
        assertThat(point[0]).isEqualTo((byte) 4);
        assertThat(EcKeys.b64(point)).hasSize(87);
        assertThat(EcKeys.decodePoint(point).getW()).isEqualTo(((ECPublicKey) pair.getPublic()).getW());
        ECPrivateKey key = (ECPrivateKey) pair.getPrivate();
        assertThat(EcKeys.privateFromScalar(EcKeys.scalar(key)).getS()).isEqualTo(key.getS());
        assertThat(EcKeys.privateFromPkcs8(key.getEncoded()).getS()).isEqualTo(key.getS());
        assertThat(EcKeys.unb64("BTBZMqHH6r4Tts7J_aSIgg==")).isEqualTo(EcKeys.unb64(AUTH));
        assertThat(EcKeys.unb64("BTBZMqHH6r4Tts7J/aSIgg")).isEqualTo(EcKeys.unb64(AUTH));
    }
}
