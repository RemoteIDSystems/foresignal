package ai.foresignal.source;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ParamValidatorTest {

    @TempDir Path inboxRoot;

    private ParamValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ParamValidator(new InboxFiles(inboxRoot),
                ai.foresignal.testsupport.net.TestOutbound.offlinePolicy());
    }

    private static SourceDescriptor.Builder base() {
        return SourceDescriptor.builder("test-source").kind("thing").defaultBudget(60, 1000);
    }

    private static SourceDescriptor withParam(ParamSpec spec) {
        return base().param(spec).build();
    }

    private ValidatedParams validate(SourceDescriptor descriptor, Map<String, Object> raw) {
        return validator.validate(descriptor, raw);
    }

    private List<FieldError> errorsOf(SourceDescriptor descriptor, Map<String, Object> raw) {
        try {
            validate(descriptor, raw);
            throw new AssertionError("Expected validation to fail");
        } catch (ValidationException e) {
            return e.getErrors();
        }
    }

    // ---------- types ----------

    @Nested
    @DisplayName("string")
    class Strings {

        @Test
        void acceptsAndTrims() {
            var d = withParam(ParamSpec.optional("feed", ParamType.STRING, "", null));

            assertThat(validate(d, Map.of("feed", "  all  ")).string("feed")).isEqualTo("all");
        }

        @Test
        @DisplayName("numbers and booleans are rendered as strings")
        void coercesScalars() {
            var d = withParam(ParamSpec.optional("feed", ParamType.STRING, "", null));

            assertThat(validate(d, Map.of("feed", 42)).string("feed")).isEqualTo("42");
            assertThat(validate(d, Map.of("feed", true)).string("feed")).isEqualTo("true");
        }
    }

    @Nested
    @DisplayName("integer")
    class Integers {

        @Test
        void acceptsIntegersAndNumericStrings() {
            var d = withParam(ParamSpec.optional("limit", ParamType.INTEGER, "", null));

            assertThat(validate(d, Map.of("limit", 500)).integer("limit")).isEqualTo(500);
            assertThat(validate(d, Map.of("limit", "500")).integer("limit")).isEqualTo(500);
            assertThat(validate(d, Map.of("limit", 500L)).integer("limit")).isEqualTo(500);
            // JSON numbers arrive as doubles; a whole-valued one is still an integer.
            assertThat(validate(d, Map.of("limit", 500.0)).integer("limit")).isEqualTo(500);
        }

        @Test
        void rejectsFractionsAndNonNumbers() {
            var d = withParam(ParamSpec.optional("limit", ParamType.INTEGER, "", null));

            assertThat(errorsOf(d, Map.of("limit", 1.5)))
                    .singleElement()
                    .satisfies(
                            e -> {
                                assertThat(e.field()).isEqualTo("params.limit");
                                assertThat(e.message()).contains("whole number");
                            });
            assertThat(errorsOf(d, Map.of("limit", "many"))).hasSize(1);
        }

        @Test
        void rejectsValuesOutsideIntRange() {
            var d = withParam(ParamSpec.optional("limit", ParamType.INTEGER, "", null));

            assertThat(errorsOf(d, Map.of("limit", Long.MAX_VALUE)))
                    .singleElement()
                    .satisfies(e -> assertThat(e.message()).contains("32-bit"));
        }
    }

    @Nested
    @DisplayName("number")
    class Numbers {

        @Test
        void acceptsNumbersAndStrings() {
            var d = withParam(ParamSpec.optional("minMag", ParamType.NUMBER, "", null));

            assertThat(validate(d, Map.of("minMag", 2.5)).number("minMag")).isEqualTo(2.5);
            assertThat(validate(d, Map.of("minMag", "2.5")).number("minMag")).isEqualTo(2.5);
            assertThat(validate(d, Map.of("minMag", 3)).number("minMag")).isEqualTo(3.0);
        }

        @Test
        @DisplayName("NaN and infinity are refused, since they cannot be stored as JSON")
        void rejectsNonFinite() {
            var d = withParam(ParamSpec.optional("minMag", ParamType.NUMBER, "", null));

            assertThat(errorsOf(d, Map.of("minMag", Double.NaN)))
                    .singleElement()
                    .satisfies(e -> assertThat(e.message()).contains("finite"));
            assertThat(errorsOf(d, Map.of("minMag", Double.POSITIVE_INFINITY))).hasSize(1);
        }
    }

    @Nested
    @DisplayName("boolean")
    class Booleans {

        @ParameterizedTest
        @CsvSource({"true, true", "false, false", "TRUE, true", "False, false"})
        void acceptsBooleansAndTheirStrings(String raw, boolean expected) {
            var d = withParam(ParamSpec.optional("includeDeleted", ParamType.BOOLEAN, "", null));

            assertThat(validate(d, Map.of("includeDeleted", raw)).bool("includeDeleted"))
                    .isEqualTo(expected);
        }

        @Test
        void rejectsAnythingElse() {
            var d = withParam(ParamSpec.optional("includeDeleted", ParamType.BOOLEAN, "", null));

            assertThat(errorsOf(d, Map.of("includeDeleted", "yes")))
                    .singleElement()
                    .satisfies(e -> assertThat(e.message()).contains("true or false"));
            assertThat(errorsOf(d, Map.of("includeDeleted", 1))).hasSize(1);
        }
    }

    @Nested
    @DisplayName("duration")
    class Durations {

        @Test
        void acceptsBothSpellingsAndBareSeconds() {
            var d = withParam(ParamSpec.optional("window", ParamType.DURATION, "", null));

            assertThat(validate(d, Map.of("window", "15m")).duration("window"))
                    .isEqualTo(Duration.ofMinutes(15));
            assertThat(validate(d, Map.of("window", "PT15M")).duration("window"))
                    .isEqualTo(Duration.ofMinutes(15));
            assertThat(validate(d, Map.of("window", 900)).duration("window"))
                    .isEqualTo(Duration.ofMinutes(15));
        }

        @Test
        @DisplayName("an unparseable duration reports the parser's own message")
        void rejectsGarbage() {
            var d = withParam(ParamSpec.optional("window", ParamType.DURATION, "", null));

            assertThat(errorsOf(d, Map.of("window", "soon")))
                    .singleElement()
                    .satisfies(
                            e -> {
                                assertThat(e.field()).isEqualTo("params.window");
                                assertThat(e.message()).contains("shorthand");
                            });
        }
    }

    @Nested
    @DisplayName("bbox")
    class Bbox {

        @Test
        void acceptsStringAndList() {
            var d = withParam(ParamSpec.optional("bbox", ParamType.BBOX, "", null));

            var fromString = validate(d, Map.of("bbox", "-123,37,-122,38")).bbox("bbox");
            assertThat(fromString.getMinX()).isEqualTo(-123);
            assertThat(fromString.getMaxY()).isEqualTo(38);

            var fromList = validate(d, Map.of("bbox", List.of(-123, 37, -122, 38))).bbox("bbox");
            assertThat(fromList).isEqualTo(fromString);
        }

        @Test
        void rejectsWrongLength() {
            var d = withParam(ParamSpec.optional("bbox", ParamType.BBOX, "", null));

            assertThat(errorsOf(d, Map.of("bbox", "-123,37,-122")))
                    .singleElement()
                    .satisfies(e -> assertThat(e.message()).contains("minLon,minLat,maxLon,maxLat"));
            assertThat(errorsOf(d, Map.of("bbox", List.of(1, 2, 3))))
                    .singleElement()
                    .satisfies(e -> assertThat(e.message()).contains("4 values"));
        }

        @ParameterizedTest
        @ValueSource(
                strings = {
                    "-181,37,-122,38",
                    "-123,37,181,38",
                    "-123,-91,-122,38",
                    "-123,37,-122,91",
                })
        @DisplayName("coordinates outside the WGS 84 range are rejected")
        void rejectsOutOfRange(String bbox) {
            var d = withParam(ParamSpec.optional("bbox", ParamType.BBOX, "", null));

            assertThat(errorsOf(d, Map.of("bbox", bbox)))
                    .singleElement()
                    .satisfies(e -> assertThat(e.message()).contains("outside"));
        }

        @Test
        @DisplayName("an inverted latitude range is rejected")
        void rejectsInvertedLatitude() {
            var d = withParam(ParamSpec.optional("bbox", ParamType.BBOX, "", null));

            assertThat(errorsOf(d, Map.of("bbox", "-123,38,-122,37")))
                    .singleElement()
                    .satisfies(e -> assertThat(e.message()).contains("above maxLat"));
        }

        @Test
        @DisplayName("minLon above maxLon is allowed: that is an antimeridian-crossing box")
        void allowsAntimeridianCrossing() {
            var d = withParam(ParamSpec.optional("bbox", ParamType.BBOX, "", null));

            assertThat(validate(d, Map.of("bbox", "170,-10,-170,10")).bbox("bbox")).isNotNull();
        }

        @Test
        void reportsEveryCoordinateProblemAtOnce() {
            var d = withParam(ParamSpec.optional("bbox", ParamType.BBOX, "", null));

            assertThat(errorsOf(d, Map.of("bbox", "-200,-100,200,100")))
                    .singleElement()
                    .satisfies(
                            e ->
                                    assertThat(e.message())
                                            .contains("minLon")
                                            .contains("maxLon")
                                            .contains("minLat")
                                            .contains("maxLat"));
        }

        @Test
        void rejectsNonNumeric() {
            var d = withParam(ParamSpec.optional("bbox", ParamType.BBOX, "", null));

            assertThat(errorsOf(d, Map.of("bbox", "a,b,c,d"))).hasSize(1);
        }
    }

    @Nested
    @DisplayName("string-list")
    class StringLists {

        @Test
        void acceptsListAndCsv() {
            var d = withParam(ParamSpec.optional("kinds", ParamType.STRING_LIST, "", null));

            assertThat(validate(d, Map.of("kinds", List.of("a", "b"))).stringList("kinds"))
                    .containsExactly("a", "b");
            assertThat(validate(d, Map.of("kinds", "a, b , c")).stringList("kinds"))
                    .containsExactly("a", "b", "c");
        }

        @Test
        void absentListIsEmptyNotNull() {
            var d = withParam(ParamSpec.optional("kinds", ParamType.STRING_LIST, "", null));

            assertThat(validate(d, Map.of()).stringList("kinds")).isEmpty();
        }

        @Test
        void rejectsNullEntries() {
            var d = withParam(ParamSpec.optional("kinds", ParamType.STRING_LIST, "", null));
            var raw = new LinkedHashMap<String, Object>();
            raw.put("kinds", java.util.Arrays.asList("a", null));

            assertThat(errorsOf(d, raw))
                    .singleElement()
                    .satisfies(e -> assertThat(e.message()).contains("null entry"));
        }
    }

    // ---------- defaults, required, allowed, unknown ----------

    @Nested
    @DisplayName("defaults and requirements")
    class DefaultsAndRequirements {

        @Test
        void appliesDefaultsWhenAbsent() {
            var d =
                    base().param(ParamSpec.optional("feed", ParamType.STRING, "", "all"))
                            .param(ParamSpec.optional("limit", ParamType.INTEGER, "", 100))
                            .build();

            ValidatedParams params = validate(d, Map.of());

            assertThat(params.string("feed")).isEqualTo("all");
            assertThat(params.integer("limit")).isEqualTo(100);
        }

        @Test
        void suppliedValueBeatsDefault() {
            var d = withParam(ParamSpec.optional("feed", ParamType.STRING, "", "all"));

            assertThat(validate(d, Map.of("feed", "mil")).string("feed")).isEqualTo("mil");
        }

        @Test
        @DisplayName("a parameter with no value and no default is simply absent")
        void absentWithoutDefaultIsAbsent() {
            var d = withParam(ParamSpec.optional("feed", ParamType.STRING, "", null));

            assertThat(validate(d, Map.of()).has("feed")).isFalse();
            assertThat(validate(d, Map.of()).string("feed")).isNull();
        }

        @Test
        void requiredMustBePresent() {
            var d = withParam(ParamSpec.required("apiFeed", ParamType.STRING, ""));

            assertThat(errorsOf(d, Map.of()))
                    .singleElement()
                    .satisfies(
                            e -> {
                                assertThat(e.field()).isEqualTo("params.apiFeed");
                                assertThat(e.message()).isEqualTo("is required");
                            });
        }

        @Test
        @DisplayName("a blank string does not satisfy a required parameter")
        void blankDoesNotSatisfyRequired() {
            var d = withParam(ParamSpec.required("apiFeed", ParamType.STRING, ""));

            assertThat(errorsOf(d, Map.of("apiFeed", "   "))).hasSize(1);
        }

        @Test
        void enforcesAllowedValues() {
            var d =
                    withParam(
                            ParamSpec.enumerated("feed", "", "mil", List.of("mil", "all", "ladd")));

            assertThat(validate(d, Map.of("feed", "all")).string("feed")).isEqualTo("all");
            assertThat(errorsOf(d, Map.of("feed", "everything")))
                    .singleElement()
                    .satisfies(e -> assertThat(e.message()).contains("must be one of"));
        }

        @Test
        @DisplayName("allowed values apply to each entry of a list")
        void enforcesAllowedPerListEntry() {
            var d =
                    withParam(
                            new ParamSpec(
                                    "kinds", ParamType.STRING_LIST, "", null, false, List.of("a", "b")));

            assertThat(validate(d, Map.of("kinds", List.of("a", "b"))).stringList("kinds")).hasSize(2);
            assertThat(errorsOf(d, Map.of("kinds", List.of("a", "z"))))
                    .singleElement()
                    .satisfies(e -> assertThat(e.message()).contains("'z'"));
        }

        @Test
        @DisplayName("an unknown key is rejected, not ignored, so a typo cannot pass silently")
        void rejectsUnknownKeys() {
            var d = withParam(ParamSpec.optional("feed", ParamType.STRING, "", "all"));

            assertThat(errorsOf(d, Map.of("fedd", "mil")))
                    .singleElement()
                    .satisfies(
                            e -> {
                                assertThat(e.field()).isEqualTo("params.fedd");
                                assertThat(e.message())
                                        .contains("is not a parameter of")
                                        .contains("[feed]");
                            });
        }

        @Test
        @DisplayName("every problem is aggregated into one failure")
        void aggregatesEveryError() {
            var d =
                    base().param(ParamSpec.required("needed", ParamType.STRING, ""))
                            .param(ParamSpec.optional("limit", ParamType.INTEGER, "", null))
                            .param(ParamSpec.optional("bbox", ParamType.BBOX, "", null))
                            .build();

            List<FieldError> errors =
                    errorsOf(d, Map.of("limit", "lots", "bbox", "1,2,3", "unknown", "x"));

            assertThat(errors)
                    .hasSize(4)
                    .extracting(FieldError::field)
                    .containsExactlyInAnyOrder(
                            "params.needed", "params.limit", "params.bbox", "params.unknown");
        }

        @Test
        @DisplayName("an invalid descriptor default is reported as such")
        void reportsInvalidDefault() {
            var d = withParam(ParamSpec.optional("limit", ParamType.INTEGER, "", "not-a-number"));

            assertThat(errorsOf(d, Map.of()))
                    .singleElement()
                    .satisfies(e -> assertThat(e.message()).contains("invalid default"));
        }

        @Test
        void noParamsMeansEmptyResult() {
            assertThat(validate(base().build(), Map.of()).values()).isEmpty();
            assertThat(validate(base().build(), null).values()).isEmpty();
        }
    }

    // ---------- LAN_URL ----------

    @Nested
    @DisplayName("LAN_URL")
    class LanUrls {

        private SourceDescriptor descriptor() {
            return withParam(ParamSpec.optional("baseUrl", ParamType.LAN_URL, "", null));
        }

        @ParameterizedTest
        @ValueSource(
                strings = {
                    "http://127.0.0.1:8080",
                    "http://localhost:8080",
                    "http://10.0.0.5",
                    "http://192.168.1.50:8080",
                    "http://172.16.0.1",
                    "http://[fd12:3456::1]:8080",
                    "ws://192.168.1.50:30003",
                })
        @DisplayName("a private or loopback address is accepted and its IP is pinned")
        void acceptsPrivateAddresses(String url) {
            ValidatedParams params = validate(descriptor(), Map.of("baseUrl", url));

            assertThat(params.lanUrl("baseUrl")).hasToString(url);
            assertThat(params.pinnedAddress("baseUrl"))
                    .as("the resolved address must be pinned, or DNS rebinding is possible")
                    .isPresent();
        }

        @Test
        @DisplayName("the pinned address is the one that was resolved at validation time")
        void pinsTheResolvedAddress() {
            ValidatedParams params = validate(descriptor(), Map.of("baseUrl", "http://127.0.0.1:8080"));

            assertThat(params.pinnedAddress("baseUrl"))
                    .get()
                    .extracting(java.net.InetAddress::getHostAddress)
                    .isEqualTo("127.0.0.1");
        }

        @ParameterizedTest
        @ValueSource(strings = {"http://8.8.8.8", "http://1.1.1.1:443", "https://93.184.216.34"})
        @DisplayName("a public address is rejected")
        void rejectsPublicAddresses(String url) {
            assertThat(errorsOf(descriptor(), Map.of("baseUrl", url)))
                    .singleElement()
                    .satisfies(e -> assertThat(e.message()).contains("public address"));
        }

        @Test
        @DisplayName("the cloud metadata address is rejected, with code URL_NOT_ALLOWED")
        void rejectsCloudMetadata() {
            // 100.100.100.200 is Alibaba's metadata endpoint, inside carrier-grade NAT.
            assertThat(errorsOf(descriptor(), Map.of("baseUrl", "http://100.100.100.200")))
                    .singleElement()
                    .satisfies(e -> assertThat(e.message()).contains("Cloud metadata"));
            try {
                validate(descriptor(), Map.of("baseUrl", "http://169.254.169.254/latest"));
                throw new AssertionError("expected a refusal");
            } catch (ValidationException e) {
                assertThat(e.getCode()).isEqualTo("URL_NOT_ALLOWED");
            }
        }

        @ParameterizedTest
        @ValueSource(strings = {"http://169.254.1.1", "http://[fe80::1]", "http://100.64.0.1", "http://0x7f.1",
            "http://2130706433", "http://127.1"})
        @DisplayName("link-local, shared 100.64/10 and non-dotted-quad IPv4 forms are rejected (035: LAN_ONLY)")
        void rejectsLinkLocalSharedAndObfuscated(String url) {
            assertThat(errorsOf(descriptor(), Map.of("baseUrl", url))).hasSize(1);
        }

        @ParameterizedTest
        @ValueSource(strings = {"not a url", "/relative/path", "192.168.1.1", "file:///etc/passwd"})
        void rejectsNonUrls(String url) {
            assertThat(errorsOf(descriptor(), Map.of("baseUrl", url))).hasSize(1);
        }

        @Test
        @DisplayName("only http, https, ws and wss are allowed")
        void rejectsOtherSchemes() {
            assertThat(errorsOf(descriptor(), Map.of("baseUrl", "ftp://192.168.1.1")))
                    .singleElement()
                    .satisfies(e -> assertThat(e.message()).contains("is not allowed"));
        }

        @Test
        @DisplayName("a name that does not resolve is rejected")
        void rejectsUnresolvableHost() {
            assertThat(
                            errorsOf(
                                    descriptor(),
                                    Map.of("baseUrl", "http://this-host-should-not-exist.invalid")))
                    .singleElement()
                    .satisfies(e -> assertThat(e.message()).contains("could not be resolved"));
        }
    }

    // ---------- INBOX_PATH ----------

    @Nested
    @DisplayName("INBOX_PATH")
    class InboxPaths {

        private SourceDescriptor descriptor() {
            return withParam(ParamSpec.optional("dir", ParamType.INBOX_PATH, "", null));
        }

        @Test
        @DisplayName("a path inside the inbox is accepted and resolved")
        void acceptsPathInsideInbox() throws Exception {
            Path nested = Files.createDirectories(inboxRoot.resolve("station-one"));

            ValidatedParams params = validate(descriptor(), Map.of("dir", "station-one"));

            assertThat(params.inboxPath("dir")).isEqualTo(nested.toRealPath());
        }

        @Test
        @DisplayName("an absolute path inside the inbox is accepted")
        void acceptsAbsolutePathInsideInbox() throws Exception {
            Path nested = Files.createDirectories(inboxRoot.resolve("station-two"));

            ValidatedParams params =
                    validate(descriptor(), Map.of("dir", nested.toAbsolutePath().toString()));

            assertThat(params.inboxPath("dir")).isEqualTo(nested.toRealPath());
        }

        @Test
        @DisplayName("a path outside the inbox is rejected")
        void rejectsPathOutsideInbox() {
            assertThat(errorsOf(descriptor(), Map.of("dir", "/etc")))
                    .singleElement()
                    .satisfies(e -> assertThat(e.message()).contains("outside the inbox"));
        }

        @Test
        @DisplayName("traversal out of the inbox is rejected")
        void rejectsTraversal() {
            assertThat(errorsOf(descriptor(), Map.of("dir", "../../etc")))
                    .singleElement()
                    .satisfies(e -> assertThat(e.message()).contains("outside the inbox"));
        }

        @Test
        @DisplayName("a symlink pointing out of the inbox is rejected")
        @DisabledOnOs(OS.WINDOWS)
        void rejectsSymlinkEscape() throws Exception {
            Path outside = Files.createDirectories(inboxRoot.getParent().resolve("outside-target"));
            Files.createSymbolicLink(inboxRoot.resolve("escape"), outside);

            assertThat(errorsOf(descriptor(), Map.of("dir", "escape")))
                    .singleElement()
                    .satisfies(e -> assertThat(e.message()).contains("outside the inbox"));
        }

        @Test
        @DisplayName("a symlink that stays inside the inbox is accepted")
        @DisabledOnOs(OS.WINDOWS)
        void acceptsSymlinkStayingInside() throws Exception {
            Path target = Files.createDirectories(inboxRoot.resolve("real-station"));
            Files.createSymbolicLink(inboxRoot.resolve("alias"), target);

            ValidatedParams params = validate(descriptor(), Map.of("dir", "alias"));

            assertThat(params.inboxPath("dir")).isEqualTo(target.toRealPath());
        }
    }

    // ---------- accessors ----------

    @Test
    @DisplayName("an accessor used with the wrong type is a bug, not bad input")
    void wrongAccessorTypeIsAnIllegalState() {
        var d = withParam(ParamSpec.optional("limit", ParamType.INTEGER, "", 5));
        ValidatedParams params = validate(d, Map.of());

        assertThatThrownBy(() -> params.duration("limit")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void accessorsFallBackWhenAbsent() {
        ValidatedParams empty = ValidatedParams.empty();

        assertThat(empty.integer("missing", 7)).isEqualTo(7);
        assertThat(empty.bool("missing", true)).isTrue();
        assertThat(empty.optionalString("missing")).isEmpty();
        assertThat(empty.pinnedAddress("missing")).isEmpty();
        assertThat(empty.stringList("missing")).isEmpty();
    }
}
