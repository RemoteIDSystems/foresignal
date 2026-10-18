package ai.foresignal.exchange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Package signing and verification (prompt 132, RPT-2): the detached JWS over {@code manifest.json} verifies with the
 * sender's JWKS (and with a pinned key); a tampered manifest, signature, header or content fails; the ZIP reader
 * refuses unsafe paths, duplicates, missing parts and oversized packages.
 */
class PackageSigningTest {

    private static final ExchangeTestPackages.Key KEY = ExchangeTestPackages.newKey();

    private static ExchangeTestPackages.Parts parts() {
        return ExchangeTestPackages.report(KEY, "fs-peer-a", "fs-local", List.of("bob"), "AMBER",
                "01JA8Q3V7S2K9W4T6Y1B5N0M3C");
    }

    @Test
    @DisplayName("the manifest JWS verifies with the sender's JWKS and with the pinned key")
    void verifies() {
        ExchangeTestPackages.Parts p = parts();
        PackageCodec.Contents read = PackageCodec.read(p.zip(), 10_000_000);
        assertThat(read.manifestBytes()).isEqualTo(p.manifestBytes());
        assertThat(read.signature()).isEqualTo(p.signature());
        PackageVerifier.Result viaJwks = PackageVerifier.verify(read.manifestBytes(), read.signature(), KEY.jwks());
        assertThat(viaJwks.valid()).isTrue();
        assertThat(viaJwks.kid()).isEqualTo(KEY.kid());
        assertThat(PackageVerifier.verifyPinned(read.manifestBytes(), read.signature(), KEY.x()).valid()).isTrue();
        assertThat(PackageCodec.verifyHashes((Map<?, ?>) p.manifest().get("contentHashes"), read.contents()))
                .isEmpty();
    }

    @Test
    @DisplayName("a tampered manifest, signature or header fails; an unknown key is reported as such")
    void tampered() {
        ExchangeTestPackages.Parts p = parts();
        byte[] changed = new String(p.manifestBytes(), StandardCharsets.UTF_8).replace("AMBER", "CLEAR")
                .getBytes(StandardCharsets.UTF_8);
        assertThat(PackageVerifier.verify(changed, p.signature(), KEY.jwks()).status())
                .isEqualTo(PackageVerifier.INVALID);
        String sig = p.signature();
        char last = sig.charAt(sig.length() - 2);
        String flipped = sig.substring(0, sig.length() - 2) + (last == 'A' ? 'B' : 'A') + sig.charAt(sig.length() - 1);
        assertThat(PackageVerifier.verify(p.manifestBytes(), flipped, KEY.jwks()).valid()).isFalse();
        String none = ExchangeTestPackages.sign(KEY, p.manifestBytes(), "none");
        assertThat(PackageVerifier.verify(p.manifestBytes(), none, KEY.jwks()).valid()).isFalse();
        ExchangeTestPackages.Key other = ExchangeTestPackages.newKey();
        assertThat(PackageVerifier.verify(p.manifestBytes(), p.signature(), other.jwks()).status())
                .isEqualTo(PackageVerifier.UNKNOWN_KEY);
        assertThat(PackageVerifier.verifyPinned(p.manifestBytes(), p.signature(), other.x()).valid()).isFalse();
        assertThat(PackageVerifier.verify(p.manifestBytes(), "garbage", KEY.jwks()).valid()).isFalse();
    }

    @Test
    @DisplayName("changed, missing or unlisted content fails the hash check")
    void contentHashes() {
        ExchangeTestPackages.Parts p = parts();
        Map<String, byte[]> contents = new LinkedHashMap<>(PackageCodec.read(p.zip(), 10_000_000).contents());
        Map<?, ?> hashes = (Map<?, ?>) p.manifest().get("contentHashes");
        contents.put(ExchangeVocabulary.RENDITION_MD, "# Tampered\n".getBytes(StandardCharsets.UTF_8));
        assertThat(PackageCodec.verifyHashes(hashes, contents)).anyMatch(s -> s.contains("rendition.md"));
        contents.remove(ExchangeVocabulary.RENDITION_MD);
        assertThat(PackageCodec.verifyHashes(hashes, contents)).anyMatch(s -> s.contains("missing"));
        contents.put("content/extra.txt", new byte[] {1});
        assertThat(PackageCodec.verifyHashes(hashes, contents)).anyMatch(s -> s.contains("not listed"));
        assertThat(PackageCodec.verifyHashes(null, contents)).isNotEmpty();
    }

    @Test
    @DisplayName("the reader refuses unsafe paths, duplicates, missing parts and oversized packages")
    void reader() throws Exception {
        assertThatThrownBy(() -> PackageCodec.read(zip(Map.of("manifest.json", "{}", "signature.jws", "a..b",
                "content/../evil", "x")), 1_000_000)).isInstanceOf(PackageCodec.FormatException.class);
        assertThatThrownBy(() -> PackageCodec.read(zip(Map.of("manifest.json", "{}", "signature.jws", "a..b",
                "/etc/passwd", "x")), 1_000_000)).isInstanceOf(PackageCodec.FormatException.class);
        assertThatThrownBy(() -> PackageCodec.read(zip(Map.of("signature.jws", "a..b")), 1_000_000))
                .isInstanceOf(PackageCodec.FormatException.class);
        assertThatThrownBy(() -> PackageCodec.read(zip(Map.of("manifest.json", "{}")), 1_000_000))
                .isInstanceOf(PackageCodec.FormatException.class);
        assertThatThrownBy(() -> PackageCodec.read(new byte[] {1, 2, 3}, 1_000_000))
                .isInstanceOf(PackageCodec.FormatException.class);
        assertThatThrownBy(() -> PackageCodec.read(new byte[0], 1_000_000))
                .isInstanceOf(PackageCodec.FormatException.class);
        byte[] big = zip(Map.of("manifest.json", "{}", "signature.jws", "a..b", "content/big.txt", "x".repeat(5000)));
        assertThatThrownBy(() -> PackageCodec.read(big, 2_000)).isInstanceOf(PackageCodec.TooLargeException.class);
        assertThatThrownBy(() -> PackageCodec.checkContentPath("content/a\\b"))
                .isInstanceOf(PackageCodec.FormatException.class);
        assertThatThrownBy(() -> PackageCodec.checkContentPath("content/")).isInstanceOf(
                PackageCodec.FormatException.class);
        PackageCodec.checkContentPath("content/attachments/abc.png");
        assertThat(PackageCodec.key("content/attachments/abc.png")).isEqualTo("attachments/abc.png");
        assertThat(PackageCodec.sha256(new byte[0]))
                .isEqualTo("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
    }

    private static byte[] zip(Map<String, String> entries) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            for (Map.Entry<String, String> e : entries.entrySet()) {
                zip.putNextEntry(new ZipEntry(e.getKey()));
                zip.write(e.getValue().getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
        return out.toByteArray();
    }
}
