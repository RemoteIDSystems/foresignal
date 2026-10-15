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
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code uk-sanctions-list} (prompt 091): the FCDO UK Sanctions List, which replaced the OFSI Consolidated
 * List (closed 28 January 2026). The list is 22 MB with {@code ETag}/{@code Last-Modified}, so it is
 * change-checked with a conditional GET, downloaded with a cap and parsed from disk.
 *
 * <p>{@code NameType = Primary Name} is the record name; the rest are aliases. {@code UNReferenceNumber}
 * becomes the {@code unReference} entity key so an entity links to the UN list.
 */
@Component
public final class UkSanctionsAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "uk-sanctions-list";

    /** The default base URL. */
    public static final String DEFAULT_BASE_URL = "https://sanctionslist.fcdo.gov.uk";

    /** Test-only base override. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.uk-sanctions-list.base-url";

    static final String STATE_KEY = "uk:list";
    static final String IDS_KEY = "uk:ids";
    static final Duration RETENTION = Duration.ofDays(1825);
    static final DateTimeFormatter UK_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final URI baseUrl;
    private final SourceDescriptor descriptor;

    /** Creates the adapter. */
    @Autowired
    public UkSanctionsAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.baseUrl = URI.create(baseUrlOverride == null || baseUrlOverride.isBlank()
                ? DEFAULT_BASE_URL : baseUrlOverride.strip());
        this.descriptor = buildDescriptor();
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        UpstreamResponse response = ctx.http().getConditional(
                URI.create(baseUrl + "/docs/UK-Sanctions-List.xml"), STATE_KEY);
        if (response.notModified()) {
            return FetchResult.unchanged();
        }
        if (response.status() != 200) {
            throw new UpstreamFormatException("The UK Sanctions List answered HTTP " + response.status(), "");
        }
        Path file = Path.of("uk-sanctions-" + System.nanoTime() + ".xml");
        // A relative name resolves under the raster temp directory; parse the reported file.
        ai.foresignal.source.spi.DownloadResult downloaded =
                ctx.http().download(URI.create(baseUrl + "/docs/UK-Sanctions-List.xml"), file, 100 * 1024 * 1024);
        List<NormalizedRecord> records;
        try {
            records = parse(downloaded.file());
        } catch (IOException | XMLStreamException e) {
            throw new UpstreamFormatException("The UK Sanctions List is not valid XML", "", e);
        }
        Set<String> previous = new LinkedHashSet<>();
        ctx.state().get(IDS_KEY).orElse("").lines().filter(s -> !s.isBlank()).forEach(previous::add);
        Set<String> current = new LinkedHashSet<>();
        records.forEach(r -> current.add(r.externalId()));
        List<NormalizedRecord> out = new ArrayList<>(records);
        if (DelistingGuard.implausible(previous.size(), current.size())) {
            ctx.state().degrade(DelistingGuard.SHRINK_REASON);
        } else {
            for (String id : DelistingGuard.gone(previous, current)) {
                out.add(NormalizedRecord.tombstone(id, Sanctions.KIND, ctx.now()));
            }
            ctx.state().clearDegraded();
        }
        ctx.state().put(IDS_KEY, String.join("\n", DelistingGuard.sorted(current)));
        return FetchResult.complete(out);
    }

    /** The records of the UK Sanctions List file. */
    public List<NormalizedRecord> parse(Path file) throws IOException, XMLStreamException {
        List<NormalizedRecord> out = new ArrayList<>();
        Instant generated = null;
        try (var in = java.nio.file.Files.newInputStream(file)) {
            XMLStreamReader reader = Xml.open(in);
            while (reader.hasNext()) {
                reader.next();
                if (Xml.at(reader, "DateGenerated")) {
                    generated = date(reader.getElementText());
                } else if (Xml.at(reader, "Designation")) {
                    NormalizedRecord record = map(Xml.readElement(reader), generated);
                    if (record != null) {
                        out.add(record);
                    }
                }
            }
        }
        return out;
    }

    private static NormalizedRecord map(Xml.Node designation, Instant generated) {
        String uniqueId = designation.childText("UniqueID");
        if (uniqueId == null) {
            return null;
        }
        String name = null;
        List<String> aliases = new ArrayList<>();
        for (Xml.Node names : designation.all("Names")) {
            for (Xml.Node nameNode : names.all("Name")) {
                String value = fullName(nameNode);
                if (value == null) {
                    continue;
                }
                if ("Primary Name".equalsIgnoreCase(nameNode.childText("NameType")) && name == null) {
                    name = value;
                } else {
                    aliases.add(value);
                }
            }
        }
        List<String> nationalities = designation.leafTexts("Nationality");
        String unReference = designation.childText("UNReferenceNumber");
        String imo = null;
        for (String candidate : designation.leafTexts("ShipIMO")) {
            if (Sanctions.isImo(candidate)) {
                imo = Sanctions.digits(candidate);
            }
        }
        if (imo == null) {
            for (String candidate : designation.leafTexts("IMO")) {
                if (Sanctions.isImo(candidate)) {
                    imo = Sanctions.digits(candidate);
                }
            }
        }

        NormalizedRecord.Builder b = NormalizedRecord.builder("uk:" + uniqueId, Sanctions.KIND)
                .observedAt(generated)
                .eventTime(date(designation.childText("DateDesignated")));
        b.put("list", "UK");
        b.put("name", name != null ? name : (aliases.isEmpty() ? null : aliases.get(0)));
        b.put("partyType", Sanctions.partyType(designation.childText("IndividualEntityShip")));
        b.put("programs", single(designation.childText("RegimeName")));
        b.put("aliases", Sanctions.cap(aliases, Sanctions.MAX_ALIASES, Sanctions.MAX_ALIAS_CHARS));
        b.put("nationalities", Sanctions.cap(nationalities, Sanctions.MAX_IDENTIFIERS, Sanctions.MAX_ALIAS_CHARS));
        b.put("remarks", Sanctions.truncate(designation.childText("OtherInformation"), Sanctions.MAX_REMARKS));
        b.put("lastUpdated", designation.childText("LastUpdated"));
        b.put("imo", imo);
        if (unReference != null && !unReference.isBlank()) {
            b.put("unReference", unReference.strip());
            b.put("crossRefs", List.of(unReference.strip()));
        }
        return b.build();
    }

    private static List<String> single(String value) {
        return value == null || value.isBlank() ? List.of() : List.of(value.strip());
    }

    private static String fullName(Xml.Node name) {
        StringBuilder out = new StringBuilder();
        for (int i = 1; i <= 6; i++) {
            String part = name.childText("Name" + i);
            if (part != null && !part.isBlank()) {
                if (!out.isEmpty()) {
                    out.append(' ');
                }
                out.append(part.strip());
            }
        }
        if (out.isEmpty()) {
            String latin = name.childText("NameNonLatinScript");
            if (latin != null) {
                return latin.strip();
            }
            return null;
        }
        return out.toString();
    }

    static Instant date(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(text.strip(), UK_DATE).atStartOfDay(ZoneOffset.UTC).toInstant();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static SourceDescriptor buildDescriptor() {
        return SourceDescriptor.builder(ID)
                .name("UK Sanctions List")
                .description("The FCDO UK Sanctions List, the single source of UK designations since the OFSI "
                        + "Consolidated List closed on 28 January 2026. A separate list still applies to specific "
                        + "Russia financial and investment restrictions; it is not ingested here.")
                .kind(Sanctions.KIND)
                .mode(Mode.POLLING)
                .homepage("https://www.gov.uk/government/publications/the-uk-sanctions-list")
                .attribution("UK Foreign, Commonwealth & Development Office")
                .licence("Open Government Licence v3.0")
                .termsUrl("https://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/")
                .interval(Duration.ofHours(6), Duration.ofHours(6))
                .defaultBudget(2, 20)
                .maxResponseBytes(100 * 1024 * 1024)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .entityKey("imo", "imo")
                .entityKey("un-reference", "unReference")
                .titleField("name")
                .searchFields("name", "aliases", "identifiers", "programs")
                .significantFields("lastUpdated")
                .zeroResultsNormal(false)
                .kindDisplay(Sanctions.KIND, new KindDisplay("{name}", "danger", "#C92A2A", null))
                .build();
    }
}
