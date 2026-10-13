package ai.foresignal.source.adapters.ct;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.JsonProps;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * One page of the Cert Spotter {@code /v1/issuances} response as {@code certificate-issuance} records (prompt 076).
 * Pure.
 *
 * <ul>
 *   <li><strong>Identity:</strong> {@code id} (SSLMate's opaque issuance id); the page's last id is the next
 *       {@code after} cursor.
 *   <li><strong>Time:</strong> the API gives no discovery time, so the source is {@code observedAtMode RECEIVED};
 *       {@code eventTime = not_before}, {@code validUntil = not_after} (an upstream-asserted expiry).
 *   <li><strong>Payload:</strong> {@code watchedDomain, dnsNames} (at most {@value #MAX_DNS_NAMES}),
 *       {@code issuerName} ({@code issuer.friendly_name}), {@code issuerOperator} ({@code issuer.operator.name}),
 *       {@code issuerCaaDomains, notBefore, notAfter, revoked, certSha256, pubkeySha256} and
 *       {@code unexpectedIssuer}: true when expected issuers are configured and the issuer matches none of them, by
 *       friendly name or by CAA domain (case-insensitive); an issuance without issuer data is unexpected then.
 * </ul>
 */
public final class CertSpotterParser {

    /** The record kind. */
    public static final String KIND = "certificate-issuance";

    /** The most DNS names kept. */
    public static final int MAX_DNS_NAMES = 100;

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /**
     * One parsed page.
     *
     * @param records one record per issuance
     * @param lastId the id of the page's last issuance, or null for an empty page
     * @param count the issuances on the page
     * @param skipped skip reason to count
     */
    public record Page(List<NormalizedRecord> records, String lastId, int count, Map<String, Integer> skipped) {}

    private CertSpotterParser() {}

    /**
     * Parses one page.
     *
     * @param body the JSON array
     * @param watchedDomain the domain this page was queried for
     * @param expectedIssuers expected issuer names or CAA domains; empty means no expectation
     * @throws UpstreamFormatException when the body is not a JSON array (an error object included)
     */
    public static Page parse(byte[] body, String watchedDomain, Collection<String> expectedIssuers) {
        JsonNode root;
        try {
            root = JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("Cert Spotter response is not JSON: " + e.getOriginalMessage(), "", e);
        }
        if (root == null || !root.isArray()) {
            String code = root != null && root.isObject() ? JsonProps.text(root, "code") : null;
            throw new UpstreamFormatException("Cert Spotter response is not an array"
                    + (code == null ? "" : " (error " + code + ")"), "");
        }
        Set<String> expected = new LinkedHashSet<>();
        if (expectedIssuers != null) {
            for (String e : expectedIssuers) {
                if (e != null && !e.isBlank()) {
                    expected.add(e.strip().toLowerCase(Locale.ROOT));
                }
            }
        }
        List<NormalizedRecord> records = new ArrayList<>();
        Map<String, Integer> skipped = new TreeMap<>();
        String lastId = null;
        for (JsonNode issuance : root) {
            String id = JsonProps.text(issuance, "id");
            if (id != null) {
                lastId = id;
            }
            NormalizedRecord record = issuance(issuance, watchedDomain, expected);
            if (record == null) {
                skipped.merge("missing-id", 1, Integer::sum);
            } else {
                records.add(record);
            }
        }
        return new Page(records, lastId, root.size(), skipped);
    }

    /**
     * One issuance, or null without an id.
     *
     * @param issuance the issuance object
     * @param watchedDomain the domain it was found for
     * @param expected lower-case expected issuer names or CAA domains; empty means no expectation
     */
    public static NormalizedRecord issuance(JsonNode issuance, String watchedDomain, Set<String> expected) {
        String id = JsonProps.text(issuance, "id");
        if (id == null || id.length() > 64) {
            return null;
        }
        Instant notBefore = JsonProps.iso(issuance, "not_before");
        Instant notAfter = JsonProps.iso(issuance, "not_after");
        JsonNode issuer = issuance.get("issuer");
        String issuerName = JsonProps.text(issuer, "friendly_name");
        String operator = issuer == null ? null : JsonProps.text(issuer.get("operator"), "name");
        List<String> caa = strings(issuer == null ? null : issuer.get("caa_domains"), 20);
        NormalizedRecord.Builder b = NormalizedRecord.builder(id, KIND).eventTime(notBefore).validUntil(notAfter);
        JsonNode revoked = issuance.get("revoked");
        b.put("watchedDomain", watchedDomain)
                .put("dnsNames", strings(issuance.get("dns_names"), MAX_DNS_NAMES))
                .put("issuerName", issuerName)
                .put("issuerOperator", operator)
                .put("issuerCaaDomains", caa)
                .put("notBefore", notBefore == null ? null : notBefore.toString())
                .put("notAfter", notAfter == null ? null : notAfter.toString())
                .put("revoked", revoked != null && revoked.isBoolean() ? revoked.booleanValue() : null)
                .put("certSha256", JsonProps.text(issuance, "cert_sha256"))
                .put("pubkeySha256", JsonProps.text(issuance, "pubkey_sha256"))
                .put("unexpectedIssuer", unexpected(issuerName, caa, expected));
        return b.build();
    }

    /** True when {@code expected} is set and neither the name nor a CAA domain is in it. */
    static boolean unexpected(String issuerName, List<String> caa, Set<String> expected) {
        if (expected == null || expected.isEmpty()) {
            return false;
        }
        if (issuerName != null && expected.contains(issuerName.toLowerCase(Locale.ROOT))) {
            return false;
        }
        if (caa != null) {
            for (String domain : caa) {
                if (expected.contains(domain.toLowerCase(Locale.ROOT))) {
                    return false;
                }
            }
        }
        return true;
    }

    /** The text elements of {@code array}, de-duplicated, at most {@code max}; null when none. */
    static List<String> strings(JsonNode array, int max) {
        if (array == null || !array.isArray()) {
            return null;
        }
        Set<String> out = new LinkedHashSet<>();
        for (JsonNode v : array) {
            if (v.isString() && !v.asString().isBlank() && out.size() < max) {
                out.add(v.asString().strip());
            }
        }
        return out.isEmpty() ? null : List.copyOf(out);
    }
}
