package ai.foresignal.source.adapters.faaregistry;

import ai.foresignal.source.adapters.faa.FaaSources;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The FAA Releasable Aircraft Database ({@code faa-registry}, prompt 098): the aircraft registration
 * catalogue used to anchor identities (airframe serial + registration + ICAO 24-bit address) without
 * ever storing an owner's name or address.
 *
 * <p>Weekly, conditional on {@code Last-Modified}: the archive is about 60 MB and changes daily.
 * Deregistered N-numbers disappear from a complete snapshot and are tombstoned through the 094
 * complete-set reconciliation ({@code dataset-release}).
 *
 * <p><strong>Privacy:</strong> {@code NAME}, {@code STREET}, {@code CITY} and the other owner
 * columns ({@link FaaRegistryParser#PII_COLUMNS}) are never copied into a payload, unless the operator
 * enables {@code nonIndividualOwnerNames} and the registrant is a corporation, government, LLC or
 * non-citizen corporation.
 */
@Component
public class FaaRegistryAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "faa-registry";

    /** The archive URL. */
    public static final String ARCHIVE_URL = "https://registry.faa.gov/database/ReleasableAircraft.zip";

    /** The terms/download page. */
    public static final String TERMS_URL =
            "https://www.faa.gov/licenses_certificates/aircraft_certification/aircraft_registry/releasable_aircraft_download";

    /** The conditional-GET validator key. */
    public static final String STATE = "releasable-aircraft";

    /** The {@code dataset-release} kind of the 094 reconciliation. */
    public static final String RELEASE_KIND = "dataset-release";

    /** The optional parameter storing individual owner names. */
    private static final String NON_INDIVIDUAL_OWNER_NAMES = "nonIndividualOwnerNames";

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;

    /** Creates the adapter. */
    public FaaRegistryAdapter(@Value("${" + FaaSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.baseUrl = baseUrlOverride == null ? "" : baseUrlOverride;
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The archive URL, rebased in tests. */
    URI archiveUri() {
        return FaaSources.uri(ARCHIVE_URL, baseUrl);
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        UpstreamResponse archive = ctx.http().getConditional(archiveUri(), STATE);
        if (archive.notModified()) {
            return FetchResult.unchanged();
        }
        FaaRegistryParser.Result result;
        try {
            boolean nonIndividual = ctx.params().bool(NON_INDIVIDUAL_OWNER_NAMES, false);
            result = FaaRegistryParser.parse(archive.body(), nonIndividual, ctx::emitChunk);
        } catch (UpstreamFormatException e) {
            ai.foresignal.source.spi.SourceHttp.forgetValidators(ctx.state(), STATE);
            throw e;
        }
        ctx.log().debug("{}: parsed {} registration(s), skipped {}", ID, result.records(), result.skipped());
        return FetchResult.complete(java.util.List.of(), "master=" + result.records());
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("FAA aircraft registry")
                .description("The FAA Releasable Aircraft Database: the registration catalogue that anchors"
                        + " aircraft identities (airframe serial, tail number and ICAO 24-bit address). Owner names and"
                        + " addresses are never stored.")
                .kind(FaaRegistryParser.KIND)
                .kind(RELEASE_KIND)
                .homepage(TERMS_URL)
                .attribution(FaaSources.ATTRIBUTION)
                .licence(FaaSources.LICENSE)
                .termsUrl(TERMS_URL)
                .mode(Mode.POLLING)
                .interval(Duration.ofDays(1), Duration.ofDays(7))
                .defaultBudget(Budget.of(2, 10, 200))
                .maxResponseBytes(150 * 1024 * 1024)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.RECEIVED)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(Duration.ofDays(1825))
                .entityKey("airframe", "serial")
                .entityKey("registration", "nNumber")
                .entityKey("icao24", "modeSHex")
                .entityAttribute("manufacturer", "manufacturer")
                .entityAttribute("model", "model")
                .entityAttribute("typeAircraft", "typeAircraft")
                .entityAttribute("typeEngine", "typeEngine")
                .entityAttribute("engines", "engines")
                .entityAttribute("seats", "seats")
                .entityAttribute("yearMfr", "yearMfr")
                .titleField("nNumber")
                .rankField("nNumber")
                .zeroResultsNormal(false)
                .param(ParamSpec.optional(NON_INDIVIDUAL_OWNER_NAMES, ParamType.BOOLEAN,
                        "Store owner names when the registrant is a corporation, government, LLC or"
                                + " non-citizen corporation (default false; never individual owners)",
                        false));
        FaaSources.text(b, "nNumber", "N-number", "The registration number without the leading N, for example 123AB");
        FaaSources.text(b, "serial", "Serial", "The airframe serial number");
        FaaSources.text(b, "mfrMdlCode", "Mfr/model code", "The FAA aircraft reference code (ACFTREF)");
        FaaSources.text(b, "manufacturer", "Manufacturer", "The manufacturer from the reference table");
        FaaSources.text(b, "model", "Model", "The model from the reference table");
        FaaSources.text(b, "typeAircraft", "Aircraft type", "Glider, Balloon, Fixed Wing, Rotorcraft, Weight-Shift…");
        FaaSources.text(b, "typeEngine", "Engine type", "Reciprocating, Turbo-prop, Turbo-jet, Turbo-shaft…");
        FaaSources.text(b, "weightClass", "Weight class", "The FAA weight class of the airframe");
        FaaSources.integer(b, "engines", null, "Engines", "Number of engines");
        FaaSources.integer(b, "seats", null, "Seats", "Number of seats");
        FaaSources.integer(b, "yearMfr", null, "Year", "Year of manufacture");
        FaaSources.text(b, "registrantType", "Registrant type", "Decoded registrant type (never an owner name)");
        FaaSources.text(b, "statusCode", "Status", "The FAA registration status code");
        FaaSources.timestamp(b, "certIssueDate", "Certified", "Certificate issue date");
        FaaSources.timestamp(b, "airworthinessDate", "Airworthy", "Airworthiness date");
        FaaSources.timestamp(b, "expirationDate", "Expires", "Registration expiration date");
        FaaSources.text(b, "modeSHex", "ICAO address", "The ICAO 24-bit address in lowercase hexadecimal");
        FaaSources.bool(b, "fractionalOwner", "Fractional", "Whether the registration is fractional");
        FaaSources.text(b, "kitMfr", "Kit manufacturer", "Kit manufacturer, when built from a kit");
        FaaSources.text(b, "kitModel", "Kit model", "Kit model, when built from a kit");
        FaaSources.text(b, "ownerName", "Owner name", "Only with nonIndividualOwnerNames and a corporation,"
                + " government, LLC or non-citizen-corporation registrant; never an individual owner");
        return b.build();
    }
}
