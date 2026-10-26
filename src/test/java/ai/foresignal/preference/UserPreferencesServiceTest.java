package ai.foresignal.preference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** Parsing and the quiet-hours conversions of {@link UserPreferencesService} (prompt R2-012). */
class UserPreferencesServiceTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final UserPreferencesService service = new UserPreferencesService(null, null, null, null, null, null, null,
            JSON, Clock.systemUTC());

    private UserPreferencesService.Input parse(String body) {
        return service.parse(JSON.readTree(body.replace('\'', '"')));
    }

    @Test
    @DisplayName("a full body parses; the locale is stored in canonical form")
    void parsesFullBody() {
        UserPreferencesService.Input in = parse("{'timezone':'Asia/Tokyo','locale':'pt-br','theme':'light',"
                + "'notifications':{'emailEnabled':false,'pushEnabled':true,'quietHours':'23:15-06:45'},"
                + "'ui':{'density':'compact'}}");
        assertThat(in.timezone()).isEqualTo("Asia/Tokyo");
        assertThat(in.locale()).isEqualTo("pt-BR");
        assertThat(in.theme()).isEqualTo("light");
        assertThat(in.emailEnabled()).isFalse();
        assertThat(in.pushEnabled()).isTrue();
        assertThat(in.setQuietHours()).isTrue();
        assertThat(in.quietHours()).isEqualTo("23:15-06:45");
        assertThat(in.ui()).containsEntry("density", "compact");
    }

    @Test
    @DisplayName("an empty body resets everything and switches nothing")
    void emptyBody() {
        UserPreferencesService.Input in = parse("{}");
        assertThat(in.timezone()).isNull();
        assertThat(in.locale()).isNull();
        assertThat(in.theme()).isNull();
        assertThat(in.ui()).isEmpty();
        assertThat(in.emailEnabled()).isNull();
        assertThat(in.pushEnabled()).isNull();
        assertThat(in.setQuietHours()).isFalse();
    }

    @Test
    @DisplayName("an explicit null quiet hours clears; an absent one leaves")
    void quietHoursTriState() {
        assertThat(parse("{'notifications':{'quietHours':null}}").setQuietHours()).isTrue();
        assertThat(parse("{'notifications':{'quietHours':null}}").quietHours()).isNull();
        assertThat(parse("{'notifications':{}}").setQuietHours()).isFalse();
        assertThat(parse("{'notifications':null}").setQuietHours()).isFalse();
    }

    @Test
    @DisplayName("every invalid member is refused")
    void refusals() {
        for (String body : new String[] {"[]", "{'timezone':'Nowhere/Town'}", "{'timezone':'+02:00'}",
                "{'locale':'en_US'}", "{'locale':'zz'}", "{'locale':''}", "{'theme':'Dark'}", "{'theme':1}",
                "{'notifications':[]}", "{'notifications':{'pushEnabled':1}}",
                "{'notifications':{'quietHours':'7-9'}}", "{'notifications':{'quietHours':5}}",
                "{'notifications':{'digest':true}}", "{'ui':'x'}", "{'extra':1}"}) {
            assertThatThrownBy(() -> parse(body)).as(body).isInstanceOf(ValidationException.class);
        }
        assertThatThrownBy(() -> service.parse(null)).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("quiet hours convert between HH:MM-HH:MM and the QuietHours object")
    void quietHoursConversion() {
        Map<String, Object> q = UserPreferencesService.quietHours("22:00-07:00");
        assertThat(q).containsExactly(Map.entry("start", "22:00"), Map.entry("end", "07:00"));
        assertThat(UserPreferencesService.window(q)).isEqualTo("22:00-07:00");
        assertThat(UserPreferencesService.window(null)).isNull();
        Map<String, Object> partial = new LinkedHashMap<>();
        partial.put("start", "22:00");
        assertThat(UserPreferencesService.window(partial)).isNull();
        assertThatThrownBy(() -> UserPreferencesService.quietHours("24:00-07:00"))
                .isInstanceOf(ValidationException.class);
    }
}
