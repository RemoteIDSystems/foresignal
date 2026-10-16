package ai.foresignal.source.adapters.eia;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link EiaApi} (prompt 093): EIA unit spellings to UCUM, and query copies. */
class EiaApiTest {

    @Test
    @DisplayName("the units the default EIA series return map to UCUM; an unknown one stays visible")
    void ucum() {
        assertThat(EiaApi.ucum("megawatthours")).isEqualTo("MW.h");
        assertThat(EiaApi.ucum("MWh")).isEqualTo("MW.h");
        assertThat(EiaApi.ucum("$/BBL")).isEqualTo("[USD]/[bbl_us]");
        assertThat(EiaApi.ucum("MBBL")).isEqualTo("k[bbl_us]");
        assertThat(EiaApi.ucum("thousand barrels per day")).isEqualTo("k[bbl_us]/d");
        assertThat(EiaApi.ucum("%")).isEqualTo("%");
        assertThat(EiaApi.ucum("furlongs")).isEqualTo("furlongs");
        assertThat(EiaApi.ucum(" ")).isNull();
    }

    @Test
    @DisplayName("a copied query is extended without changing the original")
    void copy() {
        EiaApi.Query base = EiaApi.query().add("frequency", "hourly");
        EiaApi.Query page = base.copy().add("offset", "5000");
        assertThat(EiaApi.uri("https://x", "/r", base).toString()).isEqualTo("https://x/r?frequency=hourly");
        assertThat(EiaApi.uri("https://x", "/r", page).toString()).isEqualTo("https://x/r?frequency=hourly&offset=5000");
    }
}
