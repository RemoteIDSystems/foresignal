package ai.foresignal.weather;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.place.WeatherAoiProperties;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link WeatherSettingsService} (prompt 056): defaults from the configuration until a value is stored, the
 * validation of every member in one 400, the stored value, and the change event that recomputes the places AOI.
 */
class WeatherSettingsServiceTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    @Test
    @DisplayName("defaults come from the properties until a value is stored")
    void defaults() {
        AppSettingService settings = mock(AppSettingService.class);
        when(settings.get(SettingKeys.WEATHER, WeatherSettings.class)).thenReturn(Optional.empty());
        WeatherSettingsService service = new WeatherSettingsService(settings,
                new WeatherAoiProperties(80, "-100,30,-99,31", new ArrayList<>()), mock(ApplicationEventPublisher.class));

        WeatherSettings value = service.get();

        assertThat(value.alertContext()).isEqualTo("auto");
        assertThat(value.defaultAoi()).isEqualTo("-100,30,-99,31");
        assertThat(value.aoiBufferKm()).isEqualTo(80.0);
        assertThat(value.alertContextOn()).isTrue();
        assertThat(value.defaultBox()).containsExactly(-100, 30, -99, 31);
    }

    @Test
    @DisplayName("a valid body parses; missing members take the shipped defaults")
    void parse() {
        WeatherSettings value = WeatherSettingsService.parse(JSON.readTree("{\"alertContext\":\"off\",\"aoiBufferKm\":0}"));
        assertThat(value.alertContext()).isEqualTo("off");
        assertThat(value.alertContextOn()).isFalse();
        assertThat(value.aoiBufferKm()).isZero();
        assertThat(value.defaultAoi()).isEqualTo(WeatherSettings.DEFAULT_AOI);
        assertThat(WeatherSettingsService.parse(JSON.readTree("{}"))).isEqualTo(WeatherSettings.defaults());
    }

    @Test
    @DisplayName("every problem is reported in one 400")
    void validation() {
        assertThatThrownBy(() -> WeatherSettingsService.parse(JSON.readTree(
                "{\"alertContext\":\"sometimes\",\"defaultAoi\":\"1,2,3\",\"aoiBufferKm\":2000,\"extra\":1}")))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getErrors()).extracting(f -> f.field())
                        .containsExactlyInAnyOrder("alertContext", "defaultAoi", "aoiBufferKm", "extra"));
        assertThat(WeatherSettingsService.boxProblem("10,10,5,20")).contains("minLon");
        assertThat(WeatherSettingsService.boxProblem("-200,10,5,20")).contains("within");
        assertThat(WeatherSettingsService.boxProblem("a,b,c,d")).contains("four numbers");
        assertThat(WeatherSettingsService.boxProblem("-106.65,25.84,-93.51,36.50")).isNull();
        assertThatThrownBy(() -> WeatherSettingsService.parse(JSON.readTree("[]")))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("put stores the value and publishes the change; aoiChanged tells AOI changes apart")
    void put() {
        AppSettingService settings = mock(AppSettingService.class);
        when(settings.get(SettingKeys.WEATHER, WeatherSettings.class)).thenReturn(Optional.empty());
        ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
        WeatherSettingsService service = new WeatherSettingsService(settings, WeatherAoiProperties.defaults(), events);
        WeatherSettings value = new WeatherSettings("off", null, null);

        assertThat(service.put(value)).isEqualTo(value);

        verify(settings).put(eq(SettingKeys.WEATHER), eq(value));
        ArgumentCaptor<Object> event = ArgumentCaptor.forClass(Object.class);
        verify(events).publishEvent(event.capture());
        WeatherSettingsChangedEvent changed = (WeatherSettingsChangedEvent) event.getValue();
        assertThat(changed.aoiChanged()).isFalse();
        assertThat(new WeatherSettingsChangedEvent(value, new WeatherSettings("off", "-100,30,-99,31", null)).aoiChanged())
                .isTrue();
        assertThat(new WeatherSettingsChangedEvent(value, new WeatherSettings("off", null, 10.0)).aoiChanged()).isTrue();
        assertThat(new WeatherSettingsChangedEvent(null, value).aoiChanged()).isTrue();
    }
}
