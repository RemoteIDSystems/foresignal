package ai.foresignal.doctor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link StructureDiff} (prompt 140, task 2): added, removed and retyped paths, presence drops, rename candidates by
 * snake↔camel equality, Levenshtein similarity and value distribution, and no rename across parents.
 */
class StructureDiffTest {

    final StructureExtractor extractor = new StructureExtractor();

    Structure json(String body) {
        return extractor.extract(body, "application/json");
    }

    @Test
    @DisplayName("added, removed and type-changed paths; examples of the changed paths")
    void addedRemovedTypeChanged() {
        Structure fixture = json("{\"items\":[{\"id\":1,\"depth\":10.5,\"old\":\"x\"},{\"id\":2,\"depth\":3,\"old\":\"y\"}]}");
        Structure probe = json("{\"items\":[{\"id\":1,\"depth\":\"10.5 km\",\"fresh\":true},{\"id\":2,\"depth\":\"3 km\","
                + "\"fresh\":false}]}");

        StructureDiff diff = StructureDiff.compare(fixture, probe, "fixture.json", p -> false);

        assertThat(diff.format()).isEqualTo("json");
        assertThat(diff.fixtureFile()).isEqualTo("fixture.json");
        assertThat(diff.addedPaths()).containsExactly("items[].fresh");
        assertThat(diff.removedPaths()).containsExactly("items[].old");
        assertThat(diff.typeChanged()).containsExactly(new StructureDiff.TypeChange("items[].depth", "number",
                "string"));
        assertThat(diff.examples()).containsKeys("items[].old", "items[].fresh", "items[].depth");
        assertThat(diff.examples().get("items[].depth")).contains("10.5 km");
        assertThat(diff.isUnchanged()).isFalse();
        assertThat(diff.firstDivergentPath()).isEqualTo("items[].old");
    }

    @Test
    @DisplayName("integer and decimal numbers are one type; null-only values never count as a type change")
    void numericAndNullAreNotTypeChanges() {
        Structure fixture = json("{\"a\":[{\"v\":1,\"n\":null}]}");
        Structure probe = json("{\"a\":[{\"v\":1.5,\"n\":\"now set\"}]}");

        assertThat(StructureDiff.compare(fixture, probe, null, null).typeChanged()).isEmpty();
    }

    @Test
    @DisplayName("a presence ratio falling by 50 points or more is reported")
    void presenceDropped() {
        Structure fixture = json("{\"a\":[{\"x\":1,\"y\":1},{\"x\":2,\"y\":2},{\"x\":3,\"y\":3},{\"x\":4,\"y\":4}]}");
        Structure probe = json("{\"a\":[{\"x\":1,\"y\":1},{\"x\":2},{\"x\":3},{\"x\":4}]}");

        StructureDiff diff = StructureDiff.compare(fixture, probe, null, null);

        assertThat(diff.presenceDropped()).extracting(StructureDiff.PresenceDrop::path).containsExactly("a[].y");
        assertThat(diff.presenceDropped().get(0).from()).isEqualTo(1.0);
        assertThat(diff.presenceDropped().get(0).to()).isEqualTo(0.25);
    }

    @Test
    @DisplayName("an array that held items and is now empty is listed; identical structures are unchanged")
    void arrayEmptyAndUnchanged() {
        Structure fixture = json("{\"features\":[{\"id\":1}],\"count\":1}");
        Structure empty = json("{\"features\":[],\"count\":0}");

        StructureDiff diff = StructureDiff.compare(fixture, empty, null, null);
        assertThat(diff.arrayEmpty()).containsExactly("features");
        assertThat(diff.removedPaths()).as("the elements of an emptied array are not removed fields").isEmpty();

        StructureDiff same = StructureDiff.compare(fixture, json("{\"features\":[{\"id\":7}],\"count\":9}"), null, null);
        assertThat(same.isUnchanged()).isTrue();
        assertThat(same.firstDivergentPath()).isNull();
    }

    @Test
    @DisplayName("rename candidate: snake_case ↔ camelCase equality scores 0.95")
    void snakeCamel() {
        Structure fixture = json("{\"r\":[{\"event_time\":\"2026-10-11\"}]}");
        Structure probe = json("{\"r\":[{\"eventTime\":\"2026-10-12\"}]}");

        StructureDiff diff = StructureDiff.compare(fixture, probe, null, p -> p.endsWith("event_time"));

        assertThat(diff.renameCandidates()).hasSize(1);
        StructureDiff.RenameCandidate c = diff.renameCandidates().get(0);
        assertThat(c.from()).isEqualTo("r[].event_time");
        assertThat(c.to()).isEqualTo("r[].eventTime");
        assertThat(c.reason()).isEqualTo("snake-camel");
        assertThat(c.score()).isEqualTo(0.95);
        assertThat(c.readByParser()).isTrue();
    }

    @Test
    @DisplayName("rename candidate: Levenshtein similarity ≥ 0.6 (latitude → lattitude)")
    void levenshtein() {
        Structure fixture = json("{\"r\":[{\"latitude\":10.0}]}");
        Structure probe = json("{\"r\":[{\"lattitude\":55.0}]}");

        StructureDiff.RenameCandidate c = StructureDiff.compare(fixture, probe, null, null).renameCandidates().get(0);

        assertThat(c.reason()).isEqualTo("name-similarity");
        assertThat(c.score()).isGreaterThanOrEqualTo(StructureDiff.NAME_SIMILARITY);
        assertThat(StructureDiff.levenshtein("kitten", "sitting")).isEqualTo(3);
        assertThat(StructureDiff.similarity("abc", "abc")).isEqualTo(1.0);
    }

    @Test
    @DisplayName("rename candidate: matching value distributions (mag → magnitude) score high")
    void valueDistribution() {
        Structure fixture = json("{\"features\":[{\"properties\":{\"mag\":2.5,\"place\":\"A\"}},"
                + "{\"properties\":{\"mag\":4.1,\"place\":\"B\"}}]}");
        Structure probe = json("{\"features\":[{\"properties\":{\"magnitude\":2.5,\"place\":\"A\"}},"
                + "{\"properties\":{\"magnitude\":4.1,\"place\":\"B\"}}]}");

        StructureDiff diff = StructureDiff.compare(fixture, probe, null, ParserLiterals.scan(List.of(
                "class P { double m(JsonNode p) { return p.path(\"mag\").asDouble(); } }")));

        StructureDiff.RenameCandidate c = diff.renameCandidates().get(0);
        assertThat(c.from()).isEqualTo("features[].properties.mag");
        assertThat(c.to()).isEqualTo("features[].properties.magnitude");
        assertThat(c.reason()).isEqualTo("value-distribution");
        assertThat(c.score()).isGreaterThanOrEqualTo(0.9);
        assertThat(c.readByParser()).isTrue();
    }

    @Test
    @DisplayName("no rename is proposed across parents, nor between incompatible types")
    void noFalseRenameAcrossParents() {
        Structure fixture = json("{\"a\":{\"event_time\":\"x\"},\"b\":{\"other\":\"y\"}}");
        Structure probe = json("{\"a\":{\"other\":\"y\"},\"b\":{\"eventTime\":\"x\"}}");

        assertThat(StructureDiff.compare(fixture, probe, null, null).renameCandidates()).isEmpty();

        Structure typed = json("{\"a\":{\"count\":1}}");
        Structure retyped = json("{\"a\":{\"counts\":\"one\"}}");
        assertThat(StructureDiff.compare(typed, retyped, null, null).renameCandidates()).isEmpty();
    }

    @Test
    @DisplayName("a path takes part in at most one candidate, the best")
    void bestCandidateOnly() {
        Structure fixture = json("{\"r\":[{\"event_time\":\"t\"}]}");
        Structure probe = json("{\"r\":[{\"eventTime\":\"t\",\"event_timestamp\":\"zzz\"}]}");

        List<StructureDiff.RenameCandidate> candidates = StructureDiff.compare(fixture, probe, null, null)
                .renameCandidates();

        assertThat(candidates).hasSize(1);
        assertThat(candidates.get(0).to()).isEqualTo("r[].eventTime");
    }

    @Test
    @DisplayName("CSV: reordered columns, a changed delimiter, a content-type change and the size ratio")
    void csvChanges() {
        Structure fixture = extractor.extract("a,b,c\n1,2,3\n", "text/csv");
        Structure reordered = extractor.extract("c,a,b\n3,1,2\n", "text/csv");
        Structure semicolon = extractor.extract("a;b;c\n1;2;3\n4;5;6\n", "text/plain");

        assertThat(StructureDiff.compare(fixture, reordered, null, null).columnsReordered()).isTrue();
        StructureDiff diff = StructureDiff.compare(fixture, semicolon, null, null);
        assertThat(diff.delimiterChanged()).isEqualTo(new StructureDiff.Change(",", ";"));
        assertThat(diff.contentTypeChanged()).isEqualTo(new StructureDiff.Change("text/csv", "text/plain"));
        assertThat(diff.sizeRatio()).isGreaterThan(1.0);
    }

    @Test
    @DisplayName("HTML where data was expected: no path lists, the title kept")
    void htmlInstead() {
        Structure fixture = json("{\"a\":1}");
        Structure html = extractor.extract("<html><head><title>Sign in</title></head></html>", "text/html");

        StructureDiff diff = StructureDiff.compare(fixture, html, null, null);

        assertThat(diff.format()).isEqualTo("html");
        assertThat(diff.htmlTitle()).isEqualTo("Sign in");
        assertThat(diff.removedPaths()).isEmpty();
        assertThat(diff.renameCandidates()).isEmpty();
    }

    @Test
    @DisplayName("without a fixture only the probe's format is known")
    void noFixture() {
        StructureDiff diff = StructureDiff.compare(null, json("{\"a\":1}"), null, null);

        assertThat(diff.fixtureFile()).isNull();
        assertThat(diff.addedPaths()).isEmpty();
        assertThat(diff.isUnchanged()).isTrue();
    }

    @Test
    @DisplayName("path helpers: parent, leaf name and top-level filtering")
    void pathHelpers() {
        assertThat(StructureDiff.parentOf("a.b[].c")).isEqualTo("a.b[]");
        assertThat(StructureDiff.parentOf("a.b[]")).isEqualTo("a.b");
        assertThat(StructureDiff.parentOf("feed.entry@id")).isEqualTo("feed.entry");
        assertThat(StructureDiff.parentOf("top")).isEmpty();
        assertThat(StructureDiff.leafName("features[].properties.mag")).isEqualTo("mag");
        assertThat(StructureDiff.leafName("items[][]")).isEqualTo("items");
        assertThat(StructureDiff.topLevel(List.of("a", "a.b", "a[].c", "d.e"))).containsExactly("a", "d.e");
        assertThat(StructureDiff.normalize("Event-Time_x")).isEqualTo("eventtimex");
        assertThat(Set.of(StructureDiff.leafName("x@y"))).containsExactly("y");
    }
}
