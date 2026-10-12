package ai.foresignal.trigger;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.AlertTemplates;
import ai.foresignal.reference.RegionNear;
import ai.foresignal.reference.RegionTags;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The region placeholders of prompt 040 in {@link MessageTemplate} and {@link AlertTemplates}: every
 * placeholder, imperial units (the channel setting and the {@code |imperial} filter) and missing values.
 */
class RegionTemplateTest {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");

    /** 12.2 km at bearing 20° from Austin, inside Travis County, Texas. */
    static final RegionTags AUSTIN = new RegionTags("US", "US-TX", "48453", null,
            new RegionNear(4671654, "Austin", "US", "US-TX", 12.2, 20, 931830L, "TX"), "United States of America",
            "Texas", "Travis");

    static MessageTemplate.Context context(RegionTags region) {
        return new MessageTemplate.Context("Quake", "usgs", "us1", "earthquake", "M4", "warning", T, null, null,
                Map.of("mag", 4.2), region);
    }

    @Test
    @DisplayName("every region placeholder renders")
    void everyPlaceholder() {
        String template = "{{near.name}}|{{near.km}}|{{near.mi}}|{{near.bearing}}|{{near.text}}|{{country}}|"
                + "{{country.name}}|{{admin1}}|{{admin1.name}}|{{county}}|{{sea}}";
        assertThat(MessageTemplate.render(template, context(AUSTIN)))
                .isEqualTo("Austin|12|8|NNE|12 km NNE of Austin, TX|US|United States of America|US-TX|Texas|Travis|");
    }

    @Test
    @DisplayName("imperial: from the extras (a channel's units) or the |imperial filter")
    void imperial() {
        MessageTemplate.Extras imperial = new MessageTemplate.Extras(ZoneOffset.UTC, T, Map.of(), null, true);
        assertThat(MessageTemplate.render("{{near.text}}", context(AUSTIN), imperial))
                .isEqualTo("8 mi NNE of Austin, TX");
        assertThat(MessageTemplate.render("{{near.text|imperial}}", context(AUSTIN)))
                .isEqualTo("8 mi NNE of Austin, TX");
        assertThat(MessageTemplate.render("{{near.text}}", context(AUSTIN))).isEqualTo("12 km NNE of Austin, TX");
    }

    @Test
    @DisplayName("missing values render empty: no tags, no near, at sea")
    void missing() {
        String template = "[{{near.text}}][{{near.km}}][{{country}}][{{country.name}}][{{admin1.name}}][{{county}}]"
                + "[{{sea}}]";
        assertThat(MessageTemplate.render(template, context(null))).isEqualTo("[][][][][][][]");
        RegionTags atSea = new RegionTags(null, null, null, "Gulf of Mexico", null, null, null, null);
        assertThat(MessageTemplate.render(template, context(atSea))).isEqualTo("[][][][][][][Gulf of Mexico]");
        assertThat(MessageTemplate.render("{{near.name}}", new MessageTemplate.Context("n", "s", "e", "k", null,
                "info", T, null, null, null))).as("the pre-040 context").isEmpty();
    }

    @Test
    @DisplayName("a county without a name falls back to its FIPS code; within 1 km the text says in")
    void fallbacks() {
        RegionTags unnamed = new RegionTags("US", "US-TX", "48453", null,
                new RegionNear(1, "Austin", "US", "US-TX", 0.4, 90, null, "TX"), null, null, null);
        assertThat(MessageTemplate.render("{{county}} {{near.text}}", context(unnamed))).isEqualTo("48453 in Austin, TX");
    }

    @Test
    @DisplayName("notification templates read the alert's region snapshot and the channel's units")
    void alertTemplates() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("ruleName", "Quake");
        data.put("sourceId", "usgs");
        data.put("externalId", "us1");
        data.put("severity", "warning");
        data.put("observedAt", T.toString());
        data.put("region", AUSTIN.toMap());
        AlertTemplates.Text metric = AlertTemplates.render("{{near.text}}", "{{country.name}}", data, null,
                ZoneOffset.UTC, T);
        assertThat(metric.title()).isEqualTo("12 km NNE of Austin, TX");
        assertThat(metric.body()).isEqualTo("United States of America");
        AlertTemplates.Text imperial = AlertTemplates.render("{{near.text}}", null, data, null, ZoneOffset.UTC, T,
                true);
        assertThat(imperial.title()).isEqualTo("8 mi NNE of Austin, TX");
    }

    @Test
    @DisplayName("the 16-point compass and the tags' JSON round trip")
    void compassAndRoundTrip() {
        assertThat(RegionNear.compass16(0)).isEqualTo("N");
        assertThat(RegionNear.compass16(11.24)).isEqualTo("N");
        assertThat(RegionNear.compass16(11.26)).isEqualTo("NNE");
        assertThat(RegionNear.compass16(200)).isEqualTo("SSW");
        assertThat(RegionNear.compass16(-10)).isEqualTo("N");
        assertThat(RegionNear.compass16(349)).isEqualTo("N");
        RegionTags back = RegionTags.fromMap(AUSTIN.toMap());
        assertThat(back.nearText()).isEqualTo("12 km NNE of Austin, TX");
        assertThat(back.near().text(true)).isEqualTo("8 mi NNE of Austin, TX");
        assertThat(back.iso2()).isEqualTo("US");
        assertThat(AUSTIN.toMap()).containsEntry("attribution",
                ai.foresignal.reference.RegionAttribution.GEONAMES);
        assertThat(RegionTags.fromJson("not json")).isNull();
    }
}
