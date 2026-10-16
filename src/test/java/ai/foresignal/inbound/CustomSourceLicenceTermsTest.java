package ai.foresignal.inbound;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.provenance.LicenceTerms;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Custom PUSH sources accept {@code licenceTerms} (prompt 109, task 1): a {@code LicenceTerms} object is parsed and
 * validated, null clears it, and the member is no longer refused as "arrives with prompt 109".
 */
class CustomSourceLicenceTermsTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    static JsonNode node(String json) {
        return JSON.readTree(json);
    }

    @Test
    @DisplayName("a full LicenceTerms object is read with its enums and flags")
    void parsesTerms() {
        List<FieldError> errors = new ArrayList<>();
        LicenceTerms terms = CustomSourceInputReader.licenceTerms(node("{\"id\":\"club-terms\",\"name\":\"Club\","
                + "\"commercialUse\":\"FORBIDDEN\",\"shareAlike\":true,\"attributionRequired\":true,"
                + "\"redistribution\":\"LOCAL_ONLY\",\"attributionText\":\"Our club\",\"termsUrl\":\"https://x.test\"}"),
                errors);
        assertThat(errors).isEmpty();
        assertThat(terms.id()).isEqualTo("club-terms");
        assertThat(terms.commercialUse()).isEqualTo(LicenceTerms.CommercialUse.FORBIDDEN);
        assertThat(terms.redistribution()).isEqualTo(LicenceTerms.Redistribution.LOCAL_ONLY);
        assertThat(terms.shareAlike()).isTrue();
        assertThat(terms.attributionRequired()).isTrue();
        assertThat(terms.termsUrl()).isEqualTo("https://x.test");
    }

    @Test
    @DisplayName("omitted enums read as UNKNOWN (restrictive); null clears; invalid values are field errors")
    void defaultsAndErrors() {
        List<FieldError> errors = new ArrayList<>();
        LicenceTerms minimal = CustomSourceInputReader.licenceTerms(node("{\"id\":\"mine\"}"), errors);
        assertThat(errors).isEmpty();
        assertThat(minimal.effectiveRedistribution()).isEqualTo(LicenceTerms.Redistribution.LOCAL_ONLY);
        assertThat(CustomSourceInputReader.licenceTerms(node("null"), errors)).isNull();
        assertThat(CustomSourceInputReader.licenceTerms(null, errors)).isNull();

        CustomSourceInputReader.licenceTerms(node("{\"redistribution\":\"SOMETIMES\",\"shareAlike\":\"yes\"}"), errors);
        assertThat(errors).extracting(FieldError::field).contains("licenceTerms.id", "licenceTerms.redistribution",
                "licenceTerms.shareAlike");
        errors.clear();
        CustomSourceInputReader.licenceTerms(node("\"CC-BY-4.0\""), errors);
        assertThat(errors).extracting(FieldError::field).containsExactly("licenceTerms");
    }

    @Test
    @DisplayName("no member is refused as owned by a later prompt any more")
    void notALaterMember() {
        assertThat(CustomSourceInputReader.LATER_MEMBERS).doesNotContainKey("licenceTerms");
    }
}
