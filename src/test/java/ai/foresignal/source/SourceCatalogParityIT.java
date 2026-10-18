package ai.foresignal.source;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.plugin.feed.WasmFeedAdapter;
import ai.foresignal.provenance.LicenceTerms;
import ai.foresignal.provenance.SourceLicences;
import ai.foresignal.source.kit.KitOverlay;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.test.IntegrationTest;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Keeps the parity columns of {@code docs/Source-Catalog.md} in step with the registered source descriptors (prompt
 * 144, task 1).
 *
 * <p>Every catalog table whose second to fifth columns are {@code Tier | Policy | License | Egress} is parsed. The
 * first column names the sources of a row: each back-quoted token is a source id or a family pattern ({@code {n}},
 * {@code {id}}, {@code <slug>} or {@code *} for any lower-kebab text, {@code 1..4} for a number in that range). A row
 * whose Tier cell reads {@code not implemented} names sources that must not be registered; a row whose Tier cell
 * starts with {@code per } describes a family whose members are listed elsewhere and is skipped. Every other row is a
 * parity row, and its four cells must equal what the live descriptor declares:
 *
 * <ul>
 *   <li><b>Tier</b>: {@link SourceDescriptor#historyTier()};
 *   <li><b>Policy</b>: the mode of {@link SourceDescriptor#defaultIngestPolicy()} and its keyframe
 *       ({@code · keyframe 240 s}; no keyframe written means none);
 *   <li><b>License</b>: the terms id {@link SourceLicences#termsOf(String)} resolves, exactly as spelled in
 *       {@code provenance/licences.json};
 *   <li><b>Egress</b>: the stricter of the terms' {@link LicenceTerms#effectiveRedistribution()} and the
 *       descriptor's {@link SourceDescriptor#egress()} ({@link Egress#LOCAL_ONLY} turns {@code ALLOWED} into
 *       {@code LOCAL_ONLY}).
 * </ul>
 *
 * <p>License and Egress may read {@code inherited} only for a DERIVED source without structured terms that has input
 * sources ({@link SourceLicences#inputsOf(String)}): its terms are the most restrictive of whichever inputs are
 * registered at runtime, so the catalog does not freeze them.
 *
 * <p>Sources registered at runtime are not cataloged and are skipped: dynamic registrations (custom PUSH sources,
 * federation peers), operator kit overlays ({@link KitOverlay#ID_PREFIX}) and WebAssembly feed plugins
 * ({@link WasmFeedAdapter}).
 *
 * <p>It is an integration test because the registry is assembled from the adapter and family beans, many of which
 * need injected collaborators, so only a started context knows every registered source. It uses the shared
 * {@link IntegrationTest} context, so it adds no context to the cache.
 */
@IntegrationTest
class SourceCatalogParityIT {

    /** The catalog document, relative to the project directory (Gradle's test working directory). */
    static final Path CATALOG = Path.of("docs", "Source-Catalog.md");

    /** The header cells, second to fifth column, that mark a parity table. */
    static final List<String> PARITY_HEADER = List.of("Tier", "Policy", "License", "Egress");

    /** The Tier cell of a row whose sources are not registered. */
    static final String NOT_IMPLEMENTED = "not implemented";

    /** The License and Egress cell of a DERIVED source whose terms come from its inputs. */
    static final String INHERITED = "inherited";

    /** The prefix of a Tier cell that defers to another table ({@code per feed}, {@code per source}, …). */
    static final String DESCRIPTIVE_PREFIX = "per ";

    /** A back-quoted source id or family pattern in a row's first cell. */
    private static final Pattern TOKEN = Pattern.compile("`([a-z0-9{}<>*.\\-]+)`");

    /** An unescaped pipe, the cell separator. */
    private static final Pattern CELL_SEPARATOR = Pattern.compile("(?<!\\\\)\\|");

    /** A separator-row cell: dashes with optional alignment colons. */
    private static final Pattern DASHES = Pattern.compile(":?-+:?");

    /** The parts of a family pattern: a number range, a placeholder or a wildcard. */
    private static final Pattern PATTERN_PART = Pattern.compile("(\\d+)\\.\\.(\\d+)|\\{[^}]*}|<[^>]*>|\\*");

    /** What a placeholder stands for: lower-kebab text. */
    private static final String KEBAB = "[a-z0-9]+(?:-[a-z0-9]+)*";

    /** The policy mode at the start of a Policy cell. */
    private static final Pattern POLICY_MODE = Pattern.compile("^(ALL|SAMPLED|LATEST)\\b");

    /** The keyframe in a Policy cell. */
    private static final Pattern KEYFRAME = Pattern.compile("keyframe\\s+(\\d+)\\s*(s|min|h|d)\\b");

    /** The values an Egress cell may hold besides {@link #INHERITED}. */
    private static final Set<String> EGRESS_VALUES = Set.of("ALLOWED", "LOCAL_ONLY", "FORBIDDEN");

    /** A License cell: one terms id. */
    private static final Pattern LICENSE_ID = Pattern.compile("[A-Za-z0-9.\\-]+");

    @Autowired SourceRegistry registry;
    @Autowired SourceLicences licences;

    /**
     * Every cataloged source has at least one parity row, and every parity row that names it agrees with its
     * descriptor on tier, policy mode and keyframe, license terms id and egress.
     */
    @Test
    @DisplayName("every registered source has a catalog row whose Tier, Policy, License and Egress match its descriptor")
    void everyRegisteredSourceMatchesItsRows() {
        List<Row> rows = parityRows(parse(read(CATALOG)));
        assertThat(rows).as("parity rows in %s", CATALOG).isNotEmpty();

        List<String> missing = new ArrayList<>();
        List<String> mismatches = new ArrayList<>();
        for (SourceRegistry.Registration registration : registry.all()) {
            if (!cataloged(registration)) {
                continue;
            }
            SourceDescriptor descriptor = registration.descriptor();
            List<Row> matching = rows.stream().filter(row -> row.names(descriptor.id())).toList();
            if (matching.isEmpty()) {
                missing.add(descriptor.id());
                continue;
            }
            for (Row row : matching) {
                mismatches.addAll(compare(descriptor, row));
            }
        }

        assertThat(missing).as("registered sources with no parity row in %s", CATALOG).isEmpty();
        assertThat(mismatches).as("parity cells in %s that differ from the descriptor", CATALOG).isEmpty();
    }

    /**
     * Every literal id on a parity row is a registered source; an id that is not registered must be on a row marked
     * {@code not implemented}.
     */
    @Test
    @DisplayName("every id on an implemented catalog row is registered")
    void implementedRowsNameRegisteredSources() {
        Set<String> registered = catalogedIds();
        List<String> unregistered = new ArrayList<>();
        for (Row row : parityRows(parse(read(CATALOG)))) {
            for (IdPattern id : row.ids()) {
                if (id.literal() && !registered.contains(id.token())) {
                    unregistered.add(id.token() + " (line " + row.line() + ")");
                }
            }
        }
        assertThat(unregistered)
                .as("ids in %s that are not registered; mark their rows \"%s\"", CATALOG, NOT_IMPLEMENTED)
                .isEmpty();
    }

    /** No id or pattern on a row marked {@code not implemented} matches a registered source. */
    @Test
    @DisplayName("no id on a \"not implemented\" catalog row is registered")
    void notImplementedRowsNameNoRegisteredSource() {
        Set<String> registered = catalogedIds();
        List<String> implemented = new ArrayList<>();
        for (Row row : parse(read(CATALOG))) {
            if (!row.notImplemented()) {
                continue;
            }
            for (IdPattern id : row.ids()) {
                for (String source : registered) {
                    if (id.matches(source)) {
                        implemented.add(id.token() + " matches registered " + source + " (line " + row.line() + ")");
                    }
                }
            }
        }
        assertThat(implemented).as("rows of %s marked \"%s\" that name a registered source", CATALOG, NOT_IMPLEMENTED)
                .isEmpty();
    }

    /**
     * Every row of a parity table has a Tier cell the test understands, and every parity row has a well-formed
     * Policy, License and Egress cell, so a typo cannot make a row silently skipped or unmatched.
     */
    @Test
    @DisplayName("every parity table row of the catalog is well formed")
    void parityCellsAreWellFormed() {
        List<String> malformed = new ArrayList<>();
        for (Row row : parse(read(CATALOG))) {
            if (row.notImplemented() || row.descriptive()) {
                continue;
            }
            if (!row.tier().equals("STANDARD") && !row.tier().equals("TRACK")) {
                malformed.add("line " + row.line() + ": Tier \"" + row.tier() + "\"");
                continue;
            }
            if (!POLICY_MODE.matcher(row.policy()).find()) {
                malformed.add("line " + row.line() + ": Policy \"" + row.policy() + "\"");
            }
            if (!row.license().equals(INHERITED) && !LICENSE_ID.matcher(row.license()).matches()) {
                malformed.add("line " + row.line() + ": License \"" + row.license() + "\"");
            }
            if (!row.egress().equals(INHERITED) && !EGRESS_VALUES.contains(row.egress())) {
                malformed.add("line " + row.line() + ": Egress \"" + row.egress() + "\"");
            }
            if (row.ids().isEmpty()) {
                malformed.add("line " + row.line() + ": no back-quoted source id in \"" + row.idCell() + "\"");
            }
        }
        assertThat(malformed).as("malformed parity rows in %s", CATALOG).isEmpty();
    }

    // ------------------------------------------------------------------------- comparison

    /** The differences between {@code descriptor} and the parity {@code row}, one message each. */
    private List<String> compare(SourceDescriptor descriptor, Row row) {
        List<String> out = new ArrayList<>();
        String where = descriptor.id() + " (line " + row.line() + "): ";

        String tier = descriptor.historyTier().name();
        if (!tier.equals(row.tier())) {
            out.add(where + "Tier is " + tier + ", the catalog says " + row.tier());
        }

        IngestPolicy policy = descriptor.defaultIngestPolicy();
        Matcher mode = POLICY_MODE.matcher(row.policy());
        String documentedMode = mode.find() ? mode.group(1) : row.policy();
        if (!policy.mode().name().equals(documentedMode)) {
            out.add(where + "Policy mode is " + policy.mode().name() + ", the catalog says " + documentedMode);
        }
        Duration documentedKeyframe = keyframe(row.policy());
        if (!Objects.equals(policy.keyframe(), documentedKeyframe)) {
            out.add(where + "keyframe is " + policy.keyframe() + ", the catalog says " + documentedKeyframe);
        }

        LicenceTerms terms = licences.termsOf(descriptor.id());
        boolean inheritable = inherits(descriptor);
        if (row.license().equals(INHERITED)) {
            if (!inheritable) {
                out.add(where + "License reads inherited, but the source inherits no terms; its terms id is "
                        + terms.id());
            }
        } else if (!terms.id().equals(row.license())) {
            out.add(where + "License is " + terms.id() + ", the catalog says " + row.license());
        }

        String egress = egress(terms, descriptor);
        if (row.egress().equals(INHERITED)) {
            if (!inheritable) {
                out.add(where + "Egress reads inherited, but the source inherits no terms; its egress is " + egress);
            }
        } else if (!egress.equals(row.egress())) {
            out.add(where + "Egress is " + egress + ", the catalog says " + row.egress());
        }
        return out;
    }

    /**
     * True when {@code descriptor}'s terms are inherited from its inputs: a DERIVED source without structured terms
     * that has at least one input source.
     */
    private boolean inherits(SourceDescriptor descriptor) {
        return descriptor.mode() == Mode.DERIVED && descriptor.licenceTerms() == null
                && !licences.inputsOf(descriptor.id()).isEmpty();
    }

    /**
     * The egress a source's records get: the terms' effective redistribution, made {@code LOCAL_ONLY} when it is
     * {@code ALLOWED} but the descriptor's egress is {@link Egress#LOCAL_ONLY}.
     */
    static String egress(LicenceTerms terms, SourceDescriptor descriptor) {
        LicenceTerms.Redistribution redistribution = terms.effectiveRedistribution();
        if (redistribution == LicenceTerms.Redistribution.ALLOWED && descriptor.egress() == Egress.LOCAL_ONLY) {
            return LicenceTerms.Redistribution.LOCAL_ONLY.name();
        }
        return redistribution.name();
    }

    /** The keyframe a Policy cell states, or null when it states none. */
    static Duration keyframe(String policy) {
        Matcher m = KEYFRAME.matcher(policy);
        if (!m.find()) {
            return null;
        }
        long amount = Long.parseLong(m.group(1));
        return switch (m.group(2)) {
            case "s" -> Duration.ofSeconds(amount);
            case "min" -> Duration.ofMinutes(amount);
            case "h" -> Duration.ofHours(amount);
            case "d" -> Duration.ofDays(amount);
            default -> throw new IllegalArgumentException("Unknown keyframe unit in " + policy);
        };
    }

    /**
     * True when {@code registration} is a source the catalog describes: not registered at runtime, not an operator
     * kit overlay and not a WebAssembly feed plugin.
     */
    static boolean cataloged(SourceRegistry.Registration registration) {
        if (registration.dynamic() || registration.id().startsWith(KitOverlay.ID_PREFIX)) {
            return false;
        }
        return !(registration.adapter() instanceof WasmFeedAdapter);
    }

    /** The ids of every cataloged registered source. */
    private Set<String> catalogedIds() {
        Set<String> ids = new TreeSet<>();
        for (SourceRegistry.Registration registration : registry.all()) {
            if (cataloged(registration)) {
                ids.add(registration.id());
            }
        }
        return ids;
    }

    // ------------------------------------------------------------------------- parsing

    /**
     * One row of a parity table.
     *
     * @param line the 1-based line number in the catalog
     * @param idCell the first cell, as written
     * @param tier the Tier cell without emphasis
     * @param policy the Policy cell
     * @param license the License cell without back quotes
     * @param egress the Egress cell
     * @param ids the source ids and family patterns of the first cell
     */
    record Row(int line, String idCell, String tier, String policy, String license, String egress,
            List<IdPattern> ids) {

        /** True when the row's sources must not be registered. */
        boolean notImplemented() {
            return tier.toLowerCase(Locale.ROOT).equals(NOT_IMPLEMENTED);
        }

        /** True when the row defers its values to another table and is skipped. */
        boolean descriptive() {
            return tier.toLowerCase(Locale.ROOT).startsWith(DESCRIPTIVE_PREFIX);
        }

        /** True when one of the row's ids or patterns names {@code sourceId}. */
        boolean names(String sourceId) {
            return ids.stream().anyMatch(id -> id.matches(sourceId));
        }
    }

    /**
     * A back-quoted token of a first cell: a literal source id, or a family pattern compiled to a regular expression
     * whose capturing groups are the number ranges, in order.
     *
     * @param token the token as written
     * @param regex the compiled pattern
     * @param ranges the inclusive {@code [low, high]} bounds of each number range
     */
    record IdPattern(String token, Pattern regex, List<long[]> ranges) {

        /** Compiles {@code token}. */
        static IdPattern of(String token) {
            StringBuilder regex = new StringBuilder();
            List<long[]> ranges = new ArrayList<>();
            Matcher part = PATTERN_PART.matcher(token);
            int last = 0;
            while (part.find()) {
                regex.append(Pattern.quote(token.substring(last, part.start())));
                if (part.group(1) != null) {
                    regex.append("(\\d+)");
                    ranges.add(new long[] {Long.parseLong(part.group(1)), Long.parseLong(part.group(2))});
                } else {
                    regex.append(KEBAB);
                }
                last = part.end();
            }
            regex.append(Pattern.quote(token.substring(last)));
            return new IdPattern(token, Pattern.compile(regex.toString()), List.copyOf(ranges));
        }

        /** True when the token is a plain source id. */
        boolean literal() {
            return !PATTERN_PART.matcher(token).find();
        }

        /** True when {@code sourceId} is this id, or a member of this family. */
        boolean matches(String sourceId) {
            Matcher m = regex.matcher(sourceId);
            if (!m.matches()) {
                return false;
            }
            for (int i = 0; i < ranges.size(); i++) {
                long value = Long.parseLong(m.group(i + 1));
                if (value < ranges.get(i)[0] || value > ranges.get(i)[1]) {
                    return false;
                }
            }
            return true;
        }
    }

    /** The parity rows: neither {@code not implemented} nor descriptive. */
    static List<Row> parityRows(List<Row> rows) {
        return rows.stream().filter(row -> !row.notImplemented() && !row.descriptive()).toList();
    }

    /**
     * Every row of every parity table in {@code lines}. A table starts at a table line that follows a non-table
     * line; its first line is the header, and it is a parity table when the header's second to fifth cells are
     * {@link #PARITY_HEADER}.
     */
    static List<Row> parse(List<String> lines) {
        List<Row> rows = new ArrayList<>();
        boolean inTable = false;
        boolean parityTable = false;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).strip();
            if (!line.startsWith("|")) {
                inTable = false;
                continue;
            }
            List<String> cells = cells(line);
            if (!inTable) {
                inTable = true;
                parityTable = cells.size() >= 5 && cells.subList(1, 5).equals(PARITY_HEADER);
                continue;
            }
            if (!parityTable || cells.size() < 5 || cells.stream().allMatch(c -> DASHES.matcher(c).matches())) {
                continue;
            }
            rows.add(new Row(i + 1, cells.get(0), cells.get(1).replace("*", "").strip(), cells.get(2),
                    cells.get(3).replace("`", "").strip(), cells.get(4), ids(cells.get(0))));
        }
        return rows;
    }

    /** The trimmed cells of a table line, split on unescaped pipes. */
    static List<String> cells(String line) {
        String inner = line.strip();
        if (inner.startsWith("|")) {
            inner = inner.substring(1);
        }
        if (inner.endsWith("|") && !inner.endsWith("\\|")) {
            inner = inner.substring(0, inner.length() - 1);
        }
        List<String> out = new ArrayList<>();
        for (String cell : CELL_SEPARATOR.split(inner, -1)) {
            out.add(cell.strip());
        }
        return out;
    }

    /** The source ids and family patterns back-quoted in a first cell. */
    static List<IdPattern> ids(String idCell) {
        List<IdPattern> out = new ArrayList<>();
        Matcher m = TOKEN.matcher(idCell);
        while (m.find()) {
            out.add(IdPattern.of(m.group(1)));
        }
        return out;
    }

    /** The lines of {@code file}. */
    static List<String> read(Path file) {
        try {
            return Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + file, e);
        }
    }
}
