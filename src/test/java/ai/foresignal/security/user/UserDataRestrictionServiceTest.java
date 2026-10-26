package ai.foresignal.security.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.RestrictionValidator;
import ai.foresignal.security.TokenRestrictions;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@link UserDataRestrictionService#parse} and its view (prompt R2-013). */
class UserDataRestrictionServiceTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private final UserDataRestrictionService service =
            new UserDataRestrictionService(null, RestrictionValidator.structural(), null);

    private static JsonNode node(String text) {
        return JSON.readTree(text.replace('\'', '"'));
    }

    @Test
    @DisplayName("data members parse; null and an empty object clear the restriction")
    void parses() {
        TokenRestrictions r = service.parse(node("{'sources':['a'],'kinds':['k'],'bbox':[0,0,1,1]}"));
        assertThat(r.sources()).containsExactly("a");
        assertThat(r.kinds()).containsExactly("k");
        assertThat(r.bbox()).containsExactly(0.0, 0.0, 1.0, 1.0);
        assertThat(service.parse(null)).isNull();
        assertThat(service.parse(node("null"))).isNull();
        assertThat(service.parse(node("{}"))).isNull();
    }

    @Test
    @DisplayName("endpoint, rate and unknown members are refused, naming the field")
    void refusesNonDataMembers() {
        for (String bad : List.of("{'endpoints':['GET /v1/records']}", "{'maxRequestsPerMinute':5}",
                "{'excludePrivate':true}", "{'shade':'red'}")) {
            assertThatThrownBy(() -> service.parse(node(bad))).as(bad).isInstanceOf(ValidationException.class);
        }
        assertThatThrownBy(() -> service.parse(node("{'bbox':[1,2,3]}"))).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("the view carries exactly the five data members")
    void view() {
        Map<String, Object> v = UserDataRestrictionService.view(
                new TokenRestrictions(List.of("a"), null, null, null));
        assertThat(v).containsOnlyKeys("sources", "kinds", "bbox", "area", "placeIds");
        assertThat(v.get("sources")).isEqualTo(List.of("a"));
    }
}
