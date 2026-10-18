package ai.foresignal.testsupport.formats;

import ai.foresignal.provenance.LicenceTerms;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.ingest.TestIngestSources.TestPushAdapter;
import ai.foresignal.testsupport.timetravel.TimeTravelTestSources;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * The exchange-format test context (prompt 141): the time-travel sources of {@link TimeTravelTestSources} (for CZML
 * history and tracks) plus PUSH sources of the kinds the FDSN, STIX and TAXII endpoints read:
 *
 * <ul>
 *   <li>{@value #QUAKES} and the private {@value #QUAKES_PRIVATE}: kind {@code earthquake}, title from {@code place};
 *   <li>{@value #KEV}: kind {@code vulnerability};
 *   <li>{@value #URLS}: kind {@code malware-url};
 *   <li>{@value #C2}: kind {@code malware-c2};
 *   <li>{@value #URLS_LOCAL}: kind {@code malware-url} under a {@code LOCAL_ONLY} license, so the {@code EXPORT}
 *       egress path refuses it.
 * </ul>
 *
 * <p>Every source but {@value #URLS_LOCAL} carries redistributable terms, so the egress policy lets its records out.
 * All the format integration tests import this one configuration and so share one context.
 */
@TestConfiguration
@Import(TimeTravelTestSources.class)
public class FormatsTestSources {

    /** Earthquakes. */
    public static final String QUAKES = "fmt-quakes";

    /** Private earthquakes. */
    public static final String QUAKES_PRIVATE = "fmt-quakes-private";

    /** KEV-like vulnerabilities. */
    public static final String KEV = "fmt-kev";

    /** URLhaus-like malware URLs. */
    public static final String URLS = "fmt-urlhaus";

    /** Feodo-like C2 servers. */
    public static final String C2 = "fmt-feodo";

    /** Malware URLs whose license keeps them local. */
    public static final String URLS_LOCAL = "fmt-urlhaus-local";

    /** Redistributable test terms. */
    public static final LicenceTerms OPEN = new LicenceTerms("fmt-open", "Format test open terms",
            LicenceTerms.CommercialUse.ALLOWED, false, true, LicenceTerms.Redistribution.ALLOWED,
            "Format test data", null);

    /** Terms that keep records on this instance. */
    public static final LicenceTerms LOCAL = new LicenceTerms("fmt-local", "Format test local terms",
            LicenceTerms.CommercialUse.FORBIDDEN, false, true, LicenceTerms.Redistribution.LOCAL_ONLY,
            "Format test local data", null);

    /** {@value #QUAKES}. */
    @Bean
    public TestPushAdapter fmtQuakesAdapter() {
        return new TestPushAdapter(quakes(QUAKES).build());
    }

    /** {@value #QUAKES_PRIVATE}. */
    @Bean
    public TestPushAdapter fmtQuakesPrivateAdapter() {
        return new TestPushAdapter(quakes(QUAKES_PRIVATE).privateSource(true).build());
    }

    /** {@value #KEV}. */
    @Bean
    public TestPushAdapter fmtKevAdapter() {
        return new TestPushAdapter(base(KEV, "vulnerability", OPEN).titleField("cveId").build());
    }

    /** {@value #URLS}. */
    @Bean
    public TestPushAdapter fmtUrlsAdapter() {
        return new TestPushAdapter(base(URLS, "malware-url", OPEN).titleField("urlDefanged").build());
    }

    /** {@value #C2}. */
    @Bean
    public TestPushAdapter fmtC2Adapter() {
        return new TestPushAdapter(base(C2, "malware-c2", OPEN).titleField("ip").build());
    }

    /** {@value #URLS_LOCAL}. */
    @Bean
    public TestPushAdapter fmtUrlsLocalAdapter() {
        return new TestPushAdapter(base(URLS_LOCAL, "malware-url", LOCAL).titleField("urlDefanged").build());
    }

    private static SourceDescriptor.Builder quakes(String id) {
        return base(id, "earthquake", OPEN).titleField("place")
                .kindDisplay("earthquake", new KindDisplay(null, null, "#CC3300", null));
    }

    private static SourceDescriptor.Builder base(String id, String kind, LicenceTerms terms) {
        return SourceDescriptor.builder(id)
                .name("Format test source " + id)
                .attribution("Attribution of " + id)
                .kind(kind)
                .licenceTerms(terms)
                .mode(Mode.PUSH)
                .defaultBudget(Budget.of(60, 10_000));
    }
}
