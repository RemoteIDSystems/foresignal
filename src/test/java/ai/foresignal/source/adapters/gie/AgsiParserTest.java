package ai.foresignal.source.adapters.gie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.CredentialsRejectedException;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link AgsiParser} (prompt 093): the storage rows, the status letters (N skipped) and the HTTP-200 access-denied
 * body.
 */
class AgsiParserTest {

    @Test
    @DisplayName("two storage points (C final, E estimated), the N row skipped")
    void storage() {
        List<NormalizedRecord> records = AgsiParser.parse(AdapterFixtures.bytes("gie-agsi", "storage.json"));
        assertThat(records).extracting(NormalizedRecord::externalId).containsExactly("agsi:DE:2026-10-13",
                "agsi:FR:2026-10-13");
        NormalizedRecord germany = records.get(0);
        assertThat(germany.kind()).isEqualTo("series-point");
        assertThat(germany.geometry()).isNull();
        assertThat(germany.payload()).containsEntry("series", "agsi:DE").containsEntry("value", 92.31)
                .containsEntry("unit", "%").containsEntry("status", "final").containsEntry("area", "DE");
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> extra = (java.util.Map<String, Object>) germany.payload().get("extra");
        assertThat(extra).containsEntry("gasInStorageTWh", 224.5).containsEntry("injectionGWhD", 120.4);
        assertThat(records.get(1).payload()).containsEntry("status", "estimated");
        FieldSpecContract.assertPayloadKeysDeclared(new AgsiAdapter("").descriptor(), records);
    }

    @Test
    @DisplayName("the access-denied body is a rejected credential, not an empty result")
    void accessDenied() {
        assertThatThrownBy(() -> AgsiParser.checkAuth(
                AdapterFixtures.bytes("gie-agsi", "access-denied.json")))
                .isInstanceOf(CredentialsRejectedException.class)
                .hasMessageContaining("Invalid or missing API key");
    }

    @Test
    @DisplayName("descriptor: key required, 3 h interval, 40/day, AGSI licence")
    void descriptor() {
        AgsiAdapter adapter = new AgsiAdapter("");
        SourceDescriptor d = adapter.descriptor();
        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(d.requiredCredentialFields()).extracting(c -> c.name()).containsExactly("apiKey");
        assertThat(d.defaultInterval()).isEqualTo(java.time.Duration.ofHours(3));
        assertThat(d.defaultBudget().perDay()).isEqualTo(40);
        assertThat(d.licence()).startsWith("GIE AGSI data usage rules");
    }
}
