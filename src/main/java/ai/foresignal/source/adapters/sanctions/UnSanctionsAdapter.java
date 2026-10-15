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
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
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
 * {@code un-sanctions} (prompt 091): the UN Security Council consolidated sanctions list.
 *
 * <p>The XML URL answers 302 to an Azure Blob SAS URL; the blob host is allowlisted and the SAS query is
 * never logged. Downloads are capped and parsed from disk. A delisting is a tombstone, guarded against an
 * implausible shrink.
 */
@Component
public final class UnSanctionsAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "un-sanctions";

    /** The default base URL. */
    public static final String DEFAULT_BASE_URL = "https://scsanctions.un.org";

    /** Test-only base override. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.un-sanctions.base-url";

    static final String PATH_KEY = "un:path";
    static final String IDS_KEY = "un:ids";
    static final Duration RETENTION = Duration.ofDays(1825);

    /** The blob host a redirect may land on. */
    static final String REDIRECT_HOST = "blob.core.windows.net";

    private final URI baseUrl;
    private final SourceDescriptor descriptor;

    /** Creates the adapter. */
    @Autowired
    public UnSanctionsAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
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
        URI url = URI.create(baseUrl + "/resources/xml/en/consolidated.xml");
        UpstreamResponse response = ctx.http().getNoFollow(url);
        URI target = url;
        if (response.status() >= 300 && response.status() < 400) {
            String location = response.header("location");
            if (location == null || location.isBlank()) {
                throw new UpstreamFormatException("The UN list returned " + response.status()
                        + " without a Location", "");
            }
            URI redirect = URI.create(location);
            String host = redirect.getHost() == null ? "" : redirect.getHost();
            // A same-host redirect (the configured base, or a test rebasing) is allowed; otherwise the
            // redirect must land on the reviewed Azure Blob host.
            String baseHost = baseUrl.getHost();
            if (!(host.equals(baseHost) || host.equals(REDIRECT_HOST) || host.endsWith("." + REDIRECT_HOST))) {
                throw new AdapterException("The UN list redirected to an unapproved host");
            }
            String path = redirect.getScheme() + "://" + redirect.getAuthority() + redirect.getPath();
            if (path.equals(ctx.state().get(PATH_KEY).orElse(null))) {
                return FetchResult.unchanged();
            }
            ctx.state().put(PATH_KEY, path);
            target = redirect;
        } else if (response.status() != 200) {
            throw new UpstreamFormatException("The UN list answered HTTP " + response.status(), "");
        }
        Path file = Path.of("un-sanctions-" + System.nanoTime() + ".xml");
        // A relative name resolves under the raster temp directory; parse the reported file.
        ai.foresignal.source.spi.DownloadResult downloaded = ctx.http().download(target, file, 64 * 1024 * 1024);
        List<NormalizedRecord> records;
        try {
            records = parse(downloaded.file());
        } catch (IOException | XMLStreamException e) {
            throw new UpstreamFormatException("The UN consolidated list is not valid XML", "", e);
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

    /** The records of the UN consolidated list file. */
    public List<NormalizedRecord> parse(Path file) throws IOException, XMLStreamException {
        List<NormalizedRecord> out = new ArrayList<>();
        Instant generated = null;
        try (var in = java.nio.file.Files.newInputStream(file)) {
            XMLStreamReader reader = Xml.open(in);
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
            String value = node.childText("VALUE");
            if (value == null) {
                value = node.text();
            }
            if (value != null && !value.isBlank()) {
                nationalities.add(value.strip());
            }
        }
        List<String> lastUpdated = new ArrayList<>();
        for (Xml.Node node : entry.descendants("LAST_DAY_UPDATED")) {
            String value = node.childText("VALUE");
            if (value != null) {
                lastUpdated.add(value.strip());
            }
        }
        List<Map<String, Object>> documents = new ArrayList<>();
        for (Xml.Node document : entry.descendants("INDIVIDUAL_DOCUMENT")) {
            documents.add(Sanctions.identifier(document.childText("TYPE_OF_DOCUMENT"),
                    document.childText("NUMBER"), document.childText("ISSUING_COUNTRY")));
        }

        NormalizedRecord.Builder b = NormalizedRecord.builder("un:" + reference, Sanctions.KIND)
                .observedAt(generated)
                .eventTime(Instants.parse(entry.childText("LISTED_ON")));
        b.put("list", "UN");
        b.put("name", name);
        b.put("partyType", entry.name().equals("ENTITY") ? "entity" : "individual");
        b.put("programs", single(entry.childText("UN_LIST_TYPE")));
        b.put("aliases", Sanctions.cap(aliases, Sanctions.MAX_ALIASES, Sanctions.MAX_ALIAS_CHARS));
        b.put("nationalities", Sanctions.cap(nationalities, Sanctions.MAX_IDENTIFIERS, Sanctions.MAX_ALIAS_CHARS));
        b.put("identifiers", documents.size() > Sanctions.MAX_IDENTIFIERS
                ? documents.subList(0, Sanctions.MAX_IDENTIFIERS) : documents);
        b.put("remarks", Sanctions.truncate(entry.childText("COMMENTS1"), Sanctions.MAX_REMARKS));
        b.put("lastUpdated", lastUpdated.isEmpty() ? null : lastUpdated.get(lastUpdated.size() - 1));
        b.put("unReference", reference);
        return b.build();
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
                        + "The XML URL redirects to an Azure Blob SAS URL; the host is allowlisted and the SAS "
                        + "query is never logged.")
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
