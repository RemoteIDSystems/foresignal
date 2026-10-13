package ai.foresignal.mapping.format;

import ai.foresignal.derive.DerivedRecords;
import ai.foresignal.inbound.format.IngestFormat;
import ai.foresignal.mapping.MappingCache;
import ai.foresignal.mapping.MappingEngine;
import ai.foresignal.mapping.preset.MappingPreset;
import ai.foresignal.mapping.preset.PresetCatalog;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

/**
 * Registers one {@link DeviceMappingFormat} per device preset (prompt 059). The {@code alertmanager} preset uses
 * the built-in {@code alertmanager} format and gets none.
 */
@Configuration(proxyBeanMethods = false)
public class DeviceFormatsConfiguration {

    /** The {@code ecowitt} format. */
    @Bean
    IngestFormat ecowittFormat(MappingCache m, MappingEngine e, JsonMapper j, DerivedRecords d) {
        return format("ecowitt", m, e, j, d);
    }

    /** The {@code wu-pws} format. */
    @Bean
    IngestFormat wuPwsFormat(MappingCache m, MappingEngine e, JsonMapper j, DerivedRecords d) {
        return format("wu-pws", m, e, j, d);
    }

    /** The {@code aiscatcher} format. */
    @Bean
    IngestFormat aiscatcherFormat(MappingCache m, MappingEngine e, JsonMapper j, DerivedRecords d) {
        return format("aiscatcher", m, e, j, d);
    }

    /** The {@code chirpstack-v4} format. */
    @Bean
    IngestFormat chirpstackFormat(MappingCache m, MappingEngine e, JsonMapper j, DerivedRecords d) {
        return format("chirpstack-v4", m, e, j, d);
    }

    /** The {@code tts-v3} format. */
    @Bean
    IngestFormat ttsFormat(MappingCache m, MappingEngine e, JsonMapper j, DerivedRecords d) {
        return format("tts-v3", m, e, j, d);
    }

    /** The {@code airrohr} format. */
    @Bean
    IngestFormat airrohrFormat(MappingCache m, MappingEngine e, JsonMapper j, DerivedRecords d) {
        return format("airrohr", m, e, j, d);
    }

    /** The {@code gmc} format. */
    @Bean
    IngestFormat gmcFormat(MappingCache m, MappingEngine e, JsonMapper j, DerivedRecords d) {
        return format("gmc", m, e, j, d);
    }

    private static DeviceMappingFormat format(String presetId, MappingCache m, MappingEngine e, JsonMapper j,
            DerivedRecords d) {
        MappingPreset preset = PresetCatalog.find(presetId)
                .orElseThrow(() -> new IllegalStateException("Mapping preset " + presetId + " is missing"));
        return new DeviceMappingFormat(preset, m, e, j, d);
    }
}
