package ai.foresignal.source.adapters.internet;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Set;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code ripe-atlas-probes} parsing (prompt 092): bzip2, status filter and dropped host fields. */
class AtlasDumpParserTest {

    @Test
    @DisplayName("the bzip2 dump parses, addresses and descriptions are never stored")
    void dump() throws Exception {
        byte[] json;
        try (BZip2CompressorInputStream in = new BZip2CompressorInputStream(
                new ByteArrayInputStream(AdapterFixtures.bytes("ripe-atlas-probes", "meta-latest.bz2")))) {
            json = in.readAllBytes();
        }
        List<NormalizedRecord> records = new AtlasProbesAdapter(null).parse(json, Set.of(1, 2));
        assertThat(records).isNotEmpty();
        assertThat(records).extracting(NormalizedRecord::kind).containsOnly("probe-status");
        assertThat(records).allMatch(r -> !r.payload().containsKey("address_v4"));
        assertThat(records).allMatch(r -> !r.payload().containsKey("description"));
        assertThat(records).allMatch(r -> r.observedAt() != null, "status_since parsed");
    }
}
