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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code uk-sanctions-list} (prompt 091): the FCDO UK Sanctions List, which replaced the OFSI Consolidated
 * List (closed 28 January 2026). The list is 22 MB with {@code ETag}/{@code Last-Modified}, so it is fetched
 * with one conditional GET (capped at the descriptor's {@code maxResponseBytes}) and stream-parsed from the
 * response body; an unchanged list costs one 304.
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
    private final ListReconciler reconciler;
    private final SourceDescriptor descriptor;

    /** Creates the adapter without a notification publisher (tests). */
    public UkSanctionsAdapter(String baseUrlOverride) {
        this(baseUrlOverride, null);
    }

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride test-only base URL, blank for the real one
     * @param events publishes the shrink-guard system notification; may be null
     */
    @Autowired
    public UkSanctionsAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride,
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
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        // One request: a conditional GET (the list is about 22 MB with ETag/Last-Modified). Its validators are
        // committed with the run's state only when the fetch succeeds (framework rule).
        UpstreamResponse response = ctx.http().getConditional(
                URI.create(baseUrl + "/docs/UK-Sanctions-List.xml"), STATE_KEY);
        if (response.notModified()) {
            return FetchResult.unchanged();
        }
        if (response.status() != 200) {
            throw new UpstreamFormatException("The UK Sanctions List answered HTTP " + response.status(), "");
        }
        List<NormalizedRecord> records;
        try {
            records = parse(new ByteArrayInputStream(response.body()));
        } catch (IOException | XMLStreamException e) {
            throw new UpstreamFormatException("The UK Sanctions List is not valid XML", "", e);
        }
        ListReconciler.Outcome outcome = reconciler.reconcile(ctx, IDS_KEY, "UK", records);
        ListReconciler.applyHealth(ctx.state(), List.of(outcome));
        return FetchResult.complete(outcome.records());
    }

    /** The records of the UK Sanctions List file. */
    public List<NormalizedRecord> parse(Path file) throws IOException, XMLStreamException {
        try (InputStream in = java.nio.file.Files.newInputStream(file)) {
            return parse(in);
        }
    }

    /** The records of a UK Sanctions List document, stream-parsed. */
    public List<NormalizedRecord> parse(InputStream in) throws IOException, XMLStreamException {
        List<NormalizedRecord> out = new ArrayList<>();
        Instant generated = null;
        XMLStreamReader reader = Xml.open(in);
        try {
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
        } finally {
            reader.close();
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
                // "Primary Name" for people and entities, "Primary name" for ships (2026-10-16 capture)
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
        List<Map<String, Object>> identifiers = new ArrayList<>();
        for (String candidate : designation.leafTexts("IMONumber")) {
            identifiers.add(Sanctions.identifier("IMO", candidate, null));
            if (imo == null && Sanctions.isImo(candidate)) {
                imo = Sanctions.digits(candidate);
            }
        }
        for (String passport : designation.leafTexts("PassportNumber")) {
            identifiers.add(Sanctions.identifier("Passport", passport, null));
        }
        for (String national : designation.leafTexts("NationalIdentifierNumber")) {
            identifiers.add(Sanctions.identifier("National Identifier", national, null));
        }
        for (String registration : designation.leafTexts("BusinessRegistrationNumber")) {
            identifiers.add(Sanctions.identifier("Business Registration Number", registration, null));
        }
        List<Map<String, Object>> addresses = new ArrayList<>();
        for (Xml.Node address : designation.descendants("Address")) {
            Map<String, Object> place = new LinkedHashMap<>();
            // AddressLine5 holds the town when present; otherwise AddressLine6 holds the town or the
            // province (2026-10-16 capture). The schema has no dedicated city element.
            String city = address.childText("AddressLine5");
            Sanctions.putIfPresent(place, "city", city != null ? city : address.childText("AddressLine6"));
            Sanctions.putIfPresent(place, "country", address.childText("AddressCountry"));
            if (!place.isEmpty()) {
                addresses.add(place);
            }
        }
        List<String> dobs = designation.leafTexts("DOB");
        List<String> flags = designation.leafTexts("CurrentBelievedFlagOfShip");
        Instant designated = date(designation.childText("DateDesignated"));

        NormalizedRecord.Builder b = NormalizedRecord.builder("uk:" + uniqueId, Sanctions.KIND)
                .observedAt(generated)
                .eventTime(designated);
        b.put("list", "UK");
        b.put("name", name != null ? name : (aliases.isEmpty() ? null : aliases.get(0)));
        b.put("partyType", Sanctions.partyType(designation.childText("IndividualEntityShip")));
        b.put("programs", single(designation.childText("RegimeName")));
        b.put("aliases", Sanctions.cap(aliases, Sanctions.MAX_ALIASES, Sanctions.MAX_ALIAS_CHARS));
        b.put("nationalities", Sanctions.cap(nationalities, Sanctions.MAX_IDENTIFIERS, Sanctions.MAX_ALIAS_CHARS));
        b.put("dateOfBirth", dobs.isEmpty() ? null : dobs.get(0));
        b.put("identifiers", identifiers.size() > Sanctions.MAX_IDENTIFIERS
                ? identifiers.subList(0, Sanctions.MAX_IDENTIFIERS) : identifiers);
        b.put("addresses", addresses.size() > Sanctions.MAX_ADDRESSES
                ? addresses.subList(0, Sanctions.MAX_ADDRESSES) : addresses);
        b.put("remarks", Sanctions.truncate(designation.childText("OtherInformation"), Sanctions.MAX_REMARKS));
        b.put("designationSource", split(designation.childText("DesignationSource")));
        b.put("sanctionsImposed", split(designation.childText("SanctionsImposed")));
        b.put("listedOn", designated == null ? null : LocalDate.ofInstant(designated, ZoneOffset.UTC).toString());
        b.put("lastUpdated", designation.childText("LastUpdated"));
        b.put("imo", imo);
        b.put("vesselFlag", flags.isEmpty() ? null : flags.get(0));
        if (unReference != null && !unReference.isBlank()) {
            b.put("unReference", unReference.strip());
            b.put("crossRefs", List.of(unReference.strip()));
        }
        return b.build();
    }

    /** A {@code |}-separated value (SanctionsImposed, DesignationSource) as a list; empty for none. */
    private static List<String> split(String value) {
        List<String> out = new ArrayList<>();
        if (value != null) {
            for (String part : value.split("\\|")) {
                if (!part.isBlank()) {
                    out.add(Sanctions.truncate(part, Sanctions.MAX_ALIAS_CHARS));
                }
            }
        }
        return out;
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
