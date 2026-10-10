package ai.elaro.settings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.elaro.api.error.InvalidInputException;
import ai.elaro.store.AppSettingRepository;
import ai.elaro.test.IntegrationTest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;

@IntegrationTest
@Import(AppSettingServiceIT.EventRecorderConfig.class)
class AppSettingServiceIT {

    @Autowired
    private AppSettingService service;

    @Autowired
    private AppSettingRepository repository;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EventRecorder recorder;

    @BeforeEach
    void resetState() {
        // The table is truncated for us; the service's cache and the recorder are not.
        service.invalidateCache();
        recorder.clear();
    }

    @Test
    @DisplayName("a setting that was never written reads as empty")
    void unsetKeyIsEmpty() {
        assertThat(service.get(SettingKeys.RETENTION, RetentionSettings.class)).isEmpty();
    }

    @Test
    void putThenGetReturnsTheRecord() {
        var settings =
                new RetentionSettings(
                        Duration.ofDays(14),
                        Duration.ofDays(60),
                        Duration.ofDays(10),
                        Map.of("usgs-earthquakes", Duration.ofDays(3650)));

        service.put(SettingKeys.RETENTION, settings);

        Optional<RetentionSettings> read = service.get(SettingKeys.RETENTION, RetentionSettings.class);
        assertThat(read).isPresent();
        assertThat(read.get().defaultRetention()).isEqualTo(Duration.ofDays(14));
        assertThat(read.get().alerts()).isEqualTo(Duration.ofDays(60));
        assertThat(read.get().usage()).isEqualTo(Duration.ofDays(10));
        assertThat(read.get().forSource("usgs-earthquakes")).isEqualTo(Duration.ofDays(3650));
    }

    @Test
    @DisplayName("durations are stored as shorthand in the jsonb column")
    void storesDurationsAsShorthand() {
        service.put(SettingKeys.RETENTION, RetentionSettings.defaults());

        String json =
                jdbc.queryForObject(
                        "select value::text from app_setting where key = ?",
                        String.class,
                        SettingKeys.RETENTION);

        assertThat(json).contains("\"7d\"").contains("\"90d\"").contains("\"30d\"");
    }

    @Test
    @DisplayName("the stored value is real jsonb, not a JSON-encoded string")
    void storesRealJsonb() {
        service.put(SettingKeys.CORS, CorsSettings.defaults());

        String type =
                jdbc.queryForObject(
                        "select jsonb_typeof(value) from app_setting where key = ?",
                        String.class,
                        SettingKeys.CORS);

        assertThat(type).isEqualTo("object");
    }

    @Test
    void putOverwritesTheExistingValue() {
        service.put(SettingKeys.RETENTION, RetentionSettings.defaults());
        service.put(
                SettingKeys.RETENTION,
                new RetentionSettings(Duration.ofDays(1), null, null, Map.of()));

        assertThat(repository.count()).isEqualTo(1);
        assertThat(service.get(SettingKeys.RETENTION, RetentionSettings.class))
                .get()
                .extracting(RetentionSettings::defaultRetention)
                .isEqualTo(Duration.ofDays(1));
    }

    @Test
    @DisplayName("getOrDefault returns the default without writing it")
    void getOrDefaultDoesNotPersist() {
        RetentionSettings value =
                service.getOrDefault(
                        SettingKeys.RETENTION, RetentionSettings.class, RetentionSettings::defaults);

        assertThat(value.defaultRetention()).isEqualTo(Duration.ofDays(7));
        assertThat(repository.findById(SettingKeys.RETENTION)).isEmpty();
    }

    @Test
    void getOrDefaultPrefersTheStoredValue() {
        service.put(
                SettingKeys.CORS, new CorsSettings(List.of("https://globe.example"), List.of()));

        CorsSettings value =
                service.getOrDefault(SettingKeys.CORS, CorsSettings.class, CorsSettings::defaults);

        assertThat(value.readOrigins()).containsExactly("https://globe.example");
    }

    @Test
    @DisplayName("a second read is served from the cache, not the database")
    void cachesReads() {
        service.put(SettingKeys.CORS, CorsSettings.defaults());
        service.get(SettingKeys.CORS, CorsSettings.class);

        // Change the row behind the service's back; a cached read must not notice.
        jdbc.update(
                "update app_setting set value = ?::jsonb where key = ?",
                "{\"readOrigins\":[\"https://changed.example\"],\"writeOrigins\":[]}",
                SettingKeys.CORS);

        assertThat(service.get(SettingKeys.CORS, CorsSettings.class))
                .get()
                .extracting(CorsSettings::readOrigins)
                .isEqualTo(List.of("*"));
    }

    @Test
    @DisplayName("put invalidates the cache, so the next read sees the new value")
    void putInvalidatesCache() {
        service.put(SettingKeys.CORS, CorsSettings.defaults());
        service.get(SettingKeys.CORS, CorsSettings.class);

        service.put(SettingKeys.CORS, new CorsSettings(List.of("https://new.example"), List.of()));

        assertThat(service.get(SettingKeys.CORS, CorsSettings.class))
                .get()
                .extracting(CorsSettings::readOrigins)
                .isEqualTo(List.of("https://new.example"));
    }

    @Test
    void invalidateCacheForcesAReload() {
        service.put(SettingKeys.CORS, CorsSettings.defaults());
        service.get(SettingKeys.CORS, CorsSettings.class);
        jdbc.update(
                "update app_setting set value = ?::jsonb where key = ?",
                "{\"readOrigins\":[\"https://changed.example\"],\"writeOrigins\":[]}",
                SettingKeys.CORS);

        service.invalidateCache();

        assertThat(service.get(SettingKeys.CORS, CorsSettings.class))
                .get()
                .extracting(CorsSettings::readOrigins)
                .isEqualTo(List.of("https://changed.example"));
    }

    @Test
    @DisplayName("put publishes AppSettingChangedEvent once the write has committed")
    void publishesChangeEvent() {
        service.put(SettingKeys.RETENTION, RetentionSettings.defaults());

        assertThat(recorder.keys()).containsExactly(SettingKeys.RETENTION);
    }

    @Test
    void publishesOneEventPerPut() {
        service.put(SettingKeys.RETENTION, RetentionSettings.defaults());
        service.put(SettingKeys.CORS, CorsSettings.defaults());

        assertThat(recorder.keys()).containsExactly(SettingKeys.RETENTION, SettingKeys.CORS);
    }

    @Test
    @DisplayName("scalar settings round trip too")
    void storesScalarSettings() {
        service.put(SettingKeys.TRIGGER_CURSOR, 4321L);
        service.put(SettingKeys.INSTALL_ID, "a2f1c9");

        assertThat(service.get(SettingKeys.TRIGGER_CURSOR, Long.class)).contains(4321L);
        assertThat(service.get(SettingKeys.INSTALL_ID, String.class)).contains("a2f1c9");
    }

    @Test
    void rejectsNullValue() {
        assertThatThrownBy(() -> service.put(SettingKeys.CORS, null))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining(SettingKeys.CORS);
    }

    @Test
    @DisplayName("a value stored in the wrong shape fails with a message naming the key")
    void reportsUnreadableValue() {
        jdbc.update(
                "insert into app_setting (key, value, updated_at) values (?, ?::jsonb, now())",
                SettingKeys.RETENTION,
                "[1,2,3]");
        service.invalidateCache();

        assertThatThrownBy(() -> service.get(SettingKeys.RETENTION, RetentionSettings.class))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(SettingKeys.RETENTION);
    }

    @Test
    void updatedAtIsSetOnWrite() {
        service.put(SettingKeys.CORS, CorsSettings.defaults());

        assertThat(repository.findById(SettingKeys.CORS))
                .get()
                .extracting(e -> e.getUpdatedAt())
                .isNotNull();
    }

    /** Captures published events so the test can assert on them. */
    static class EventRecorder {

        private final List<String> keys = new ArrayList<>();

        @EventListener
        synchronized void onChange(AppSettingChangedEvent event) {
            keys.add(event.key());
        }

        synchronized List<String> keys() {
            return List.copyOf(keys);
        }

        synchronized void clear() {
            keys.clear();
        }
    }

    @TestConfiguration
    static class EventRecorderConfig {

        @Bean
        EventRecorder eventRecorder() {
            return new EventRecorder();
        }
    }
}
