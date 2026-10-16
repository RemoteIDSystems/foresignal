package ai.foresignal.source.adapters.satdump;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The dcp_locations.csv reader and address normalization (prompt 107). */
class DcpLocationStoreTest {

    @Test
    @DisplayName("parses rows, skips the header and invalid rows")
    void parse() {
        String csv = "address,lat,lon,name,agency\n"
                + "0ABC,30.5,-97.5,Home,Agency\n"
                + "bad\n"
                + "XYZ,999,-97.5,Bad,Nope\n"
                + "0DEF,31.0,-98.0,,Other\n";
        List<DcpLocationStore.DcpLocation> rows =
                DcpLocationStore.parse(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).address()).isEqualTo("ABC");
        assertThat(rows.get(0).lat()).isEqualTo(30.5);
        assertThat(rows.get(0).agency()).isEqualTo("Agency");
        assertThat(rows.get(1).address()).isEqualTo("DEF");
        assertThat(rows.get(1).name()).isNull();
    }

    @Test
    @DisplayName("normalizes an address: upper-case, no leading zeros")
    void normalize() {
        assertThat(DcpLocationStore.normalize("0abc")).isEqualTo("ABC");
        assertThat(DcpLocationStore.normalize("0x0FF")).isEqualTo("FF");
        assertThat(DcpLocationStore.normalize("0")).isEqualTo("0");
    }
}
