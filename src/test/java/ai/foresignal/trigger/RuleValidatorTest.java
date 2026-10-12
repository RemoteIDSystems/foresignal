package ai.foresignal.trigger;

import static ai.foresignal.trigger.TestRules.box;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.TriggerMode;
import ai.foresignal.store.TriggerRuleEntity;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@link RuleValidator}: every rule of prompt 017, task 1. */
class RuleValidatorTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    static final SourceDescriptor QUAKES = SourceDescriptor.builder("quakes").name("Quakes").kind("earthquake")
            .volatileFields("fetchedAt").mode(Mode.PUSH).defaultBudget(Budget.of(60, 10_000)).build();

    static final SourceDescriptor SHIPS = SourceDescriptor.builder("ships").name("Ships").kind("vessel")
            .mode(Mode.PUSH).defaultBudget(Budget.of(60, 10_000)).build();

    static final MultiPolygon FENCE = box(0, 0, 1, 1);

    SourceRegistry registry;
    GeofenceMaterializer geofences;
    @SuppressWarnings("unchecked")
    ObjectProvider<PlaceLookup> places = mock(ObjectProvider.class);
    JdbcTemplate jdbc;
    RuleValidator validator;

    @BeforeEach
    void setUp() {
        registry = mock(SourceRegistry.class);
        when(registry.contains("quakes")).thenReturn(true);
        when(registry.contains("ships")).thenReturn(true);
        when(registry.kinds()).thenReturn(Set.of("earthquake", "vessel"));
        when(registry.descriptors()).thenReturn(List.of(QUAKES, SHIPS));
        when(registry.find("quakes")).thenReturn(Optional.of(new SourceRegistry.Registration(QUAKES, null, false)));
        when(registry.find("ships")).thenReturn(Optional.of(new SourceRegistry.Registration(SHIPS, null, false)));
        geofences = mock(GeofenceMaterializer.class);
        when(geofences.polygonal(any())).thenReturn(FENCE);
        when(geofences.bufferLine(any(), anyDouble())).thenReturn(FENCE);
        when(geofences.bufferPoint(anyDouble(), anyDouble(), anyDouble())).thenReturn(FENCE);
        when(geofences.offset(any(), anyDouble())).thenReturn(FENCE);
        jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForObject(anyString(), eq(Long.class))).thenReturn(1L);
        validator = new RuleValidator(registry, OperatorRegistry.builtIn(), geofences, places, jdbc);
    }

    static JsonNode json(String text) {
        return JSON.readTree(text.replace('\'', '"'));
    }

    ValidatedRule valid(String body) {
        return validator.validate(json(body));
    }

    ValidationException invalid(String body) {
        return (ValidationException) org.assertj.core.api.Assertions.catchThrowable(() -> validator.validate(json(body)));
    }

    static void hasField(ValidationException e, String field) {
        assertThat(e).isNotNull();
        assertThat(e.getErrors()).extracting(err -> err.field()).contains(field);
    }

    @Nested
    @DisplayName("basics")
    class Basics {

        @Test
        @DisplayName("a minimal rule gets every default")
        void defaults() {
            ValidatedRule rule = valid("{'name':'Big','sources':['quakes'],'conditions':[{'path':'mag','op':'>=','value':6}]}");
            assertThat(rule.enabled()).isTrue();
            assertThat(rule.mode()).isEqualTo(TriggerMode.MATCH);
            assertThat(rule.severity()).isEqualTo(Severity.WARNING);
            assertThat(rule.cooldown()).isZero();
            assertThat(rule.cooldownScope()).isEqualTo(CooldownScope.RECORD);
            assertThat(rule.reappearAsEnter()).isFalse();
            assertThat(rule.autoResolve()).isNull();
            assertThat(rule.enrichRadiusKm()).isEqualTo(100);
            assertThat(rule.enrichWindow()).isEqualTo(Duration.ofHours(24));
            assertThat(rule.hysteresisMeters()).isZero();
            assertThat(rule.conditions()).singleElement().satisfies(c -> assertThat(c.value()).isEqualTo(new BigDecimal("6")));
        }

        @Test
        @DisplayName("name is required and at most 200 characters")
        void name() {
            hasField(invalid("{'sources':['quakes']}"), "name");
            hasField(invalid("{'name':'','sources':['quakes']}"), "name");
            hasField(invalid("{'name':'" + "x".repeat(201) + "','sources':['quakes']}"), "name");
        }

        @Test
        @DisplayName("the body must be an object, and a rule needs at least one filter")
        void shape() {
            assertThatThrownBy(() -> validator.validate(json("[]"))).isInstanceOf(ValidationException.class);
            hasField(invalid("{'name':'x'}"), "sources");
        }

        @Test
        @DisplayName("applyTo writes every field onto the entity")
        void applyTo() {
            ValidatedRule rule = valid("{'name':'n','kinds':['vessel'],'tags':['a'],'geofence':{'type':'Polygon','coordinates':"
                    + "[[[0,0],[1,0],[1,1],[0,1],[0,0]]]},'mode':'enter','cooldown':'15m','severity':'critical'}");
            TriggerRuleEntity entity = new TriggerRuleEntity(UUID.randomUUID(), "old");
            rule.applyTo(entity);
            assertThat(entity.getName()).isEqualTo("n");
            assertThat(entity.getKinds()).containsExactly("vessel");
            assertThat(entity.getTags()).containsExactly("a");
            assertThat(entity.getGeofence()).isEqualTo(FENCE);
            assertThat(entity.getMode()).isEqualTo(TriggerMode.ENTER);
            assertThat(entity.getCooldownSeconds()).isEqualTo(900);
            assertThat(entity.getSeverity()).isEqualTo("critical");
            assertThat(entity.isReappearAsEnter()).isTrue();
            assertThat(entity.getAutoResolve()).containsEntry("onInverseEdge", true);
        }
    }

    @Nested
    @DisplayName("sources, kinds and tags")
    class Filters {

        @Test
        @DisplayName("the deprecated scalars fold into the arrays; both at once is 400")
        void aliases() {
            ValidatedRule rule = valid("{'name':'x','source':'quakes','kind':'earthquake'}");
            assertThat(rule.sources()).containsExactly("quakes");
            assertThat(rule.kinds()).containsExactly("earthquake");
            hasField(invalid("{'name':'x','source':'quakes','sources':['ships']}"), "source");
            hasField(invalid("{'name':'x','kind':'a','kinds':['b']}"), "kind");
        }

        @Test
        @DisplayName("an unknown source is 400; an unknown kind is only a warning")
        void unknown() {
            hasField(invalid("{'name':'x','sources':['quakes','nope']}"), "sources[1]");
            ValidatedRule rule = valid("{'name':'x','kinds':['mystery']}");
            assertThat(rule.kinds()).containsExactly("mystery");
            assertThat(rule.warnings()).singleElement().asString().contains("mystery");
        }

        @Test
        @DisplayName("at most 50 sources or kinds")
        void limits() {
            StringBuilder kinds = new StringBuilder();
            for (int i = 0; i < 51; i++) {
                kinds.append(i == 0 ? "" : ",").append("'k").append(i).append("'");
            }
            hasField(invalid("{'name':'x','kinds':[" + kinds + "]}"), "kinds");
        }

        @Test
        @DisplayName("tags: at most 16, each lowercase and short")
        void tags() {
            hasField(invalid("{'name':'x','sources':['quakes'],'tags':['Bad']}"), "tags[0]");
            hasField(invalid("{'name':'x','sources':['quakes'],'tags':['" + "a".repeat(33) + "']}"), "tags[0]");
            StringBuilder tags = new StringBuilder();
            for (int i = 0; i < 17; i++) {
                tags.append(i == 0 ? "" : ",").append("'t").append(i).append("'");
            }
            hasField(invalid("{'name':'x','sources':['quakes'],'tags':[" + tags + "]}"), "tags");
            assertThat(valid("{'name':'x','sources':['quakes'],'tags':['a-1','b_2']}").tags()).containsExactly("a-1", "b_2");
        }
    }

    @Nested
    @DisplayName("conditions")
    class Conditions {

        @ParameterizedTest
        @ValueSource(strings = {"bad-path", ".mag", "mag.", "a..b", ""})
        @DisplayName("paths must match the grammar")
        void paths(String path) {
            hasField(invalid("{'name':'x','sources':['quakes'],'conditions':[{'path':'" + path + "','op':'>','value':1}]}"),
                    "conditions[0].path");
        }

        @Test
        @DisplayName("unknown operators and missing values")
        void ops() {
            hasField(invalid("{'name':'x','sources':['quakes'],'conditions':[{'path':'a','op':'gt','value':1}]}"), "conditions[0].op");
            hasField(invalid("{'name':'x','sources':['quakes'],'conditions':[{'path':'a','op':'>'}]}"), "conditions[0].value");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "{'path':'a','op':'>','value':'6'}",
            "{'path':'a','op':'<=','value':true}",
            "{'path':'a','op':'==','value':[1]}",
            "{'path':'a','op':'in','value':[]}",
            "{'path':'a','op':'in','value':[1,'a']}",
            "{'path':'a','op':'notIn','value':5}",
            "{'path':'a','op':'in','value':[[1]]}",
            "{'path':'a','op':'exists','value':1}",
            "{'path':'a','op':'startsWith','value':''}",
            "{'path':'a','op':'startsWith','value':5}",
            "{'path':'a','op':'matches','value':'(a)\\\\1'}",
            "{'path':'a','op':'matches','value':'(?=a)b'}",
            "{'path':'a','op':'matches','value':'(?<=a)b'}",
            "{'path':'a','op':'matches','value':'(a+)+'}",
            "{'path':'a','op':'matches','value':'[unclosed'}",
        })
        @DisplayName("values must suit their operator")
        void values(String condition) {
            hasField(invalid("{'name':'x','sources':['quakes'],'conditions':[" + condition + "]}"), "conditions[0].value");
        }

        @Test
        @DisplayName("in accepts up to 1,000 values and startsWith up to 256 characters")
        void sizes() {
            StringBuilder values = new StringBuilder();
            for (int i = 0; i < 1001; i++) {
                values.append(i == 0 ? "" : ",").append(i);
            }
            hasField(invalid("{'name':'x','sources':['quakes'],'conditions':[{'path':'a','op':'in','value':[" + values + "]}]}"),
                    "conditions[0].value");
            hasField(invalid("{'name':'x','sources':['quakes'],'conditions':[{'path':'a','op':'startsWith','value':'"
                    + "x".repeat(257) + "'}]}"), "conditions[0].value");
        }

        @Test
        @DisplayName("a path on a volatile field of an applicable source is TRIGGER_PATH_VOLATILE")
        void volatilePaths() {
            ValidationException bySource = invalid(
                    "{'name':'x','sources':['quakes'],'conditions':[{'path':'fetchedAt.x','op':'exists','value':true}]}");
            assertThat(bySource.getCode()).isEqualTo("TRIGGER_PATH_VOLATILE");
            ValidationException byKind = invalid(
                    "{'name':'x','kinds':['earthquake'],'conditions':[{'path':'fetchedAt','op':'exists','value':true}]}");
            assertThat(byKind.getCode()).isEqualTo("TRIGGER_PATH_VOLATILE");
            assertThat(valid("{'name':'x','sources':['ships'],'conditions':[{'path':'fetchedAt','op':'exists','value':true}]}"))
                    .isNotNull();
        }
    }

    @Nested
    @DisplayName("geofences and near")
    class Geofences {

        static final String LINE = "{'type':'LineString','coordinates':[[0,0],[1,1]]}";
        static final String POLYGON = "{'type':'Polygon','coordinates':[[[0,0],[1,0],[1,1],[0,1],[0,0]]]}";

        @Test
        @DisplayName("a line needs bufferMeters 1..50,000 and keeps the route for display")
        void lines() {
            hasField(invalid("{'name':'x','geofence':" + LINE + "}"), "bufferMeters");
            hasField(invalid("{'name':'x','geofence':" + LINE + ",'bufferMeters':0}"), "bufferMeters");
            hasField(invalid("{'name':'x','geofence':" + LINE + ",'bufferMeters':50001}"), "bufferMeters");
            ValidatedRule rule = valid("{'name':'x','geofence':" + LINE + ",'bufferMeters':500}");
            verify(geofences).bufferLine(any(Geometry.class), eq(500.0));
            assertThat(rule.geofenceRoute()).containsEntry("type", "LineString");
            assertThat(rule.bufferMeters()).isEqualTo(500.0);
            assertThat(rule.reappearAsEnter()).isTrue();
        }

        @Test
        @DisplayName("geofenceRoute plus bufferMeters is the route input of a rule read back")
        void routeRoundTrip() {
            ValidatedRule rule = valid("{'name':'x','geofence':" + POLYGON + ",'geofenceRoute':" + LINE + ",'bufferMeters':250}");
            verify(geofences).bufferLine(any(Geometry.class), eq(250.0));
            assertThat(rule.bufferMeters()).isEqualTo(250.0);
        }

        @Test
        @DisplayName("bufferMeters without a line is 400; points are not fences")
        void wrongShapes() {
            hasField(invalid("{'name':'x','geofence':" + POLYGON + ",'bufferMeters':5}"), "bufferMeters");
            hasField(invalid("{'name':'x','sources':['quakes'],'bufferMeters':5}"), "bufferMeters");
            hasField(invalid("{'name':'x','geofence':{'type':'Point','coordinates':[0,0]}}"), "geofence");
            hasField(invalid("{'name':'x','geofence':{'type':'Polygon'}}"), "geofence");
        }

        @Test
        @DisplayName("an empty fence after fixing is 400")
        void empty() {
            when(geofences.polygonal(any())).thenReturn(TestRules.GEO.createMultiPolygon());
            hasField(invalid("{'name':'x','geofence':" + POLYGON + "}"), "geofence");
        }

        @Test
        @DisplayName("near a point is buffered by its radius in meters; geofence and near together are 400")
        void nearPoint() {
            ValidatedRule rule = valid("{'name':'x','near':{'point':[-97.5,30.2],'radiusKm':20}}");
            verify(geofences).bufferPoint(-97.5, 30.2, 20_000.0);
            assertThat(rule.near()).containsEntry("radiusKm", 20.0);
            assertThat(rule.geofence()).isEqualTo(FENCE);
            hasField(invalid("{'name':'x','geofence':" + POLYGON + ",'near':{'point':[0,0],'radiusKm':1}}"), "near");
            hasField(invalid("{'name':'x','near':{'point':[0,0],'radiusKm':0.05}}"), "near.radiusKm");
            hasField(invalid("{'name':'x','near':{'point':[0],'radiusKm':5}}"), "near.point");
            hasField(invalid("{'name':'x','near':{'point':[200,0],'radiusKm':5}}"), "near.point");
        }

        @Test
        @DisplayName("near a place without Places is PLACES_UNAVAILABLE")
        void nearPlace() {
            assertThat(invalid("{'name':'x','near':{'placeId':'home','radiusKm':5}}").getCode()).isEqualTo("PLACES_UNAVAILABLE");
        }

        @Test
        @DisplayName("a moving anchor (prompt 036) stores no fence; its source must exist and its fields are checked")
        void nearAnchor() {
            ValidatedRule rule = valid("{'name':'x','mode':'enter','near':{'anchor':{'source':'quakes','externalId':'phone'},"
                    + "'radiusKm':150}}");
            assertThat(rule.geofence()).isNull();
            assertThat(rule.near()).containsKey("anchor").containsEntry("radiusKm", 150.0)
                    .containsEntry("maxAnchorAge", "PT30M").containsEntry("minMoveMeters", 200.0);
            hasField(invalid("{'name':'x','near':{'anchor':{'source':'a','externalId':'b'},'radiusKm':5}}"),
                    "near.anchor.source");
            hasField(invalid("{'name':'x','near':{'anchor':{'source':'quakes'},'radiusKm':5}}"), "near.anchor");
            hasField(invalid("{'name':'x','near':{'anchor':{'source':'quakes','externalId':'p'},'radiusKm':2001}}"),
                    "near.radiusKm");
            hasField(invalid("{'name':'x','near':{'anchor':{'source':'quakes','externalId':'p'},'radiusKm':5,"
                    + "'maxAnchorAge':'soon'}}"), "near.maxAnchorAge");
            hasField(invalid("{'name':'x','near':{'anchor':{'source':'quakes','externalId':'p'},'radiusKm':5,"
                    + "'minMoveMeters':-1}}"), "near.minMoveMeters");
            hasField(invalid("{'name':'x','mode':'enter','hysteresisMeters':50,'near':{'anchor':{'source':'quakes',"
                    + "'externalId':'p'},'radiusKm':5}}"), "hysteresisMeters");
            valid("{'name':'x','near':{'anchor':{'source':'quakes','externalId':'p'},'radiusKm':0}}");
        }

        @Test
        @DisplayName("near a place resolves through the PlaceLookup when there is one")
        void nearPlaceLookup() {
            PlaceLookup lookup = id -> "home".equals(id) ? Optional.of(TestRules.point(1, 2)) : Optional.empty();
            when(places.getIfAvailable()).thenReturn(lookup);
            valid("{'name':'x','near':{'placeId':'home','radiusKm':5}}");
            verify(geofences).bufferPoint(1.0, 2.0, 5_000.0);
            hasField(invalid("{'name':'x','near':{'placeId':'away','radiusKm':5}}"), "near.placeIds");
        }

        @Test
        @DisplayName("near several places (prompt 036) unions their areas, buffered by radiusKm, and keeps their versions")
        void nearPlaces() {
            PlaceLookup lookup = new PlaceLookup() {
                @Override
                public Optional<org.locationtech.jts.geom.Geometry> geometry(String id) {
                    return switch (id) {
                        case "home" -> Optional.of(box(0, 0, 1, 1));
                        case "work" -> Optional.of(box(2, 2, 3, 3));
                        default -> Optional.empty();
                    };
                }

                @Override
                public Optional<Integer> version(String id) {
                    return Optional.of("home".equals(id) ? 3 : 7);
                }
            };
            when(places.getIfAvailable()).thenReturn(lookup);
            ValidatedRule rule = valid("{'name':'x','near':{'placeIds':['home','work'],'radiusKm':2}}");
            verify(geofences, org.mockito.Mockito.atLeastOnce()).offset(any(), eq(2_000.0));
            assertThat(rule.near()).containsEntry("placeIds", List.of("home", "work"))
                    .containsEntry(RuleValidator.PLACE_VERSIONS, List.of(3, 7));
            valid("{'name':'x','near':{'placeId':'home'}}");
            hasField(invalid("{'name':'x','near':{'placeIds':[]}}"), "near.placeIds");
            hasField(invalid("{'name':'x','near':{'placeIds':['home'],'radiusKm':-1}}"), "near.radiusKm");
            hasField(invalid("{'name':'x','near':{'placeIds':['home'],'extra':1}}"), "near.extra");
        }
    }

    @Nested
    @DisplayName("modes, hysteresis and auto-resolve")
    class Modes {

        static final String FENCED = "'geofence':{'type':'Polygon','coordinates':[[[0,0],[1,0],[1,1],[0,1],[0,0]]]}";

        @Test
        @DisplayName("edge modes need a geofence, near or a condition")
        void edgeModes() {
            hasField(invalid("{'name':'x','sources':['quakes'],'mode':'enter'}"), "mode");
            hasField(invalid("{'name':'x','sources':['quakes'],'mode':'sideways'}"), "mode");
            assertThat(valid("{'name':'x','sources':['quakes'],'mode':'exit','conditions':[{'path':'a','op':'>','value':1}]}").mode())
                    .isEqualTo(TriggerMode.EXIT);
        }

        @Test
        @DisplayName("autoResolve: all on for enter, off for match, refused on exit; true, false and objects")
        void autoResolve() {
            assertThat(valid("{'name':'x'," + FENCED + ",'mode':'enter'}").autoResolve()).isEqualTo(AutoResolve.ALL);
            assertThat(valid("{'name':'x'," + FENCED + ",'mode':'enter','autoResolve':null}").autoResolve()).isNull();
            assertThat(valid("{'name':'x'," + FENCED + ",'autoResolve':true}").autoResolve()).isEqualTo(AutoResolve.ALL);
            assertThat(valid("{'name':'x'," + FENCED + ",'mode':'enter','autoResolve':false}").autoResolve()).isNull();
            assertThat(valid("{'name':'x'," + FENCED + ",'mode':'enter','autoResolve':{'onExpiry':false,'staleAfter':'2h'}}")
                    .autoResolve()).isEqualTo(new AutoResolve(true, true, false, Duration.ofHours(2)));
            hasField(invalid("{'name':'x'," + FENCED + ",'mode':'exit','autoResolve':true}"), "autoResolve");
            assertThat(valid("{'name':'x'," + FENCED + ",'mode':'exit'}").autoResolve()).isNull();
            hasField(invalid("{'name':'x'," + FENCED + ",'mode':'enter','autoResolve':{'onTombstone':'yes'}}"),
                    "autoResolve.onTombstone");
        }

        @Test
        @DisplayName("hysteresis needs an edge-mode fence; a fence it consumes is GEOFENCE_TOO_SMALL_FOR_HYSTERESIS")
        void hysteresis() {
            hasField(invalid("{'name':'x'," + FENCED + ",'hysteresisMeters':100}"), "hysteresisMeters");
            hasField(invalid("{'name':'x','sources':['quakes'],'conditions':[{'path':'a','op':'>','value':1}],'mode':'enter',"
                    + "'hysteresisMeters':100}"), "hysteresisMeters");
            hasField(invalid("{'name':'x'," + FENCED + ",'mode':'enter','hysteresisMeters':5001}"), "hysteresisMeters");
            ValidatedRule rule = valid("{'name':'x'," + FENCED + ",'mode':'enter','hysteresisMeters':100}");
            verify(geofences).offset(FENCE, -100.0);
            verify(geofences).offset(FENCE, 100.0);
            assertThat(rule.geofenceInner()).isNotNull();
            assertThat(rule.geofenceOuter()).isNotNull();
            when(geofences.offset(any(), eq(-5000.0))).thenReturn(TestRules.GEO.createMultiPolygon());
            assertThat(invalid("{'name':'x'," + FENCED + ",'mode':'enter','hysteresisMeters':5000}").getCode())
                    .isEqualTo("GEOFENCE_TOO_SMALL_FOR_HYSTERESIS");
        }
    }

    @Nested
    @DisplayName("time, storm, horizon and template")
    class Other {

        @Test
        @DisplayName("durations, scopes and severities are checked")
        void scalars() {
            hasField(invalid("{'name':'x','sources':['quakes'],'cooldown':'soon'}"), "cooldown");
            hasField(invalid("{'name':'x','sources':['quakes'],'cooldownScope':'entity'}"), "cooldownScope");
            hasField(invalid("{'name':'x','sources':['quakes'],'severity':'high'}"), "severity");
            hasField(invalid("{'name':'x','sources':['quakes'],'enrichRadiusKm':0.5}"), "enrichRadiusKm");
            hasField(invalid("{'name':'x','sources':['quakes'],'enrichWindow':'5m'}"), "enrichWindow");
            hasField(invalid("{'name':'x','sources':['quakes'],'enabled':'yes'}"), "enabled");
            assertThat(valid("{'name':'x','sources':['quakes'],'cooldown':'PT15M','cooldownScope':'rule'}").cooldownScope())
                    .isEqualTo(CooldownScope.RULE);
        }

        @Test
        @DisplayName("maxAlertsPerHour is a non-negative integer or null")
        void storm() {
            hasField(invalid("{'name':'x','sources':['quakes'],'maxAlertsPerHour':-1}"), "maxAlertsPerHour");
            hasField(invalid("{'name':'x','sources':['quakes'],'maxAlertsPerHour':1.5}"), "maxAlertsPerHour");
            assertThat(valid("{'name':'x','sources':['quakes'],'maxAlertsPerHour':0}").maxAlertsPerHour()).isZero();
            assertThat(valid("{'name':'x','sources':['quakes']}").maxAlertsPerHour()).isNull();
        }

        @Test
        @DisplayName("activeFrom must precede activeUntil; activeWindow is checked field by field")
        void active() {
            hasField(invalid("{'name':'x','sources':['quakes'],'activeFrom':'2026-01-02T00:00:00Z','activeUntil':'2026-01-01T00:00:00Z'}"),
                    "activeUntil");
            hasField(invalid("{'name':'x','sources':['quakes'],'activeFrom':'yesterday'}"), "activeFrom");
            hasField(invalid("{'name':'x','sources':['quakes'],'activeWindow':{'timezone':'Mars/Olympus','windows':"
                    + "[{'days':['MON'],'start':'08:00','end':'17:00'}]}}"), "activeWindow.timezone");
            hasField(invalid("{'name':'x','sources':['quakes'],'activeWindow':{'timezone':'UTC','windows':[]}}"), "activeWindow.windows");
            hasField(invalid("{'name':'x','sources':['quakes'],'activeWindow':{'timezone':'UTC','windows':"
                    + "[{'days':['MONDAY'],'start':'08:00','end':'17:00'}]}}"), "activeWindow.windows[0].days");
            hasField(invalid("{'name':'x','sources':['quakes'],'activeWindow':{'timezone':'UTC','windows':"
                    + "[{'days':['MON'],'start':'8:00','end':'17:00'}]}}"), "activeWindow.windows[0].start");
            hasField(invalid("{'name':'x','sources':['quakes'],'activeWindow':{'timezone':'UTC','windows':"
                    + "[{'days':['MON'],'start':'08:00','end':'08:00'}]}}"), "activeWindow.windows[0].end");
            ValidatedRule rule = valid("{'name':'x','sources':['quakes'],'activeWindow':{'timezone':'America/Chicago','windows':"
                    + "[{'days':['FRI','SAT'],'start':'22:00','end':'07:00'}]}}");
            assertThat(rule.activeWindow().toMap()).containsEntry("timezone", "America/Chicago");
        }

        @Test
        @DisplayName("forecastHorizon is FEATURE_UNAVAILABLE without record_version.valid_from")
        void horizon() {
            assertThat(valid("{'name':'x','sources':['quakes'],'forecastHorizon':'6h'}").forecastHorizon())
                    .isEqualTo(Duration.ofHours(6));
            when(jdbc.queryForObject(anyString(), eq(Long.class))).thenReturn(0L);
            RuleValidator without = new RuleValidator(registry, OperatorRegistry.builtIn(), geofences, places, jdbc);
            assertThat(((ValidationException) org.assertj.core.api.Assertions.catchThrowable(
                    () -> without.validate(json("{'name':'x','sources':['quakes'],'forecastHorizon':'6h'}")))).getCode())
                    .isEqualTo("FEATURE_UNAVAILABLE");
        }

        @Test
        @DisplayName("messageTemplate is at most 1,000 characters")
        void template() {
            hasField(invalid("{'name':'x','sources':['quakes'],'messageTemplate':'" + "x".repeat(1001) + "'}"), "messageTemplate");
            assertThat(valid("{'name':'x','sources':['quakes'],'messageTemplate':'M{{payload.mag}}'}").messageTemplate())
                    .isEqualTo("M{{payload.mag}}");
        }

        @Test
        @DisplayName("several field problems are reported together as VALIDATION_FAILED")
        void collected() {
            ValidationException e = invalid("{'name':'','severity':'x','cooldown':'y','sources':['quakes']}");
            assertThat(e.getCode()).isEqualTo("VALIDATION_FAILED");
            assertThat(e.getErrors()).extracting(err -> err.field()).contains("name", "severity", "cooldown");
        }
    }
}
