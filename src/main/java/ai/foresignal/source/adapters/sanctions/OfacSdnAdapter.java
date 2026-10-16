package ai.foresignal.source.adapters.sanctions;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.DownloadResult;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code ofac-sdn} (prompt 091): the US Treasury OFAC SDN and Consolidated (Non-SDN) lists.
 *
 * <p>The legacy {@code SDN.XML}/{@code CONS_PRIM.XML} schema is used rather than {@code SDN_ADVANCED.XML}:
 * it carries the same entries with a far simpler structure. The Consolidated (Non-SDN) list in that schema is
 * {@code CONSOLIDATED.XML} (verified 2026-10-16: {@code CONS_PRIM.XML} answers 200 with an empty body). The
 * export URL answers 302 to a pre-signed S3 object; the publication path is compared (query stripped) so an
 * unchanged publication costs one request, and a downloaded file whose SHA-256 matches the last one is not
 * re-ingested. The pre-signed query is masked wherever a URL is stored or logged ({@code UrlRedactor}).
 * Downloads are capped at {@value #MAX_BYTES} bytes, parsed from disk with secure StAX and then deleted.
 *
 * <p>A run whose every list is unchanged returns {@link FetchResult#unchanged()}. Delistings go through
 * {@link ListReconciler} (tombstones, the shrink guard and its {@code allowShrink} override).
 */
@Component
public final class OfacSdnAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "ofac-sdn";

    /** The list id of the SDN list. */
    public static final String SDN = "SDN";

    /** The list id of the Consolidated (Non-SDN) list. */
    public static final String CONSOLIDATED = "OFAC-CONS";

    /** The default base URL. */
    public static final String DEFAULT_BASE_URL = "https://sanctionslistservice.ofac.treas.gov";

    /** Test-only base override. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.ofac-sdn.base-url";

    /** The byte cap: the SDN export was about 29 MB (2026-10-14). */
    public static final int MAX_BYTES = 200 * 1024 * 1024;

    static final Duration RETENTION = Duration.ofDays(1825);
    static final DateTimeFormatter SDN_DATE = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    /**
     * The host a redirect may land on: the pre-signed S3 object in {@code us-gov-west-1} (verified 2026-10-16:
     * {@code wc2h-sls-prod-public-published.s3.us-gov-west-1.amazonaws.com}), or a subdomain of it.
     */
    static final String REDIRECT_HOST = "s3.us-gov-west-1.amazonaws.com";

    /** The adapter-state key prefix of a list's last publication path. */
    static final String PATH_KEY_PREFIX = "ofac:path:";

    /** The adapter-state key prefix of a list's last downloaded SHA-256. */
    static final String SHA_KEY_PREFIX = "ofac:sha:";

    /** The adapter-state key prefix of a list's previous id set. */
    static final String IDS_KEY_PREFIX = "ofac:ids:";

    /** The registration id form of an aircraft name observed in SDN.XML ({@code EP-GOL}). */
    static final java.util.regex.Pattern REGISTRATION = java.util.regex.Pattern.compile("^[A-Z0-9]{1,2}-[A-Z0-9]{2,5}$");

    private final URI baseUrl;
    private final ListReconciler reconciler;
    private final SourceDescriptor descriptor;

    /** Creates the adapter without a notification publisher (tests). */
    public OfacSdnAdapter(String baseUrlOverride) {
        this(baseUrlOverride, null);
    }

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride test-only base URL, blank for the real one
     * @param events publishes the shrink-guard system notification; may be null
     */
    @Autowired
    public OfacSdnAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            ApplicationEventPublisher events) {
        this.baseUrl = URI.create(baseUrlOverride == null || baseUrlOverride.isBlank()
                ? DEFAULT_BASE_URL : baseUrlOverride.strip());
        this.reconciler = new ListReconciler(events);
        this.descriptor = buildDescriptor();
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public Map<String, String> checkParams(ai.foresignal.source.spi.ValidatedParams params) {
        Map<String, String> problems = new java.util.LinkedHashMap<>();
        for (String list : params.stringList("lists")) {
            if (!"sdn".equals(list) && !"consolidated".equals(list)) {
                problems.put("lists", "allowed values are sdn and consolidated, not " + list);
            }
        }
        return problems;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        List<String> lists = ctx.params().stringList("lists");
        if (lists.isEmpty()) {
            lists = List.of("sdn");
        }
        List<ListReconciler.Outcome> outcomes = new ArrayList<>();
        for (String list : lists) {
            ListReconciler.Outcome outcome = fetchList(ctx, list);
            if (outcome != null) {
                outcomes.add(outcome);
            }
        }
        if (outcomes.isEmpty()) {
            // Every list's publication is the one already ingested: not modified, not "zero records".
            return FetchResult.unchanged();
        }
        ListReconciler.applyHealth(ctx.state(), outcomes);
        List<NormalizedRecord> records = new ArrayList<>();
        outcomes.forEach(outcome -> records.addAll(outcome.records()));
        return FetchResult.complete(records);
    }

    /** The export file name of a {@code lists} value. */
    static String exportName(String list) {
        return "consolidated".equals(list) ? "CONSOLIDATED.XML" : "SDN.XML";
    }

    /**
     * Fetches one list, or returns null when its publication is unchanged: the redirect's publication path
     * (query stripped) equals the stored one, or the downloaded bytes hash to the stored SHA-256 (the
     * consolidated export moves to a new publication path whenever any list is republished).
     */
    private ListReconciler.Outcome fetchList(FetchContext ctx, String list) throws AdapterException {
        String listId = "consolidated".equals(list) ? CONSOLIDATED : SDN;
        URI export = URI.create(baseUrl + "/api/PublicationPreview/exports/" + exportName(list));
        UpstreamResponse response = ctx.http().getNoFollow(export);
        URI target = export;
        if (response.status() >= 300 && response.status() < 400) {
            String location = response.header("location");
            if (location == null || location.isBlank()) {
                throw new UpstreamFormatException("The OFAC export returned " + response.status()
                        + " without a Location", "");
            }
            URI redirect = URI.create(location);
            requireAllowedRedirect(redirect);
            String path = pathOf(redirect);
            String stateKey = PATH_KEY_PREFIX + listId;
            if (path.equals(ctx.state().get(stateKey).orElse(null))) {
                return null;
            }
            // Committed with the rest of the run's state only when the fetch succeeds (framework rule).
            ctx.state().put(stateKey, path);
            target = redirect;
        } else if (response.status() != 200) {
            throw new UpstreamFormatException("The OFAC export answered HTTP " + response.status(), "");
        }
        Path file = Path.of("ofac-" + listId + "-" + System.nanoTime() + ".xml");
        // The download is charged against the byte budget; a relative name is resolved under the
        // raster temp directory, so parse the file the download reports, not the requested name.
        DownloadResult downloaded = ctx.http().download(target, file, MAX_BYTES);
        try {
            String shaKey = SHA_KEY_PREFIX + listId;
            if (downloaded.sha256() != null && downloaded.sha256().equals(ctx.state().get(shaKey).orElse(null))) {
                return null;
            }
            List<NormalizedRecord> records;
            try {
                records = parse(downloaded.file(), listId);
            } catch (IOException | XMLStreamException e) {
                throw new UpstreamFormatException("The OFAC " + listId + " export is not valid XML", "", e);
            }
            if (downloaded.sha256() != null) {
                ctx.state().put(shaKey, downloaded.sha256());
            }
            return reconciler.reconcile(ctx, IDS_KEY_PREFIX + listId, listId, records);
        } finally {
            Sanctions.deleteQuietly(downloaded.file());
        }
    }

    private void requireAllowedRedirect(URI redirect) throws AdapterException {
        String host = redirect.getHost() == null ? "" : redirect.getHost();
        // A same-host redirect (the configured base, or a test rebasing) is always allowed; otherwise
        // the redirect must land on the reviewed pre-signed S3 host in us-gov-west-1.
        if (host.equals(baseUrl.getHost()) || host.equals(REDIRECT_HOST) || host.endsWith("." + REDIRECT_HOST)) {
            return;
        }
        throw new AdapterException("The OFAC export redirected to an unapproved host");
    }

    private static String pathOf(URI uri) {
        return uri.getScheme() + "://" + uri.getAuthority() + uri.getPath();
    }

    /** The records of one exported list file. */
    public List<NormalizedRecord> parse(Path file, String listId) throws IOException, XMLStreamException {
        List<NormalizedRecord> out = new ArrayList<>();
        Instant publish = null;
        try (var in = java.nio.file.Files.newInputStream(file)) {
            XMLStreamReader reader = Xml.open(in);
            while (reader.hasNext()) {
                reader.next();
                if (Xml.at(reader, "Publish_Date")) {
                    publish = date(reader.getElementText());
                } else if (Xml.at(reader, "sdnEntry")) {
                    Xml.Node entry = Xml.readElement(reader);
                    NormalizedRecord record = map(entry, listId, publish);
                    if (record != null) {
                        out.add(record);
                    }
                }
            }
        }
        return out;
    }

    private static NormalizedRecord map(Xml.Node entry, String listId, Instant publish) {
        String uid = entry.childText("uid");
        if (uid == null) {
            return null;
        }
        String name = join(entry.childText("firstName"), entry.childText("lastName"));
        if (name == null) {
            name = entry.childText("lastName");
        }
        List<String> aliases = new ArrayList<>();
        for (Xml.Node aka : entry.all("akaList").stream().flatMap(n -> n.all("aka").stream()).toList()) {
            String alias = join(aka.childText("firstName"), aka.childText("lastName"));
            if (alias != null) {
                aliases.add(alias);
            }
        }
        List<Map<String, Object>> identifiers = new ArrayList<>();
        List<String> nationalities = new ArrayList<>();
        List<Map<String, Object>> addresses = new ArrayList<>();
        String imo = null;
        String mmsi = null;
        String aircraftTail = null;
        for (Xml.Node idList : entry.all("idList")) {
            for (Xml.Node id : idList.all("id")) {
                String type = id.childText("idType");
                String value = id.childText("idNumber");
                identifiers.add(Sanctions.identifier(type, value, id.childText("idCountry")));
                String upper = type == null ? "" : type.toUpperCase(java.util.Locale.ROOT);
                String valueUpper = value == null ? "" : value.toUpperCase(java.util.Locale.ROOT).strip();
                // OFAC often labels an IMO as "Vessel Registration Identification" with the number
                // itself prefixed "IMO " (for example "IMO 7406784"), so accept either.
                if ((upper.contains("IMO") || valueUpper.startsWith("IMO")) && Sanctions.isImo(value)) {
                    imo = Sanctions.digits(value);
                } else if (upper.contains("MMSI") && Sanctions.isMmsi(value)) {
                    mmsi = Sanctions.digits(value);
                } else if ("AIRCRAFT TAIL NUMBER".equals(upper.strip()) && value != null && !value.isBlank()) {
                    // the current tail; "Previous Aircraft Tail Number" is history, not a key
                    aircraftTail = value.strip();
                }
            }
        }
        for (Xml.Node list : entry.all("nationalityList")) {
            for (Xml.Node nationality : list.all("nationality")) {
                if (nationality.text() != null) {
                    nationalities.add(nationality.text());
                }
            }
        }
        for (Xml.Node list : entry.all("addressList")) {
            for (Xml.Node address : list.all("address")) {
                Map<String, Object> place = new java.util.LinkedHashMap<>();
                place.put("city", address.childText("city"));
                place.put("country", address.childText("country"));
                addresses.add(place);
            }
        }
        String partyType = Sanctions.partyType(entry.childText("sdnType"));
        String lastName = entry.childText("lastName");
        if (aircraftTail == null && "aircraft".equals(partyType) && lastName != null
                && REGISTRATION.matcher(lastName.strip()).matches()) {
            // Most aircraft entries without an "Aircraft Tail Number" id are named by their registration
            // (178 of 342 in the 2026-10-15 export, for example EP-GOL)
            aircraftTail = lastName.strip();
        }
        String dateOfBirth = entry.path("dateOfBirthList", "dateOfBirthItem", "dateOfBirth");
        Xml.Node vessel = entry.first("vesselInfo");
        String callSign = vessel == null ? null : vessel.childText("callSign");
        String vesselFlag = vessel == null ? null : vessel.childText("vesselFlag");

        NormalizedRecord.Builder b = NormalizedRecord.builder(listId + ":" + uid, Sanctions.KIND)
                .observedAt(publish);
        b.put("list", listId);
        b.put("name", name);
        b.put("partyType", partyType);
        b.put("programs", programs(entry));
        b.put("aliases", Sanctions.cap(aliases, Sanctions.MAX_ALIASES, Sanctions.MAX_ALIAS_CHARS));
        b.put("nationalities", Sanctions.cap(nationalities, Sanctions.MAX_IDENTIFIERS, Sanctions.MAX_ALIAS_CHARS));
        b.put("dateOfBirth", dateOfBirth);
        b.put("identifiers", identifiers.size() > Sanctions.MAX_IDENTIFIERS
                ? identifiers.subList(0, Sanctions.MAX_IDENTIFIERS) : identifiers);
        b.put("addresses", addresses.size() > Sanctions.MAX_ADDRESSES
                ? addresses.subList(0, Sanctions.MAX_ADDRESSES) : addresses);
        b.put("remarks", Sanctions.truncate(entry.childText("remarks"), Sanctions.MAX_REMARKS));
        b.put("imo", imo);
        b.put("mmsi", mmsi);
        b.put("callSign", callSign);
        b.put("vesselFlag", vesselFlag);
        b.put("aircraftTail", aircraftTail);
        return b.build();
    }

    private static List<String> programs(Xml.Node entry) {
        List<String> out = new ArrayList<>();
        for (Xml.Node list : entry.all("programList")) {
            for (Xml.Node program : list.all("program")) {
                if (program.text() != null) {
                    out.add(program.text());
                }
            }
        }
        return out;
    }

    private static String join(String first, String last) {
        String f = first == null ? "" : first.strip();
        String l = last == null ? "" : last.strip();
        String value = (f + " " + l).strip();
        return value.isEmpty() ? null : value;
    }

    /** {@code MM/DD/YYYY} as a UTC midnight instant, or null. */
    static Instant date(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(text.strip(), SDN_DATE).atStartOfDay(ZoneOffset.UTC).toInstant();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static SourceDescriptor buildDescriptor() {
        return SourceDescriptor.builder(ID)
                .name("OFAC sanctions lists")
                .description("US Treasury OFAC Specially Designated Nationals (SDN) and Consolidated (Non-SDN) "
                        + "lists, from the OFAC Sanctions List Service legacy XML export. A delisting is a "
                        + "tombstone, unless the list shrank implausibly (LIST_SHRINK_GUARD).")
                .kind(Sanctions.KIND)
                .mode(Mode.POLLING)
                .homepage("https://ofac.treasury.gov/sanctions-list-service")
                .attribution("US Department of the Treasury, Office of Foreign Assets Control")
                .licence("US Government work (public domain)")
                .termsUrl("https://ofac.treasury.gov/faqs")
                .param(ParamSpec.optional("lists", ParamType.STRING_LIST, "Lists to fetch: sdn, consolidated",
                        List.of("sdn")))
                .interval(Duration.ofHours(6), Duration.ofHours(6))
                .defaultBudget(4, 40)
                .maxResponseBytes(MAX_BYTES)
                .fetchTimeout(Duration.ofMinutes(5))
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .entityKey("imo", "imo")
                .entityKey("mmsi", "mmsi")
                .entityKey("registration", "aircraftTail")
                .titleField("name")
                .searchFields("name", "aliases", "identifiers", "programs")
                .significantFields("lastUpdated")
                .zeroResultsNormal(false)
                .kindDisplay(Sanctions.KIND, new KindDisplay("{name}", "danger", "#C92A2A", null))
                .build();
    }
}
