package ai.foresignal.source.adapters.cisa;

import ai.foresignal.source.kit.KitUrls;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The CISA Known Exploited Vulnerabilities catalog (prompt 076): one {@code vulnerability} record per entry, no
 * geometry.
 *
 * <p><strong>Each run:</strong> one conditional GET of {@value #CATALOG_URL} (verified 2026-10-13: {@code ETag} and
 * {@code Last-Modified}, about 1.8 MB, {@code catalogVersion 2026.10.13}, 1,741 entries). A 304 is
 * {@link FetchResult#unchanged()}. A non-200 answer, a truncated body or a catalog that does not parse is an
 * {@link UpstreamFormatException} and forgets the validators. The catalog is parsed by {@link KevParser}.
 *
 * <p><strong>Removals:</strong> an entry missing from two consecutive complete catalogs is tombstoned
 * ({@link AbsenceTombstoner}); CISA removes entries only rarely. An empty catalog tombstones nothing.
 */
@Component
public class CisaKevAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "cisa-kev";

    /** The production catalog. */
    public static final String CATALOG_URL =
            "https://www.cisa.gov/sites/default/files/feeds/known_exploited_vulnerabilities.json";

    /** The test-only property moving every cyber-source URL under one base as {@code {base}/{host}{path}}. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.cyber.base-url";

    /** The {@code getConditional} state key. */
    public static final String STATE_KEY = "catalog";

    /** Raw retention: KEV entries are the {@code event} analytic class (Conventions), 1825 days. */
    static final Duration RETENTION = Duration.ofDays(1825);

    /** The response cap (the catalog was 1.8 MB on 2026-10-13). */
    static final int MAX_RESPONSE_BYTES = 16 * 1024 * 1024;

    private final URI catalogUri;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank in production
     */
    @Autowired
    public CisaKevAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.catalogUri = URI.create(KitUrls.rebased(CATALOG_URL, baseUrlOverride));
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The catalog URL this adapter fetches. */
    public URI catalogUri() {
        return catalogUri;
    }

    /**
     * Fetches the catalog once (see the class note).
     *
     * @throws AdapterException when the request fails
     * @throws UpstreamFormatException on a bad status, a truncated body or a catalog that does not parse
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        UpstreamResponse response = ctx.http().getConditional(catalogUri, STATE_KEY);
        if (response.notModified()) {
            return FetchResult.unchanged();
        }
        if (response.status() != 200 || response.truncated()) {
            SourceHttp.forgetValidators(ctx.state(), STATE_KEY);
            throw new UpstreamFormatException(ID + (response.status() != 200
                    ? ": the catalog answered HTTP " + response.status() : ": the catalog exceeds the response cap"),
                    "");
        }
        KevParser.Catalog catalog;
        try {
            catalog = KevParser.parse(response.body());
        } catch (UpstreamFormatException e) {
            SourceHttp.forgetValidators(ctx.state(), STATE_KEY);
            throw e;
        }
        ctx.log().debug("{}: catalog {} with {} entr(ies), skipped {}", ctx.sourceId(), catalog.catalogVersion(),
                catalog.records().size(), catalog.skipped());
        if (catalog.records().isEmpty()) {
            return FetchResult.complete(catalog.records());
        }
        List<String> gone = AbsenceTombstoner.update(ctx.state(), AbsenceTombstoner.Outcome.COMPLETE,
                catalog.records().stream().map(NormalizedRecord::externalId).toList());
        List<NormalizedRecord> out = new ArrayList<>(catalog.records());
        out.addAll(AbsenceTombstoner.tombstones(gone, KevParser.KIND, catalog.released()));
        return FetchResult.complete(out);
    }

    private static SourceDescriptor buildDescriptor() {
        String kind = KevParser.KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("CISA Known Exploited Vulnerabilities")
                .description("The CISA Known Exploited Vulnerabilities (KEV) catalog: one record per CVE with"
                        + " vendor, product, name, a short description, the required action and its due date, and"
                        + " whether the CVE is known to be used in ransomware campaigns. No geometry. Every text"
                        + " field is upstream text. An entry missing from two consecutive catalogs is tombstoned.")
                .kind(kind)
                .homepage("https://www.cisa.gov/known-exploited-vulnerabilities-catalog")
                .attribution("Cybersecurity and Infrastructure Security Agency (CISA), Known Exploited"
                        + " Vulnerabilities Catalog")
                .licence("CC0 1.0 (\"The KEV database is distributed under the Creative Commons 0 1.0 License\","
                        + " github.com/cisagov/kev-data)")
                .termsUrl("https://github.com/cisagov/kev-data/blob/develop/LICENSE")
                .mode(Mode.POLLING)
                .interval(Duration.ofHours(1), Duration.ofHours(6))
                .defaultBudget(Budget.of(1, 20))
                .maxResponseBytes(MAX_RESPONSE_BYTES)
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(new IngestPolicy.All(null))
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .defaultRetention(RETENTION)
                .kindRetention(kind, RETENTION)
                .titleField("vulnerabilityName")
                .searchFields("cveId", "vendorProject", "product", "vulnerabilityName")
                .embedFields("vulnerabilityName", "shortDescription");
        text(b, "cveId", "CVE", "The CVE id");
        text(b, "vendorProject", "Vendor", "The vendor or project (trimmed)");
        text(b, "product", "Product", "The product (trimmed)");
        text(b, "vulnerabilityName", "Name", "CISA's name for the vulnerability (trimmed); upstream text, untrusted");
        text(b, "shortDescription", "Description", "A short description; upstream text, untrusted");
        text(b, "requiredAction", "Required action", "The action CISA requires of federal agencies; upstream text,"
                + " untrusted");
        text(b, "dueDate", "Due date", "The remediation due date (yyyy-MM-dd)");
        text(b, "knownRansomwareCampaignUse", "Ransomware use", "Known or Unknown");
        b.fieldSpec("cwes", new FieldSpec(FieldType.ARRAY, null, "CWEs", "CWE ids, at most 10", null, Set.of()));
        text(b, "notes", "Notes", "CISA's notes and reference links, at most 2 KB; upstream text, untrusted");
        b.fieldSpec("kev", new FieldSpec(FieldType.BOOLEAN, null, "In KEV", "Always true for this source", null,
                Set.of()));
        return b.build();
    }

    private static void text(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, Set.of()));
    }
}
