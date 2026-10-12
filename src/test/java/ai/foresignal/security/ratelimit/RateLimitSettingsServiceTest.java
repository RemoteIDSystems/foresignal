package ai.foresignal.security.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** {@link RateLimitSettingsService#parse} and the {@link RateLimitSettings} defaults (prompt 035). */
class RateLimitSettingsServiceTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    static RateLimitSettings parse(String json) {
        return RateLimitSettingsService.parse(JSON.readTree(json.replace('\'', '"')));
    }

    static ValidationException invalid(String json) {
        return catchThrowableOfType(ValidationException.class, () -> parse(json));
    }

    @Test
    @DisplayName("defaults: read 600, tiles 1200, stream 30 + 8, expensive 20, write 120, anonymous 120, no metrics limit")
    void defaults() {
        RateLimitSettings d = RateLimitSettings.defaults();

        assertThat(d.read()).isEqualTo(600);
        assertThat(d.tiles()).isEqualTo(1_200);
        assertThat(d.stream().opensPerMinute()).isEqualTo(30);
        assertThat(d.stream().concurrent()).isEqualTo(8);
        assertThat(d.expensive()).isEqualTo(20);
        assertThat(d.write()).isEqualTo(120);
        assertThat(d.anonymous()).isEqualTo(120);
        assertThat(d.metricsPerMinute()).isNull();
        assertThat(parse("{}")).isEqualTo(d);
    }

    @Test
    @DisplayName("a full body round-trips; perMinute maps each class")
    void full() {
        RateLimitSettings s = parse("{'read':1,'tiles':2,'stream':{'opensPerMinute':3,'concurrent':4},"
                + "'expensive':5,'write':6,'anonymous':7,'metricsPerMinute':8}");

        assertThat(s.perMinute(RateClass.READ)).isEqualTo(1);
        assertThat(s.perMinute(RateClass.TILES)).isEqualTo(2);
        assertThat(s.perMinute(RateClass.STREAM)).isEqualTo(3);
        assertThat(s.stream().concurrent()).isEqualTo(4);
        assertThat(s.perMinute(RateClass.EXPENSIVE)).isEqualTo(5);
        assertThat(s.perMinute(RateClass.WRITE)).isEqualTo(6);
        assertThat(s.perMinute(RateClass.ANONYMOUS)).isEqualTo(7);
        assertThat(s.perMinute(RateClass.METRICS)).isEqualTo(8);
    }

    @Test
    @DisplayName("members left out take their defaults; a partial stream object keeps the other default")
    void partial() {
        RateLimitSettings s = parse("{'read':50,'stream':{'concurrent':2},'metricsPerMinute':null}");

        assertThat(s.read()).isEqualTo(50);
        assertThat(s.tiles()).isEqualTo(1_200);
        assertThat(s.stream().opensPerMinute()).isEqualTo(30);
        assertThat(s.stream().concurrent()).isEqualTo(2);
        assertThat(s.metricsPerMinute()).isNull();
        assertThat(s.perMinute(RateClass.METRICS)).isZero();
    }

    @Test
    @DisplayName("zero, negative, fractional, textual and oversized values are field errors")
    void badNumbers() {
        ValidationException e = invalid("{'read':0,'tiles':-1,'expensive':1.5,'write':'10','anonymous':9999999999,"
                + "'metricsPerMinute':0}");

        assertThat(e.getCode()).isEqualTo(ErrorCodes.VALIDATION_FAILED);
        assertThat(e.getErrors()).extracting(FieldError::field)
                .containsExactlyInAnyOrder("read", "tiles", "expensive", "write", "anonymous", "metricsPerMinute");
    }

    @Test
    @DisplayName("unknown members, a non-object stream and bad stream members are field errors")
    void badShape() {
        assertThat(invalid("{'reads':5}").getErrors()).extracting(FieldError::field).containsExactly("reads");
        assertThat(invalid("{'stream':5}").getErrors()).extracting(FieldError::field).containsExactly("stream");
        assertThat(invalid("{'stream':{'opens':5,'concurrent':0}}").getErrors()).extracting(FieldError::field)
                .containsExactlyInAnyOrder("stream.opens", "stream.concurrent");
    }

    @Test
    @DisplayName("a body that is not an object is refused")
    void notAnObject() {
        assertThatThrownBy(() -> parse("[]")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> RateLimitSettingsService.parse(null)).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("stored values that are missing or non-positive read back as defaults")
    void storedFormIsLenient() {
        RateLimitSettings s = JSON.readValue("{\"read\":0,\"metricsPerMinute\":-3}", RateLimitSettings.class);

        assertThat(s).isEqualTo(RateLimitSettings.defaults());
        assertThat(JSON.readValue(JSON.writeValueAsString(s), RateLimitSettings.class)).isEqualTo(s);
    }
}
