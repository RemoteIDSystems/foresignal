package ai.foresignal.source.adapters.alertsinua;

import ai.foresignal.reference.ukr.UaRegionIndex;
import ai.foresignal.source.adapters.conflict.ConflictSources;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.CredentialsRejectedException;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.support.AbsenceTombstoner;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code alerts-in-ua} (prompt 081): the alerts in force in Ukraine from alerts.in.ua ({@link AlertsInUaParser}).
 *
 * <ul>
 *   <li><strong>Request:</strong> a conditional GET of {@value #ACTIVE_URL} (state key {@value #STATE_KEY}: the
 *       stored {@code Last-Modified} goes back as {@code If-Modified-Since}) with the required credential
 *       {@value #TOKEN} as {@code Authorization: Bearer} (never as a query parameter, so it never appears in a URL); a
 *       304 is {@link FetchResult#unchanged()}. A 401 is {@link CredentialsRejectedException}; a 429 puts the source in
 *       backoff in {@code UpstreamClient} (the hard limit is 12 requests per minute per IP).
 *   <li><strong>End of an alert:</strong> an alert that arrives with {@code finished_at} is tombstoned at once; one
 *       that disappears from the list is tombstoned by {@link AbsenceTombstoner} after two consecutive complete
 *       fetches (at most 60 s at the default 30 s interval), per kind.
 * </ul>
 *
 * <p>The token stays on this server: clients read the stored records, never alerts.in.ua.
 */
@Component
public class AlertsInUaAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "alerts-in-ua";

    /** The active alerts. */
    public static final String ACTIVE_URL = "https://api.alerts.in.ua/v1/alerts/active.json";

    /** The conditional GET state key. */
    public static final String STATE_KEY = "alerts-in-ua-active";

    /** The token credential. */
    public static final String TOKEN = "token";

    /** The description, with the upstream's disclaimer. */
    static final String DESCRIPTION = "Air-raid and other threat alerts in force in Ukraine from alerts.in.ua"
            + " (volunteer-run; data from official channels such as the national alert API, regional military"
            + " administrations and the State Emergency Service), with the oblast or raion polygon of each alert; a"
            + " hromada alert carries its raion's polygon and geometryApproximate. Needs your own alerts.in.ua API"
            + " token (request form at alerts.in.ua/api-request); the token stays on this server. Disclaimer from"
            + " the alerts.in.ua developer docs: do not use the API for critical infrastructure; access and use are at"
            + " your own discretion and risk, and the service accepts no liability for any damage from using its"
            + " information or for outages. The notes field is the service's free text and is stored as untrusted"
            + " text.";

    private final URI active;
    private final UaRegionIndex regions;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value ConflictSources#BASE_URL_PROPERTY}; blank in production
     * @param regions the Ukraine region index
     */
    @Autowired
    public AlertsInUaAdapter(@Value("${" + ConflictSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            UaRegionIndex regions) {
        this.active = ConflictSources.uri(ACTIVE_URL, baseUrlOverride);
        this.regions = regions;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The active alerts URL. */
    public URI activeUri() {
        return active;
    }

    /**
     * Reads the alerts in force, conditionally, and maps them with their tombstones.
     *
     * @throws CredentialsRejectedException when the token is missing or refused (401)
     * @throws AdapterException when the request fails otherwise
     * @throws UpstreamFormatException when the body does not parse (its validators are forgotten)
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        String token = ConflictSources.trimmed(ctx.credentials().get(TOKEN));
        if (token == null) {
            throw new CredentialsRejectedException(ID + " needs the " + TOKEN + " credential (an alerts.in.ua API token)");
        }
        UpstreamResponse response;
        try {
            response = ctx.http().getConditional(active, STATE_KEY, Map.of("Authorization", "Bearer " + token));
        } catch (AdapterException e) {
            if (e.httpStatus().orElse(0) == 401) {
                throw new CredentialsRejectedException("alerts.in.ua refused the token (HTTP 401); check the "
                        + TOKEN + " credential", e);
            }
            throw e;
        }
        if (response.notModified()) {
            return FetchResult.unchanged();
        }
        Instant now = ctx.now();
        AlertsInUaParser.Result parsed;
        try {
            parsed = AlertsInUaParser.parse(ConflictSources.body(ctx, response, STATE_KEY, active), regions, now);
        } catch (UpstreamFormatException e) {
            SourceHttp.forgetValidators(ctx.state(), STATE_KEY);
            throw e;
        }
        List<NormalizedRecord> records = new ArrayList<>(parsed.records());
        for (Map.Entry<String, List<String>> scope : parsed.activeIds().entrySet()) {
            List<String> gone = AbsenceTombstoner.update(ctx.state(), scope.getKey(),
                    AbsenceTombstoner.Outcome.COMPLETE, scope.getValue());
            records.addAll(AbsenceTombstoner.tombstones(gone, scope.getKey(), now));
        }
        if (!parsed.skipped().isEmpty()) {
            ctx.log().debug("{}: skipped {}", ID, parsed.skipped());
        }
        return FetchResult.complete(records);
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("Ukraine alerts (alerts.in.ua)")
                .description(DESCRIPTION)
                .kinds(AlertsInUaParser.AIR_RAID_KIND, AlertsInUaParser.THREAT_KIND)
                .homepage("https://alerts.in.ua")
                .attribution("alerts.in.ua")
                .licence("unspecified (alerts.in.ua)")
                .termsUrl("https://devs.alerts.in.ua/")
                .mode(Mode.POLLING)
                .credential(CredentialField.required(TOKEN, "Your alerts.in.ua API token (request it at"
                        + " https://alerts.in.ua/api-request); sent as Authorization: Bearer"))
                .interval(Duration.ofSeconds(20), Duration.ofSeconds(30))
                .defaultBudget(Budget.of(3, 4320))
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(IngestPolicy.all())
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .defaultRetention(ConflictSources.EVENT_RETENTION)
                .kindRetention(AlertsInUaParser.AIR_RAID_KIND, ConflictSources.EVENT_RETENTION)
                .kindRetention(AlertsInUaParser.THREAT_KIND, ConflictSources.EVENT_RETENTION)
                .zeroResultsNormal(true)
                .egress(Egress.OPEN)
                .titleField("locationEn")
                .tileProperties("alertType", "alertLevel", "location")
                .searchFields("location", "locationEn", "oblast", "raion", "notes")
                .embedFields("notes");
        ConflictSources.text(b, "location", "Location", "The alert's location (Ukrainian)");
        ConflictSources.text(b, "locationEn", "Location (English)", "The alert's location in English");
        ConflictSources.text(b, "locationType", "Location type", "oblast, raion, city, hromada or unknown");
        ConflictSources.text(b, "oblast", "Oblast", "The oblast of the location");
        ConflictSources.text(b, "raion", "Raion", "The raion of the location");
        ConflictSources.text(b, "alertType", "Alert type", "air_raid, artillery_shelling, urban_fights, chemical or"
                + " nuclear");
        ConflictSources.text(b, "alertLevel", "Alert level", "red or yellow");
        ConflictSources.array(b, "threatTypes", "Threats", "The open threat types (drones, ballistic_missiles, …),"
                + " sorted");
        ConflictSources.text(b, "notes", "Notes", "The service's note (untrusted text, at most 1 KB)");
        ConflictSources.bool(b, "geometryApproximate", "Approximate area", "True when the polygon is the raion that"
                + " contains the alert's hromada or city");
        b.kindDisplay(AlertsInUaParser.AIR_RAID_KIND, new KindDisplay("{locationEn}: air raid", "warning", "#C92A2A",
                null));
        b.kindDisplay(AlertsInUaParser.THREAT_KIND, new KindDisplay("{locationEn}: {alertType}", "danger", "#E8590C",
                null));
        return b.build();
    }
}
