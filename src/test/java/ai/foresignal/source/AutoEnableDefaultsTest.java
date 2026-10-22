package ai.foresignal.source;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.adapters.usgs.UsgsEarthquakesAdapter;
import ai.foresignal.source.adapters.wikistream.WikiStreamAdapter;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.SourceDescriptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Which built-in descriptors hold themselves off by default (prompt 146): {@code wiki-edit-surge} needs no credential
 * and no parameter yet is not auto-enabled, for its bandwidth; a plain free source such as {@code usgs-earthquakes}
 * is. The seeding and {@code enable-free} behavior built on the flag is covered by {@code SourceServiceIT}.
 */
class AutoEnableDefaultsTest {

    @Test
    @DisplayName("wiki-edit-surge is free yet held off by its descriptor")
    void wikiEditSurgeIsHeldOff() {
        SourceDescriptor wiki = new WikiStreamAdapter("").descriptor();

        assertThat(wiki.id()).isEqualTo(WikiStreamAdapter.ID);
        assertThat(wiki.requiresCredentials()).isFalse();
        assertThat(wiki.params()).noneMatch(ParamSpec::required);
        assertThat(wiki.autoEnable()).isFalse();
    }

    @Test
    @DisplayName("usgs-earthquakes is free and may start enabled")
    void usgsEarthquakesMayStartEnabled() {
        SourceDescriptor usgs = new UsgsEarthquakesAdapter("").descriptor();

        assertThat(usgs.requiresCredentials()).isFalse();
        assertThat(usgs.params()).noneMatch(ParamSpec::required);
        assertThat(usgs.autoEnable()).isTrue();
    }
}
