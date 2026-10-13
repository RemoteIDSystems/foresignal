/*
 * The local-feed rules (band 1090/978, a path ending in aircraft.json, no credentials, query or fragment,
 * a 2 s timeout, staleness at 10 s by the feed's own now, loopback and private hosts only) are ported in
 * part from God's Eye View, server/providers/local-receivers.js (parseEntry, LOCAL_RECEIVER_* constants,
 * readFeed) and src/data/tapAddress.js (the receiver-tap address rule),
 * https://github.com/bilawalsidhu/gods-eye-view, commit 591f299d11f38a612629a274463196d57ae3862e.
 * Copyright (c) 2026 Bilawal Sidhu. MIT License; the full notice is in THIRD_PARTY_NOTICES.md.
 * Changes: Java; each feed is a numbered source slot with its own settings instead of an environment
 * list; host checking and pinning are Foresignal's LAN_URL validation (OutboundUrlPolicy LAN_ONLY), which
 * also accepts unique-local IPv6 and refuses link-local; the body cap is 4 MB.
 */
package ai.foresignal.source.adapters.local;

import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * What every local receiver source shares (prompt 069): the slot count, the descriptor defaults and the
 * URL rules.
 *
 * <ul>
 *   <li><strong>Egress:</strong> {@code LOCAL_ONLY} and {@code egressOverridable} on every descriptor: the
 *       data is the operator's own, so the operator may choose to share it, but it never leaves by default.
 *   <li><strong>Provenance:</strong> no upstream license; the attribution names the operator's receiver.
 *   <li><strong>URLs:</strong> only {@code LAN_URL} parameters, never a host list from a request.
 * </ul>
 */
public final class LocalReceivers {

    /** The property setting how many slots each family registers. */
    public static final String SLOTS_PROPERTY = "foresignal.local-receivers.slots";

    /** The slots per family when the property is unset. */
    public static final int DEFAULT_SLOTS = 2;

    /** The most slots a family registers. */
    public static final int MAX_SLOTS = 16;

    /** The attribution of every local source. */
    static final String ATTRIBUTION = "The operator's own receiver";

    /** The license line of every local source (the descriptor field is named {@code licence}). */
    static final String LICENSE = "Operator's own data (no upstream license)";

    /** The sentence every description ends with. */
    static final String LOCAL_NOTE = " The URL must resolve to a loopback, private or unique-local address on the"
            + " operator's network; it is resolved once and pinned. Disabled by default; egress LOCAL_ONLY (the"
            + " operator may set it to OPEN).";

    /** A URL path ending in {@code aircraft.json} (GEV {@code parseEntry}). */
    static final Pattern AIRCRAFT_JSON = Pattern.compile("(?:^|.*/)aircraft\\.json$");

    /** A URL path ending in {@code stats.json}. */
    static final Pattern STATS_JSON = Pattern.compile("(?:^|.*/)stats\\.json$");

    private LocalReceivers() {}

    /**
     * Applies the shared descriptor fields: {@code egress} LOCAL_ONLY, overridable, the attribution and the
     * license line.
     */
    static SourceDescriptor.Builder local(SourceDescriptor.Builder builder) {
        return builder.attribution(ATTRIBUTION)
                .licence(LICENSE)
                .egress(Egress.LOCAL_ONLY)
                .egressOverridable(true);
    }

    /**
     * The problems of an {@code http(s)} receiver URL whose path must match {@code path} (GEV
     * {@code parseEntry}): no user info, query or fragment, and the expected file name.
     *
     * @param name the parameter name the problems are reported under
     * @param fileName the expected file name, for the message
     */
    static Map<String, String> checkJsonUrl(ValidatedParams params, String name, Pattern path, String fileName) {
        Map<String, String> problems = new LinkedHashMap<>();
        URI url = params.lanUrl(name);
        if (url == null) {
            return problems;
        }
        if (url.getRawUserInfo() != null) {
            problems.put(name, "must not carry credentials");
        } else if (url.getRawQuery() != null || url.getRawFragment() != null) {
            problems.put(name, "must not have a query or fragment");
        } else if (url.getRawPath() == null || !path.matcher(url.getRawPath()).matches()) {
            problems.put(name, "the path must end in " + fileName + ", for example http://192.168.1.50/data/" + fileName);
        }
        return problems;
    }

    /** {@code value} rounded half-up to {@code places} decimals, without {@code -0.0}. */
    static double round(double value, int places) {
        double rounded = java.math.BigDecimal.valueOf(value)
                .setScale(places, java.math.RoundingMode.HALF_UP).doubleValue();
        return rounded == 0.0 ? 0.0 : rounded;
    }
}
