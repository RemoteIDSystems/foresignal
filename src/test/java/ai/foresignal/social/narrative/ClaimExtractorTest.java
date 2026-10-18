package ai.foresignal.social.narrative;

import static ai.foresignal.testsupport.social.NarrativeTestSupport.extract;
import static ai.foresignal.testsupport.social.NarrativeTestSupport.locality;
import static ai.foresignal.testsupport.social.NarrativeTestSupport.places;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The deterministic claim extractor (prompt 136): lexicon matches in English, Arabic, Hebrew, Persian and Russian;
 * negation, denial, questions and hedges; place resolution with ambiguity and the longest match; entities by IMO
 * number; time expressions relative to the post.
 */
class ClaimExtractorTest {

    static final Instant AT = Instant.parse("2026-10-17T14:30:00Z");

    static final PlaceIndex PLACES = places(Map.of(
            "Aden", List.of(locality("Aden", 45.03, 12.78, 1_000_000, "YE")),
            "Tehran", List.of(locality("Tehran", 51.42, 35.69, 8_700_000, "IR")),
            "تهران", List.of(locality("Tehran", 51.42, 35.69, 8_700_000, "IR")),
            "Springfield", List.of(locality("Springfield", -89.65, 39.80, 115_000, "US-IL"),
                    locality("Springfield", -72.59, 42.10, 155_000, "US-MA"),
                    locality("Springfield", -93.29, 37.21, 170_000, "US-MO")),
            "New York", List.of(locality("New York", -74.0, 40.7, 8_000_000, "US-NY")),
            "York", List.of(locality("York", -1.08, 53.96, 150_000, "GB"))));

    private static ClaimExtractor.PostClaim only(ClaimExtractor.Extraction e, String type) {
        return e.claims().stream().filter(c -> c.type().equals(type)).findFirst().orElseThrow(
                () -> new AssertionError("no " + type + " claim in " + e.claims()));
    }

    @Test
    @DisplayName("English: explosion and fire in Aden, asserted, with place, family and confidence")
    void english() {
        ClaimExtractor.Extraction e = extract("Huge explosion and fire at the port of Aden right now", AT, PLACES);
        ClaimExtractor.PostClaim explosion = only(e, "explosion");
        assertThat(explosion.signalFamily()).isEqualTo("conflict");
        assertThat(explosion.polarity()).isEqualTo("asserted");
        assertThat(explosion.hedged()).isFalse();
        assertThat(explosion.place().name()).isEqualTo("Aden");
        assertThat(explosion.place().regionCode()).isEqualTo("YE");
        assertThat(explosion.place().precision()).isEqualTo("locality");
        assertThat(explosion.confidence()).isGreaterThan(ClaimExtractor.BASE_CONFIDENCE);
        assertThat(only(e, "fire").signalFamily()).isEqualTo("fire");
        assertThat(e.regionCodes()).containsExactly("YE");
        assertThat(explosion.eventTime()).isNotNull();
        assertThat(explosion.eventTime().precision()).isEqualTo("minute");
    }

    @Test
    @DisplayName("Arabic, Hebrew, Persian and Russian event terms, with proclitics and inflections")
    void multilingual() {
        assertThat(only(extract("انفجار كبير في الميناء", AT, PLACES), "explosion").polarity()).isEqualTo("asserted");
        assertThat(extract("والانفجار هز المدينة", AT, PLACES).claims()).extracting(ClaimExtractor.PostClaim::type)
                .contains("explosion");
        assertThat(extract("פיצוץ חזק נשמע בעיר", AT, PLACES).claims()).extracting(ClaimExtractor.PostClaim::type)
                .contains("explosion");
        ClaimExtractor.Extraction fa = extract("انفجار در تهران", AT, PLACES);
        assertThat(only(fa, "explosion").place().name()).isEqualTo("Tehran");
        assertThat(extract("Мощный взрыв и пожар на заводе", AT, PLACES).claims())
                .extracting(ClaimExtractor.PostClaim::type).contains("explosion", "fire");
        assertThat(extract("Сообщают о взрывах", AT, PLACES).claims()).extracting(ClaimExtractor.PostClaim::type)
                .contains("explosion");
    }

    @Test
    @DisplayName("negation within three words and denials make a claim denied; questions questioned; hedges hedged")
    void polarity() {
        assertThat(only(extract("There was no explosion in Aden", AT, PLACES), "explosion").polarity())
                .isEqualTo("denied");
        assertThat(only(extract("Officials denied reports of an explosion", AT, PLACES), "explosion").polarity())
                .isEqualTo("denied");
        assertThat(only(extract("Explosion in Aden?", AT, PLACES), "explosion").polarity()).isEqualTo("questioned");
        assertThat(only(extract("Can anyone confirm an explosion near the port", AT, PLACES), "explosion")
                .polarity()).isEqualTo("questioned");
        ClaimExtractor.PostClaim hedged = only(extract("Reportedly an explosion in Aden, unconfirmed", AT, PLACES),
                "explosion");
        assertThat(hedged.hedged()).isTrue();
        assertThat(hedged.polarity()).isEqualTo("asserted");
        assertThat(only(extract("نفى الجيش وقوع انفجار", AT, PLACES), "explosion").polarity()).isEqualTo("denied");
        assertThat(only(extract("Сообщают о взрыве, якобы на складе", AT, PLACES), "explosion").hedged()).isTrue();
        assertThat(only(extract("No electricity since the morning", AT, PLACES), "outage").polarity())
                .as("a negation inside the event term itself does not negate it").isEqualTo("asserted");
    }

    @Test
    @DisplayName("places: longest match wins; ambiguous names pick the most populous with lower confidence")
    void placeResolution() {
        ClaimExtractor.PostClaim ny = only(extract("Blackout across New York tonight", AT, PLACES), "blackout");
        assertThat(ny.place().name()).isEqualTo("New York");
        assertThat(ny.place().regionCode()).isEqualTo("US-NY");
        ClaimExtractor.PostClaim spring = only(extract("Fire in Springfield", AT, PLACES), "fire");
        assertThat(spring.place().regionCode()).isEqualTo("US-MO");
        assertThat(spring.place().confidence()).isLessThan(0.5);
        ClaimExtractor.PostClaim aden = only(extract("Fire in Aden", AT, PLACES), "fire");
        assertThat(aden.place().confidence()).isEqualTo(0.9);
        assertThat(extract("Fire somewhere", AT, PLACES).claims().get(0).place()).isNull();
    }

    @Test
    @DisplayName("an operator place outranks gazetteer candidates")
    void operatorPlace() {
        PlaceIndex index = places(Map.of("Harbor Gate", List.of(new PlaceIndex.Candidate("place:P1", "Harbor Gate",
                10.0, 20.0, "point", 0, null), locality("Harbor Gate", 1, 1, 5_000_000, "XX"))));
        ClaimExtractor.PostClaim c = only(extract("Smoke at Harbor Gate", AT, index), "smoke");
        assertThat(c.place().ref()).isEqualTo("place:P1");
        assertThat(c.place().confidence()).isEqualTo(0.95);
    }

    @Test
    @DisplayName("entities: IMO numbers resolve through the entity index; actors come from the lexicon")
    void entitiesAndActors() {
        EntityIndex entities = new EntityIndex() {
            @Override
            public List<Hit> find(String folded) {
                return List.of();
            }

            @Override
            public List<String> byKey(String keyType, String value) {
                return "imo".equals(keyType) && "9876543".equals(value) ? List.of("entity:V1") : List.of();
            }
        };
        ClaimExtractor.Extraction e = ClaimExtractor.extract("Houthis claim drone strike on tanker IMO 9876543",
                AT, ClaimLexicon.bundled(), PLACES, entities);
        assertThat(e.entityRefs()).containsExactly("entity:V1");
        assertThat(only(e, "drone").actor()).isEqualTo("Houthis");
        assertThat(only(e, "drone").entityRefs()).containsExactly("entity:V1");
    }

    @Test
    @DisplayName("time expressions resolve relative to createdAt")
    void times() {
        assertThat(TimeExpressions.resolve("explosion 2 hours ago", AT).from())
                .isEqualTo(Instant.parse("2026-10-17T12:00:00Z"));
        assertThat(TimeExpressions.resolve("explosion 10 minutes ago", AT).from())
                .isEqualTo(Instant.parse("2026-10-17T14:20:00Z"));
        TimeExpressions.EventTime yesterday = TimeExpressions.resolve("fire yesterday", AT);
        assertThat(yesterday.from()).isEqualTo(Instant.parse("2026-10-16T00:00:00Z"));
        assertThat(yesterday.precision()).isEqualTo("day");
        TimeExpressions.EventTime night = TimeExpressions.resolve("loud blasts last night", AT);
        assertThat(night.from()).isEqualTo(Instant.parse("2026-10-16T18:00:00Z"));
        assertThat(night.to()).isEqualTo(Instant.parse("2026-10-17T06:00:00Z"));
        assertThat(TimeExpressions.resolve("انفجار أمس", AT).precision()).isEqualTo("day");
        assertThat(TimeExpressions.resolve("взрыв вчера", AT).from()).isEqualTo(Instant.parse("2026-10-16T00:00:00Z"));
        assertThat(TimeExpressions.resolve("פיצוץ היום", AT).from()).isEqualTo(Instant.parse("2026-10-17T00:00:00Z"));
        assertThat(TimeExpressions.resolve("no time stated", AT)).isNull();
    }

    @Test
    @DisplayName("one claim per type per post; claim keys group by type, polarity and place")
    void keys() {
        ClaimExtractor.Extraction e = extract("Explosion, another explosion, explosions in Aden", AT, PLACES);
        assertThat(e.claims()).hasSize(1);
        assertThat(e.claims().get(0).key()).isEqualTo("explosion|asserted|aden");
        assertThat(extract(null, AT, PLACES).claims()).isEmpty();
    }
}
