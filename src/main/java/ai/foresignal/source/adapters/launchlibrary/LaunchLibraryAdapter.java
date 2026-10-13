package ai.foresignal.source.adapters.launchlibrary;

// Origin: the optional Authorization: Token header follows God's Eye View (MIT, Copyright (c) 2026 Bilawal
// Sidhu): server/providers/space/launch-library.js (launchLibraryRequestHeaders). See THIRD_PARTY_NOTICES.md.

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code launch-library}: orbital launches from The Space Devs' Launch Library 2 (prompt 064; ported in part
 * from God's Eye View {@code server/providers/space/launch-library.js}, MIT, see {@code THIRD_PARTY_NOTICES.md}:
 * the optional {@code Authorization: Token} header).
 *
 * <p>{@code GET /2.3.0/launches/?net__gte={now−7d}&net__lte={now+60d}&limit=100&mode=normal&ordering=net},
 * following {@code next} for at most {@value #MAX_PAGES} pages. Verified 2026-10: 2.3.0 is current (2.4.0
 * answered 500) and the anonymous limit is 15 requests an hour, so the default interval is 15 minutes; with a
 * {@code token} 5 minutes is allowed. A 429 honors {@code Retry-After}, else backs off 30 minutes.
 *
 * <p>Kind {@code launch}, revised-event time base ({@code eventTime} = {@code net}, {@code observedAt} =
 * {@code last_updated}), pad point geometry. {@code missionDescription} is embedded for search; it is upstream
 * text and untrusted when it reaches an LLM (prompt 028).
 */
@Component
public class LaunchLibraryAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "launch-library";

    /** The production API. */
    public static final String DEFAULT_BASE_URL = "https://ll.thespacedevs.com";

    /** Test-only override of {@link #DEFAULT_BASE_URL}. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.launch-library.base-url";

    /** The optional API token credential. */
    public static final String TOKEN = "token";

    /** The most pages read per run. */
    public static final int MAX_PAGES = 3;

    /** The backoff after a 429 without Retry-After. */
    public static final Duration THROTTLE_BACKOFF = Duration.ofMinutes(30);

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank means {@value #DEFAULT_BASE_URL}
     */
    public LaunchLibraryAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        String chosen = baseUrlOverride == null || baseUrlOverride.isBlank() ? DEFAULT_BASE_URL : baseUrlOverride.trim();
        this.baseUrl = chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The first page's URL for {@code now}. */
    public URI firstPage(Instant now) {
        Instant from = now.minus(Duration.ofDays(7)).truncatedTo(ChronoUnit.SECONDS);
        Instant to = now.plus(Duration.ofDays(60)).truncatedTo(ChronoUnit.SECONDS);
        return URI.create(baseUrl + "/2.3.0/launches/?net__gte=" + from + "&net__lte=" + to
                + "&limit=100&mode=normal&ordering=net");
    }

    /**
     * Fetches up to three pages.
     *
     * @throws AdapterException when the upstream fails
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        Map<String, String> headers = new LinkedHashMap<>();
        String token = ctx.credentials().get(TOKEN);
        if (token != null && !token.isBlank()) {
            headers.put("Authorization", "Token " + token.strip());
        }
        List<NormalizedRecord> records = new ArrayList<>();
        URI page = firstPage(ctx.now());
        int pages = 0;
        boolean more = false;
        while (page != null && pages < MAX_PAGES) {
            UpstreamResponse response = ctx.http().get(page, headers);
            pages++;
            if (response.truncated()) {
                throw new UpstreamFormatException("Launch Library page exceeded maxResponseBytes", null);
            }
            LaunchLibraryParser.Page parsed = LaunchLibraryParser.parse(response.body());
            records.addAll(parsed.records());
            page = sameHost(parsed.next());
            more = page != null;
        }
        if (more) {
            ctx.log().info("{}: stopped after {} pages; later launches are left for the next run", ctx.sourceId(),
                    MAX_PAGES);
        }
        return more ? FetchResult.of(records) : FetchResult.complete(records);
    }

    /** {@code next} when it points at the configured API host, else null (never follow a link elsewhere). */
    URI sameHost(String next) {
        if (next == null || next.isBlank()) {
            return null;
        }
        try {
            URI uri = URI.create(next);
            URI base = URI.create(baseUrl);
            if (!base.getHost().equalsIgnoreCase(uri.getHost()) || base.getPort() != uri.getPort()
                    || !base.getScheme().equalsIgnoreCase(uri.getScheme())) {
                return null;
            }
            return uri;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static SourceDescriptor buildDescriptor() {
        return SourceDescriptor.builder(ID)
                .name("Launch Library 2 launches")
                .description("Orbital launches from a week ago to 60 days ahead, from The Space Devs' Launch Library 2"
                        + " (2.3.0). Anonymous access allows 15 requests an hour, so the interval is 15 minutes;"
                        + " an optional token (sent as Authorization: Token) allows 5 minutes.")
                .kind(LaunchLibraryParser.KIND)
                .mode(Mode.POLLING)
                .homepage("https://thespacedevs.com/llapi")
                .attribution("Launch Library 2 by The Space Devs, https://thespacedevs.com")
                .licence("The Space Devs API terms (unverified)")
                .termsUrl("https://thespacedevs.com/llapi")
                .credential(CredentialField.optional(TOKEN,
                        "Launch Library 2 API token (Patreon); allows a 5-minute interval"))
                .interval(Duration.ofMinutes(5), Duration.ofMinutes(15))
                .defaultBudget(3, 300)
                .defaultRetryAfter(THROTTLE_BACKOFF)
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(IngestPolicy.all())
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .titleField("name")
                .searchFields("name", "provider", "rocket", "missionName", "padName", "location")
                .embedFields("missionDescription")
                .rankField("probability")
                .intervalFields("windowStart", "windowEnd")
                .tileProperties("name", "status", "net", "provider", "probability")
                .kindDisplay(LaunchLibraryParser.KIND, new KindDisplay("{name}", "rocket", "#F08C00", null))
                .zeroResultsNormal(true)
                .build();
    }
}
