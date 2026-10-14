package ai.foresignal.mapping.preset;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.inbound.format.MappingFormat;
import ai.foresignal.source.spi.NormalizedRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The {@code rsudp-alarm} preset (prompt 086): a Raspberry Shake's rsudp ALARM posted by {@code contrib/rsudp} to the
 * generic {@code mapping} format becomes a {@code seismic-trigger} record at the source's station point, with
 * {@code eventTime = time}. The fixture under {@code fixtures/mapping/rsudp-alarm/} gives the golden record.
 */
class RsudpAlarmPresetContractTest {

    @Test
    @DisplayName("the fixture maps to the golden record: station point geometry, eventTime and observedAt = time")
    void golden() {
        PresetContract.Run run = PresetContract.run("rsudp-alarm");
        PresetContract.assertGolden(run);
        NormalizedRecord r = run.parsed().records().getFirst();
        assertThat(r.eventTime()).isEqualTo(r.observedAt());
    }

    @Test
    @DisplayName("the preset posts to the mapping format with the key in the path; seismic-trigger is its only kind")
    void preset() {
        MappingPreset preset = PresetCatalog.find("rsudp-alarm").orElseThrow();
        assertThat(preset.format()).isEqualTo(MappingFormat.ID);
        assertThat(preset.locationMode()).isEqualTo("const");
        assertThat(preset.urlTemplate()).endsWith("/v1/ingest/{sourceId}/k/{key}/mapping");
        assertThat(preset.document().path("kinds").get(0).asString()).isEqualTo("seismic-trigger");
        assertThat(preset.document().path("allowPathKey").asBoolean()).isTrue();
        assertThat(preset.paramNames()).contains("lon", "lat", "exactLocation");
    }
}
