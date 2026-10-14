package ai.foresignal.source.adapters.sanctions;

import ai.foresignal.source.spi.AdapterException;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code ofac-sdn} (prompt 091): the US Treasury OFAC SDN and Consolidated (Non-SDN) lists.
 *
 * <p>The legacy {@code SDN.XML}/{@code CONS_PRIM.XML} schema is used rather than {@code SDN_ADVANCED.XML}:
 * it carries the same entries with a far simpler structure. The export URL answers 302 to a pre-signed S3
 * object; the publication path is compared (query stripped) so an unchanged publication costs one request,
 * and the pre-signed query is never logged. Downloads are capped at {@value #MAX_BYTES} bytes and parsed from
 * disk with secure StAX.
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

    /** Hosts a redirect may land on (the pre-signed S3 object). */
    static final List<String> REDIRECT_HOSTS = List.of("s3.us-gov-west-1.amazonaws.com", "s3.amazonaws.com");

    private final URI baseUrl;
    private final SourceDescriptor descriptor;

    /** Creates the adapter. */
    @Autowired
    public OfacSdnAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.baseUrl = URI.create(baseUrlOverride == null || baseUrlOverride.isBlank()
                ? DEFAULT_BASE_URL : baseUrlOverride.strip());
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
        List<NormalizedRecord> records = new ArrayList<>();
        for (String list : lists) {
            records.addAll(fetchList(ctx, list));
        }
        return FetchResult.complete(records);
    }

    private List<NormalizedRecord> fetchList(FetchContext ctx, String list) throws AdapterException {
        String listId = "consolidated".equals(list) ? CONSOLIDATED : SDN;
        URI export = URI.create(baseUrl + "/api/PublicationPreview/exports/"
                + ("consolidated".equals(list) ? "CONS_PRIM.XML" : "SDN.XML"));
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
            String stateKey = "ofac:path:" + listId;
            if (path.equals(ctx.state().get(stateKey).orElse(null))) {
                return List.of();
            }
            ctx.state().put(stateKey, path);
            target = redirect;
        } else if (response.status() != 200) {
            throw new UpstreamFormatException("The OFAC export answered HTTP " + response.status(), "");
        }
        Path file = Path.of("ofac-" + listId + "-" + System.nanoTime() + ".xml");
        // The download is charged against the byte budget; the file is parsed from disk.
        ctx.http().download(target, file, MAX_BYTES);
        List<NormalizedRecord> records;
        try {
            records = parse(file, listId);
        } catch (IOException | XMLStreamException e) {
            throw new UpstreamFormatException("The OFAC " + listId + " export is not valid XML", "", e);
        }
        return guard(ctx, listId, records, Instant.now());
    }

    private static void requireAllowedRedirect(URI redirect) throws AdapterException {
        String host = redirect.getHost() == null ? "" : redirect.getHost();
        for (String allowed : REDIRECT_HOSTS) {
            if (host.equals(allowed) || host.endsWith("." + allowed)) {
                return;
            }
        }
        throw new AdapterException("The OFAC export redirected to an unapproved host");
    }

    private static String pathOf(URI uri) {
        return uri.getScheme() + "://" + uri.getAuthority() + uri.getPath();
    }

    private List<NormalizedRecord> guard(FetchContext ctx, String listId, List<NormalizedRecord> records,
            Instant observedAt) {
        String stateKey = "ofac:ids:" + listId;
        Set<String> previous = new LinkedHashSet<>();
        ctx.state().get(stateKey).orElse("").lines().filter(s -> !s.isBlank()).forEach(previous::add);
        Set<String> current = new LinkedHashSet<>();
        for (NormalizedRecord record : records) {
            current.add(record.externalId());
        }
        List<NormalizedRecord> out = new ArrayList<>(records);
        if (DelistingGuard.implausible(previous.size(), current.size())) {
            ctx.state().degrade(DelistingGuard.SHRINK_REASON);
        } else {
            for (String id : DelistingGuard.gone(previous, current)) {
                out.add(NormalizedRecord.tombstone(id, Sanctions.KIND, observedAt));
            }
            ctx.state().clearDegraded();
        }
        ctx.state().put(stateKey, String.join("\n", DelistingGuard.sorted(current)));
        return out;
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
                if (upper.contains("IMO") && Sanctions.isImo(value)) {
                    imo = Sanctions.digits(value);
                } else if (upper.contains("MMSI") && Sanctions.isMmsi(value)) {
                    mmsi = Sanctions.digits(value);
                } else if (upper.contains("AIRCRAFT") && upper.contains("TAIL")) {
                    aircraftTail = value;
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
        String dateOfBirth = entry.path("dateOfBirthList", "dateOfBirthItem", "dateOfBirth");
        Xml.Node vessel = entry.first("vesselInfo");
        String callSign = vessel == null ? null : vessel.childText("callSign");
        String vesselFlag = vessel == null ? null : vessel.childText("vesselFlag");

        NormalizedRecord.Builder b = NormalizedRecord.builder(listId + ":" + uid, Sanctions.KIND)
                .observedAt(publish);
        b.put("list", listId);
        b.put("name", name);
        b.put("partyType", Sanctions.partyType(entry.childText("sdnType")));
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
                .entityKey("aircraft-tail", "aircraftTail")
                .titleField("name")
                .searchFields("name", "aliases", "identifiers", "programs")
                .significantFields("lastUpdated")
                .zeroResultsNormal(false)
                .kindDisplay(Sanctions.KIND, new KindDisplay("{name}", "danger", "#C92A2A", null))
                .build();
    }
}
