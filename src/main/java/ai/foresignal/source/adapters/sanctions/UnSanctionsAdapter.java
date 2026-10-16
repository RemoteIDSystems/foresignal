package ai.foresignal.source.adapters.sanctions;

import ai.foresignal.source.adapters.humanitarian.Instants;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code un-sanctions} (prompt 091): the UN Security Council consolidated sanctions list.
 *
 * <p>The XML URL answers 302 to an Azure Blob SAS URL whose path is fixed and whose SAS query changes on
 * every request; the blob account is allowlisted, the blob is fetched with a conditional GET on its ETag (an
 * unchanged list costs the redirect and one 304), and the SAS query is masked wherever a URL is stored or
 * logged ({@code UrlRedactor}). The list (about 2.2 MB) is stream-parsed from the response body. A delisting
 * is a tombstone, guarded against an implausible shrink ({@link ListReconciler}).
 */
@Component
public final class UnSanctionsAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "un-sanctions";

    /** The default base URL. */
    public static final String DEFAULT_BASE_URL = "https://scsanctions.un.org";

    /** Test-only base override. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.un-sanctions.base-url";

    /** The adapter-state key of the blob's conditional-GET validators. */
    static final String BLOB_KEY = "un:blob";
    static final String IDS_KEY = "un:ids";
    static final Duration RETENTION = Duration.ofDays(1825);

    /** The Azure Blob account a redirect may land on (the prompt's verified host). */
    static final String REDIRECT_HOST = "unsolprodfiles.blob.core.windows.net";

    private final URI baseUrl;
    private final ListReconciler reconciler;
    private final SourceDescriptor descriptor;

    /** Creates the adapter without a notification publisher (tests). */
    public UnSanctionsAdapter(String baseUrlOverride) {
        this(baseUrlOverride, null);
    }

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride test-only base URL, blank for the real one
     * @param events publishes the shrink-guard system notification; may be null
     */
    @Autowired
    public UnSanctionsAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride,
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
        URI url = URI.create(baseUrl + "/resources/xml/en/consolidated.xml");
        UpstreamResponse response = ctx.http().getNoFollow(url);
        byte[] body;
        if (response.status() >= 300 && response.status() < 400) {
            String location = response.header("location");
            if (location == null || location.isBlank()) {
                throw new UpstreamFormatException("The UN list returned " + response.status()
                        + " without a Location", "");
            }
            URI redirect = URI.create(location);
            String host = redirect.getHost() == null ? "" : redirect.getHost();
            // A same-host redirect (the configured base, or a test rebasing) is allowed; otherwise the
            // redirect must land on the reviewed Azure Blob account.
            if (!(host.equals(baseUrl.getHost()) || host.equals(REDIRECT_HOST))) {
                throw new AdapterException("The UN list redirected to an unapproved host");
            }
            // The blob path is fixed and only the SAS query changes, so the path says nothing about the
            // content. The blob's ETag does (verified 2026-10-16: a conditional GET answers 304), and the
            // validators are committed with the run's state only when the fetch succeeds (framework rule).
            UpstreamResponse blob = ctx.http().getConditional(redirect, BLOB_KEY);
            if (blob.notModified()) {
                return FetchResult.unchanged();
            }
            if (blob.status() != 200) {
                throw new UpstreamFormatException("The UN list blob answered HTTP " + blob.status(), "");
            }
            body = blob.body();
        } else if (response.status() == 200) {
            body = response.body();
        } else {
            throw new UpstreamFormatException("The UN list answered HTTP " + response.status(), "");
        }
        List<NormalizedRecord> records;
        try {
            records = parse(new ByteArrayInputStream(body));
        } catch (IOException | XMLStreamException e) {
            throw new UpstreamFormatException("The UN consolidated list is not valid XML", "", e);
        }
        ListReconciler.Outcome outcome = reconciler.reconcile(ctx, IDS_KEY, "UN", records);
        ListReconciler.applyHealth(ctx.state(), List.of(outcome));
        return FetchResult.complete(outcome.records());
    }

    /** The records of the UN consolidated list file. */
    public List<NormalizedRecord> parse(Path file) throws IOException, XMLStreamException {
        try (InputStream in = java.nio.file.Files.newInputStream(file)) {
            return parse(in);
        }
    }

    /** The records of a UN consolidated list document, stream-parsed. */
    public List<NormalizedRecord> parse(InputStream in) throws IOException, XMLStreamException {
        List<NormalizedRecord> out = new ArrayList<>();
        Instant generated = null;
        XMLStreamReader reader = Xml.open(in);
        try {
            while (reader.hasNext()) {
                reader.next();
                if (Xml.at(reader, "CONSOLIDATED_LIST")) {
                    generated = Instants.parse(reader.getAttributeValue(null, "dateGenerated"));
                } else if (Xml.at(reader, "INDIVIDUAL") || Xml.at(reader, "ENTITY")) {
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

    private static NormalizedRecord map(Xml.Node entry, Instant generated) {
        String reference = entry.childText("REFERENCE_NUMBER");
        if (reference == null) {
            return null;
        }
        String name = join(entry.childText("FIRST_NAME"), entry.childText("SECOND_NAME"),
                entry.childText("THIRD_NAME"), entry.childText("FOURTH_NAME"));
        List<String> aliases = new ArrayList<>();
        for (Xml.Node node : entry.descendants("ALIAS_NAME")) {
            if (node.text() != null && !node.text().isBlank()) {
                aliases.add(node.text());
            }
        }
        List<String> nationalities = new ArrayList<>();
        for (Xml.Node node : entry.descendants("NATIONALITY")) {
            List<String> values = node.leafTexts("VALUE");
            if (values.isEmpty() && node.text() != null) {
                values = List.of(node.text());
            }
            for (String value : values) {
                if (!value.isBlank()) {
                    nationalities.add(value.strip());
                }
            }
        }
        // LAST_DAY_UPDATED holds one VALUE per amendment (up to three in the 2026-10-13 capture); the
        // latest ISO date is the one that says when the entry last changed.
        String lastUpdated = null;
        for (Xml.Node node : entry.descendants("LAST_DAY_UPDATED")) {
            for (String value : node.leafTexts("VALUE")) {
                String day = value.strip();
                if (!day.isEmpty() && (lastUpdated == null || day.compareTo(lastUpdated) > 0)) {
                    lastUpdated = day;
                }
            }
        }
        List<Map<String, Object>> documents = new ArrayList<>();
        for (Xml.Node document : entry.descendants("INDIVIDUAL_DOCUMENT")) {
            if (document.childText("NUMBER") == null && document.childText("TYPE_OF_DOCUMENT") == null) {
                continue;
            }
            documents.add(Sanctions.identifier(document.childText("TYPE_OF_DOCUMENT"),
                    document.childText("NUMBER"), document.childText("ISSUING_COUNTRY")));
        }
        List<Map<String, Object>> addresses = new ArrayList<>();
        for (String element : List.of("INDIVIDUAL_ADDRESS", "ENTITY_ADDRESS")) {
            for (Xml.Node address : entry.descendants(element)) {
                Map<String, Object> place = new LinkedHashMap<>();
                Sanctions.putIfPresent(place, "city", address.childText("CITY"));
                Sanctions.putIfPresent(place, "country", address.childText("COUNTRY"));
                if (!place.isEmpty()) {
                    addresses.add(place);
                }
            }
        }
        String listedOn = entry.childText("LISTED_ON");

        NormalizedRecord.Builder b = NormalizedRecord.builder("un:" + reference, Sanctions.KIND)
                .observedAt(generated)
                .eventTime(Instants.parse(listedOn));
        b.put("list", "UN");
        b.put("name", name);
        b.put("partyType", entry.name().equals("ENTITY") ? "entity" : "individual");
        b.put("programs", single(entry.childText("UN_LIST_TYPE")));
        b.put("aliases", Sanctions.cap(aliases, Sanctions.MAX_ALIASES, Sanctions.MAX_ALIAS_CHARS));
        b.put("nationalities", Sanctions.cap(nationalities, Sanctions.MAX_IDENTIFIERS, Sanctions.MAX_ALIAS_CHARS));
        b.put("dateOfBirth", dateOfBirth(entry));
        b.put("identifiers", documents.size() > Sanctions.MAX_IDENTIFIERS
                ? documents.subList(0, Sanctions.MAX_IDENTIFIERS) : documents);
        b.put("addresses", addresses.size() > Sanctions.MAX_ADDRESSES
                ? addresses.subList(0, Sanctions.MAX_ADDRESSES) : addresses);
        b.put("remarks", Sanctions.truncate(entry.childText("COMMENTS1"), Sanctions.MAX_REMARKS));
        b.put("listedOn", listedOn == null || listedOn.isBlank() ? null : listedOn.strip());
        b.put("lastUpdated", lastUpdated);
        b.put("unReference", reference);
        return b.build();
    }

    /**
     * The first {@code INDIVIDUAL_DATE_OF_BIRTH} as published: its {@code DATE}, else its {@code YEAR}, else
     * {@code FROM_YEAR-TO_YEAR}; null when none carries a value.
     */
    static String dateOfBirth(Xml.Node entry) {
        for (Xml.Node dob : entry.descendants("INDIVIDUAL_DATE_OF_BIRTH")) {
            String date = dob.childText("DATE");
            if (date != null) {
                return date;
            }
            String year = dob.childText("YEAR");
            if (year != null) {
                return year;
            }
            String from = dob.childText("FROM_YEAR");
            String to = dob.childText("TO_YEAR");
            if (from != null || to != null) {
                return (from == null ? "" : from) + "-" + (to == null ? "" : to);
            }
        }
        return null;
    }

    private static List<String> single(String value) {
        return value == null || value.isBlank() ? List.of() : List.of(value.strip());
    }

    private static String join(String... parts) {
        StringBuilder out = new StringBuilder();
        for (String part : parts) {
            if (part != null && !part.isBlank()) {
                if (!out.isEmpty()) {
                    out.append(' ');
                }
                out.append(part.strip());
            }
        }
        return out.isEmpty() ? null : out.toString();
    }

    private static SourceDescriptor buildDescriptor() {
        return SourceDescriptor.builder(ID)
                .name("UN Security Council sanctions list")
                .description("The UN Security Council consolidated sanctions list (individuals and entities). "
                        + "The XML URL redirects to an Azure Blob SAS URL; the host is allowlisted, the blob is "
                        + "change-checked by its ETag and the SAS query is masked in logs.")
                .kind(Sanctions.KIND)
                .mode(Mode.POLLING)
                .homepage("https://main.un.org/securitycouncil/en/content/un-sc-consolidated-list")
                .attribution("United Nations Security Council")
                .licence("UN terms of use (public information)")
                .termsUrl("https://www.un.org/en/about-us/terms-of-use")
                .interval(Duration.ofHours(12), Duration.ofHours(12))
                .defaultBudget(2, 10)
                .maxResponseBytes(64 * 1024 * 1024)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .entityKey("un-reference", "unReference")
                .titleField("name")
                .searchFields("name", "aliases", "identifiers", "programs")
                .significantFields("lastUpdated")
                .zeroResultsNormal(false)
                .kindDisplay(Sanctions.KIND, new KindDisplay("{name}", "danger", "#C92A2A", null))
                .build();
    }
}
