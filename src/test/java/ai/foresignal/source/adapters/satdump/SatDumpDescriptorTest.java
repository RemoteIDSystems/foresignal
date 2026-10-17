package ai.foresignal.source.adapters.satdump;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The {@code satdump-{station}} descriptor, parameters and family (prompt 049, Weather-Imagery-Design
 * §4.1 and §2.5).
 */
class SatDumpDescriptorTest {

    @Test
    @DisplayName("STREAMING, three kinds with 30/365/7 d retention, STANDARD, ALL, UPSTREAM, no TTL, LAN budget")
    void descriptor() {
        SourceDescriptor d = SatDumpDescriptor.build("home");
        assertThat(d.id()).isEqualTo("satdump-home");
        assertThat(d.mode()).isEqualTo(Mode.STREAMING);
        // prompt 107 adds the DCP and EMWIN kinds
        assertThat(d.kinds()).containsExactlyInAnyOrder("ground-station", "satellite-pass", "raster-frame",
                "dcp-observation", "emwin-product", "emwin-warning");
        assertThat(d.kindRetention()).containsEntry("ground-station", Duration.ofDays(30))
                .containsEntry("satellite-pass", Duration.ofDays(365)).containsEntry("raster-frame", Duration.ofDays(7));
        assertThat(d.historyTier()).isEqualTo(HistoryTier.STANDARD);
        assertThat(d.defaultIngestPolicy().mode()).isEqualTo(IngestPolicy.PolicyMode.ALL);
        assertThat(d.observedAtMode()).isEqualTo(ObservedAtMode.UPSTREAM);
        assertThat(d.recordTtl()).isNull();
        assertThat(d.licence()).isEqualTo("operator-owned");
        assertThat(d.defaultBudget().perMinute()).isEqualTo(60);
        assertThat(d.defaultBudget().perDay()).isEqualTo(50_000);
        assertThat(d.titleField()).isEqualTo("title");
        assertThat(d.zeroResultsNormal()).isTrue();
        assertThat(d.volatileFields()).contains("expiresAt");
        assertThat(d.param("apiUrl").type()).isEqualTo(ParamType.LAN_URL);
        assertThat(d.param("inboxPath").type()).isEqualTo(ParamType.INBOX_PATH);
        assertThat(d.param("location").required()).isTrue();
        assertThat(d.param("apiInterval").defaultValue()).isEqualTo("5s");
        assertThat(d.param("scanInterval").defaultValue()).isEqualTo("30s");
        assertThat(DescriptorValidator.problems(d, new SatDumpAdapter("home", null, null))).isEmpty();
        assertThat(DescriptorValidator.warnings(d)).isEmpty();
    }

    @Test
    @DisplayName("054: polarGlob (default **/*.tif, checked), the optional ingestKey credential, acceptsRemoteMedia")
    void polarAndRemote() {
        SourceDescriptor d = SatDumpDescriptor.build("home");
        assertThat(d.param("polarGlob").type()).isEqualTo(ParamType.STRING);
        assertThat(d.param("polarGlob").defaultValue()).isEqualTo("**/*.tif");
        assertThat(d.credentialFields()).singleElement().satisfies(f -> {
            assertThat(f.name()).isEqualTo("ingestKey");
            assertThat(f.required()).isFalse();
        });
        assertThat(d.acceptsRemoteMedia()).isTrue();
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("location", "29.76,-95.37");
        values.put("inboxPath", Path.of("/data/inbox/satdump/home"));
        values.put("polarGlob", "../outside/*.tif");
        assertThat(SatDumpDescriptor.check(new ValidatedParams(values, Map.of()))).containsKey("polarGlob");
        values.put("polarGlob", "MSU-MR/*_projected.tif");
        assertThat(SatDumpDescriptor.check(new ValidatedParams(values, Map.of()))).doesNotContainKey("polarGlob");
    }

    @Test
    @DisplayName("at least one of apiUrl and inboxPath; intervals in range; location and satNames parse")
    void checks() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("location", "29.76,-95.37,15");
        assertThat(SatDumpDescriptor.check(ValidatedParams.of(values))).containsOnlyKeys("apiUrl");

        values.put("apiUrl", URI.create("http://192.168.1.60:8081"));
        assertThat(SatDumpDescriptor.check(ValidatedParams.of(values))).isEmpty();
        values.remove("apiUrl");
        values.put("inboxPath", Path.of("/data/inbox/satdump/home"));
        assertThat(SatDumpDescriptor.check(ValidatedParams.of(values))).isEmpty();

        values.put("apiInterval", Duration.ofSeconds(1));
        values.put("scanInterval", Duration.ofHours(1));
        values.put("satNames", "[\"x\"]");
        values.put("location", "95,0");
        assertThat(SatDumpDescriptor.check(ValidatedParams.of(values)))
                .containsOnlyKeys("apiInterval", "scanInterval", "satNames", "location");
        values.put("apiInterval", Duration.ofSeconds(30));
        values.put("scanInterval", Duration.ofSeconds(5));
        values.put("satNames", "{\"METEOR-M2 3\":\"meteor-m2-3\"}");
        values.put("location", "home");
        assertThat(SatDumpDescriptor.check(ValidatedParams.of(values))).isEmpty();
    }

    @Test
    @DisplayName("location: coordinates or a place id")
    void location() {
        StationLocation coordinates = StationLocation.parse(" 29.76, -95.37 ");
        assertThat(coordinates.point(null)).containsExactly(-95.37, 29.76);
        StationLocation place = StationLocation.parse("home");
        assertThat(place.point(null)).isNull();
        assertThat(place.point(id -> java.util.Optional.of(new double[] {-97.7, 30.3}))).containsExactly(-97.7, 30.3);
        assertThatThrownBy(() -> StationLocation.parse("1,2,3,4")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StationLocation.parse("0,200")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StationLocation.parse("Home Base")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StationLocation.parse("a,b")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("the family: one source per kebab-case name, repeats ignored, an invalid name fails startup")
    void family() {
        assertThat(SatDumpFamily.names(" home, pi ,home,,")).containsExactly("home", "pi");
        assertThat(SatDumpFamily.names("")).isEmpty();
        assertThat(SatDumpFamily.names(null)).isEmpty();
        assertThatThrownBy(() -> SatDumpFamily.names("Home")).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> SatDumpFamily.names("a".repeat(57))).isInstanceOf(IllegalStateException.class);
        SatDumpFamily family = new SatDumpFamily("home,pi", null, null, null);
        assertThat(family.adapters()).extracting(a -> a.id()).containsExactly("satdump-home", "satdump-pi");
        assertThat(new SatDumpFamily("", null, null, null).adapters()).isEmpty();
    }
}
