/*
 * The 30 s cooldown after a 429 without Retry-After follows God's Eye View,
 * server/providers/aircraft/adsb-lol.js (RATE_LIMIT_COOLDOWN_MS), https://github.com/bilawalsidhu/gods-eye-view,
 * commit 591f299d11f38a612629a274463196d57ae3862e.
 * Copyright (c) 2026 Bilawal Sidhu. MIT License; the full notice is in THIRD_PARTY_NOTICES.md.
 * Changes: Java; applied through the descriptor's defaultRetryAfter by UpstreamClient, with Retry-After
 * clamped to [5 s, 1 h] by BackoffRegistry instead of GEV's [5 s, 120 s].
 */
package ai.foresignal.source.adapters.adsblol;

import ai.foresignal.source.formats.readsb.MapperOptions;
import ai.foresignal.source.formats.readsb.ReadsbAircraftMapper;
import ai.foresignal.source.formats.readsb.ReadsbDocument;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;

/**
 * What the adsb.lol sources share (prompt 062): the base URL, the descriptor fields of the upstream and
 * the response check.
 */
final class AdsbLol {

    /** The production upstream. */
    static final String DEFAULT_BASE_URL = "https://api.adsb.lol";

    /** Test-only override of {@link #DEFAULT_BASE_URL} (Adapter Guide §8). */
    static final String BASE_URL_PROPERTY = "foresignal.test.adapters.adsblol.base-url";

    /** The attribution line. */
    static final String ATTRIBUTION = "adsb.lol";

    /** The data license. */
    static final String LICENCE = "ODbL-1.0";

    /** The API's terms. */
    static final String TERMS_URL = "https://github.com/adsblol/api";

    /** The backoff after a 429 without {@code Retry-After}. */
    static final Duration THROTTLE_RETRY = Duration.ofSeconds(30);

    /** The message of a 200 that is not JSON. */
    static final String NON_JSON_MESSAGE = "adsb.lol answered HTTP 200 with a non-JSON body; non-JSON 200, treated as throttling";

    private AdsbLol() {}

    /** {@code override}, or the production URL when blank, without a trailing slash. */
    static String baseUrl(String override) {
        String chosen = override == null || override.isBlank() ? DEFAULT_BASE_URL : override.trim();
        return chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
    }

    /**
     * Applies the upstream's descriptor fields: polling, attribution, ODbL (egress OPEN: share-alike with
     * attribution), terms, a 403 that means "this client is refused" and the 30 s throttle default.
     */
    static SourceDescriptor.Builder upstream(SourceDescriptor.Builder builder) {
        return builder.mode(Mode.POLLING)
                .homepage("https://adsb.lol")
                .attribution(ATTRIBUTION)
                .licence(LICENCE)
                .termsUrl(TERMS_URL)
                .egress(Egress.OPEN)
                .accessDeniedIsTerminal(true)
                .defaultRetryAfter(THROTTLE_RETRY);
    }

    /**
     * Checks a 200 and maps it.
     *
     * @throws UpstreamFormatException when the body was truncated, or is not JSON (a non-JSON content type,
     *     or a body that does not start with {@code {}): an HTML page served with 200 is how a throttling
     *     front end answers, and it must never parse as "no aircraft"
     */
    static ReadsbAircraftMapper.MapResult parse(FetchContext ctx, UpstreamResponse response) {
        if (response.truncated()) {
            throw new UpstreamFormatException("adsb.lol response exceeded maxResponseBytes and was truncated", null);
        }
        String type = response.contentType() == null ? "" : response.contentType().toLowerCase(Locale.ROOT);
        if (!type.contains("json") || !startsWithBrace(response.body())) {
            throw new UpstreamFormatException(NON_JSON_MESSAGE,
                    new String(response.body(), 0, Math.min(response.body().length, 500), StandardCharsets.UTF_8));
        }
        ReadsbAircraftMapper.MapResult result =
                ReadsbAircraftMapper.mapAll(ReadsbDocument.parse(response.body()), MapperOptions.DEFAULTS);
        if (result.skipped() > 0) {
            ctx.log().debug("{}: skipped {} aircraft without hex", ctx.sourceId(), result.skipped());
        }
        return result;
    }

    /** True when the first non-whitespace byte is {@code {}. */
    static boolean startsWithBrace(byte[] body) {
        for (byte b : body) {
            if (b == ' ' || b == '\t' || b == '\r' || b == '\n') {
                continue;
            }
            return b == '{';
        }
        return false;
    }
}
