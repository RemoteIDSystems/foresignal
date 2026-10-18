package ai.foresignal.exchange;

import ai.foresignal.provenance.Jwk;
import ai.foresignal.security.Ulids;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Signature;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.json.JsonMapper;

/**
 * Packages "recorded" from another instance for the prompt 132 tests: a fresh Ed25519 key with its JWKS, detached
 * JWS signing exactly as 109's {@code JwsSigner} does ({@code b64:false}, {@code crit:["b64"]}), and report packages
 * addressed to principals of the instance under test. Nothing here talks to a network.
 */
public final class ExchangeTestPackages {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final Base64.Encoder URL = Base64.getUrlEncoder().withoutPadding();

    private ExchangeTestPackages() {}

    /**
     * A signing key of another instance.
     *
     * @param kid the RFC 7638 thumbprint
     * @param x the raw public point
     * @param privateKey the private key
     * @param jwks its JWKS document
     */
    public record Key(String kid, byte[] x, PrivateKey privateKey, Map<String, Object> jwks) {}

    /** A new Ed25519 key. */
    public static Key newKey() {
        try {
            KeyPair kp = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
            byte[] der = kp.getPublic().getEncoded();
            byte[] x = Arrays.copyOfRange(der, der.length - 32, der.length);
            String kid = Jwk.thumbprint(x);
            return new Key(kid, x, kp.getPrivate(), Map.of("keys", List.of(Jwk.of(kid, x))));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** A detached JWS over {@code payload} with {@code key} ({@code alg} may be overridden, e.g. {@code none}). */
    public static String sign(Key key, byte[] payload, String alg) {
        try {
            Map<String, Object> header = new LinkedHashMap<>();
            header.put("alg", alg);
            header.put("kid", key.kid());
            header.put("typ", "application/json");
            header.put("b64", false);
            header.put("crit", List.of("b64"));
            String headerB64 = URL.encodeToString(JSON.writeValueAsBytes(header));
            byte[] prefix = (headerB64 + ".").getBytes(StandardCharsets.US_ASCII);
            byte[] input = new byte[prefix.length + payload.length];
            System.arraycopy(prefix, 0, input, 0, prefix.length);
            System.arraycopy(payload, 0, input, prefix.length, payload.length);
            Signature s = Signature.getInstance("Ed25519");
            s.initSign(key.privateKey());
            s.update(input);
            return headerB64 + ".." + URL.encodeToString(s.sign());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** A detached JWS with {@code alg: Ed25519}. */
    public static String sign(Key key, byte[] payload) {
        return sign(key, payload, "Ed25519");
    }

    /**
     * The parts of a package before zipping, so tests can tamper with any of them.
     *
     * @param manifest the manifest
     * @param manifestBytes its exact bytes
     * @param signature the detached JWS over them
     * @param contents the content files by ZIP path
     */
    public record Parts(Map<String, Object> manifest, byte[] manifestBytes, String signature,
            Map<String, Object> contents) {

        /** The ZIP. */
        public byte[] zip() {
            return PackageCodec.write(manifestBytes, signature, contents);
        }
    }

    /**
     * A report package from {@code sender} (instance {@code senderInstance}) to {@code handles} on
     * {@code recipientInstance} (or {@code *} when {@code handles} is null), labeled {@code tlp}, citing one remote
     * situation.
     */
    public static Parts report(Key sender, String senderInstance, String recipientInstance, List<String> handles,
            String tlp, String situationId) {
        String reportRef = "report:" + Ulids.next(Instant.parse("2026-10-12T10:00:00Z"));
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("ref", "situation:" + situationId);
        envelope.put("type", "situation");
        envelope.put("title", "Remote situation " + situationId);
        envelope.put("status", "active");
        envelope.put("time", Map.of("start", "2026-10-11T00:00:00Z"));
        envelope.put("bbox", List.of(42.0, 12.0, 44.0, 15.0));
        envelope.put("provenance", Map.of("sourceIds", List.of()));
        Map<String, Object> objects = new LinkedHashMap<>();
        objects.put("id", "b01");
        objects.put("type", "objects");
        objects.put("title", "Cited");
        objects.put("textOrigin", "deterministic");
        objects.put("items", List.of(Map.of("ref", "situation:" + situationId, "envelope", envelope)));
        Map<String, Object> text = new LinkedHashMap<>();
        text.put("id", "b02");
        text.put("type", "markdown");
        text.put("title", "Assessment");
        text.put("textOrigin", "author");
        text.put("markdown", "Shipping remains disrupted. <script>alert(1)</script>");
        text.put("citations", List.of("situation:" + situationId));
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("id", reportRef.substring("report:".length()));
        report.put("ref", reportRef);
        report.put("displayId", "RPT-2026-0042");
        report.put("title", "Red Sea weekly");
        report.put("status", "published");
        report.put("owner", Map.of("handle", "alice", "displayName", "Alice"));
        report.put("authors", List.of(Map.of("handle", "alice", "displayName", "Alice")));
        report.put("tlp", tlp);
        report.put("publishedAt", "2026-10-12T09:00:00Z");
        report.put("blocks", List.of(objects, text));
        Map<String, Object> bundle = new LinkedHashMap<>();
        bundle.put("report", report);
        bundle.put("revision", 1);
        bundle.put("tlp", tlp);
        bundle.put("envelopes", List.of(envelope));
        bundle.put("snapshots", Map.of());
        bundle.put("attachments", List.of());
        Map<String, Object> contents = new LinkedHashMap<>();
        contents.put(ExchangeVocabulary.BUNDLE, JSON.writeValueAsBytes(bundle));
        contents.put(ExchangeVocabulary.RENDITION_MD, "# Red Sea weekly\n".getBytes(StandardCharsets.UTF_8));
        contents.put(ExchangeVocabulary.RENDITION_HTML, "<html><script>alert(1)</script></html>"
                .getBytes(StandardCharsets.UTF_8));
        return sign(sender, manifest(sender, senderInstance, recipientInstance, handles, tlp, reportRef, contents),
                contents);
    }

    /** The manifest of a package with {@code contents}. */
    public static Map<String, Object> manifest(Key sender, String senderInstance, String recipientInstance,
            List<String> handles, String tlp, String objectRef, Map<String, Object> contents) {
        Map<String, Object> manifest = new LinkedHashMap<>();
        manifest.put("packageId", Ulids.next(Instant.now()));
        manifest.put("type", "report");
        manifest.put("objectRef", objectRef);
        manifest.put("title", "Red Sea weekly");
        manifest.put("sender", Map.of("instanceId", senderInstance, "instanceName", "Peer A", "keyId", sender.kid()));
        manifest.put("authors", List.of(Map.of("handle", "alice", "displayName", "Alice", "instanceId", senderInstance,
                "role", "author")));
        manifest.put("tlp", tlp);
        manifest.put("createdAt", "2026-10-12T10:00:00Z");
        manifest.put("recipients", handles == null ? "*" : List.of(Map.of("instanceId", recipientInstance,
                "principals", handles)));
        manifest.put("contentHashes", PackageCodec.hashes(contents));
        manifest.put("message", "For your review.");
        manifest.put("provenanceChain", List.of(Map.of("instanceId", senderInstance, "principal", "alice",
                "productRef", objectRef, "role", "origin")));
        return manifest;
    }

    /** Signs a manifest and returns the parts. */
    public static Parts sign(Key sender, Map<String, Object> manifest, Map<String, Object> contents) {
        byte[] bytes = JSON.writeValueAsBytes(manifest);
        return new Parts(manifest, bytes, sign(sender, bytes), contents);
    }
}
