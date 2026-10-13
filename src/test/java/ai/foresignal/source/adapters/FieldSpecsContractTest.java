package ai.foresignal.source.adapters;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.adapters.adsblol.AdsbLolAreaFamily;
import ai.foresignal.source.adapters.adsblol.AdsbLolMilAdapter;
import ai.foresignal.source.adapters.opensky.OpenSkyStatesAdapter;
import ai.foresignal.source.adapters.opensky.OpenSkyStatesParser;
import ai.foresignal.source.formats.readsb.MapperOptions;
import ai.foresignal.source.formats.readsb.ReadsbAircraftMapper;
import ai.foresignal.source.formats.readsb.ReadsbDocument;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The 011 {@code fieldSpecs} contract for the three aircraft sources of prompt 062: every payload key of every
 * fixture is declared, with the declared JSON type.
 */
class FieldSpecsContractTest {

    private static List<NormalizedRecord> readsb(String dir, String file) {
        return ReadsbAircraftMapper.mapAll(ReadsbDocument.parse(AdapterFixtures.bytes(dir, file)), MapperOptions.DEFAULTS)
                .records();
    }

    @Test
    @DisplayName("adsblol-mil: the /v2/mil capture and the readsb edge cases")
    void mil() {
        SourceDescriptor d = new AdsbLolMilAdapter("").descriptor();
        List<NormalizedRecord> records = readsb("adsblol-mil", "mil.json");
        assertThat(records).hasSize(30);
        FieldSpecContract.assertPayloadKeysDeclared(d, records);
        FieldSpecContract.assertPayloadKeysDeclared(d, readsb("readsb", "readsb-edge.json"));
    }

    @Test
    @DisplayName("adsblol-area-*: the /v2/point capture")
    void area() {
        SourceDescriptor d = new AdsbLolAreaFamily("").adapters().getFirst().descriptor();
        List<NormalizedRecord> records = readsb("adsblol-area", "point-41.98--87.90-250.json");
        assertThat(records).hasSize(30);
        FieldSpecContract.assertPayloadKeysDeclared(d, records);
    }

    @Test
    @DisplayName("opensky-states: the bbox capture and the edge cases")
    void opensky() {
        SourceDescriptor d = new OpenSkyStatesAdapter("", "").descriptor();
        FieldSpecContract.assertPayloadKeysDeclared(d,
                OpenSkyStatesParser.parse(AdapterFixtures.bytes("opensky-states", "states-bbox.json")));
        FieldSpecContract.assertPayloadKeysDeclared(d,
                OpenSkyStatesParser.parse(AdapterFixtures.bytes("opensky-states", "states-edge.json")));
    }
}
