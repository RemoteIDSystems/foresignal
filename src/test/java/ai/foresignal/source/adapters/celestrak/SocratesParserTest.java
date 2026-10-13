package ai.foresignal.source.adapters.celestrak;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link SocratesParser} over the captured head of {@code sort-minRange.csv} and a synthetic 12 MB file (prompt 064). */
class SocratesParserTest {

    private static final String ID = CelestrakSocratesAdapter.ID;
    private static final SocratesParser.Filter KEEP_ALL = new SocratesParser.Filter(0, Double.MAX_VALUE, Set.of());

    private static InputStream fixture() {
        return new ByteArrayInputStream(AdapterFixtures.bytes(ID, "sort-minRange-head.csv"));
    }

    @Test
    @DisplayName("header-driven columns map to the lowerCamel payload, externalId low-high-minute, eventTime TCA")
    void columns() {
        SocratesParser.Parsed parsed = SocratesParser.parse(fixture(), KEEP_ALL);
        assertThat(parsed.rows()).isEqualTo(30);
        assertThat(parsed.records()).hasSize(30);
        NormalizedRecord first = parsed.records().get(0);
        Instant tca = Instant.parse("2026-10-16T10:59:33.430Z");
        assertThat(first.externalId()).isEqualTo("66259-100303-" + Math.floorDiv(tca.getEpochSecond(), 60));
        assertThat(first.kind()).isEqualTo("conjunction");
        assertThat(first.eventTime()).isEqualTo(tca);
        assertThat(first.observedAt()).isNull();
        assertThat(first.geometry()).isNull();
        assertThat(first.payload()).containsEntry("norad1", 66259L).containsEntry("name1", "STARLINK-35529 [+]")
                .containsEntry("daysSinceEpoch1", 4.531).containsEntry("norad2", 100303L)
                .containsEntry("tca", "2026-10-16T10:59:33.430Z").containsEntry("tcaRangeKm", 0.010)
                .containsEntry("relSpeedKms", 14.759).containsEntry("maxProb", 1.0).containsEntry("dilutionKm", 0.0);
        NormalizedRecord second = parsed.records().get(1);
        assertThat(second.externalId()).startsWith("48354-54569-");
    }

    @Test
    @DisplayName("a row is kept when any filter matches: probability, range or a watched object")
    void filters() {
        SocratesParser.Filter none = new SocratesParser.Filter(2, -1, Set.of());
        assertThat(SocratesParser.parse(fixture(), none).records()).isEmpty();
        SocratesParser.Filter prob = new SocratesParser.Filter(0.1, -1, Set.of());
        assertThat(SocratesParser.parse(fixture(), prob).records())
                .allSatisfy(r -> assertThat((Double) r.payload().get("maxProb")).isGreaterThanOrEqualTo(0.1));
        SocratesParser.Filter range = new SocratesParser.Filter(2, 0.03, Set.of());
        assertThat(SocratesParser.parse(fixture(), range).records()).hasSize(5);
        SocratesParser.Filter watch = new SocratesParser.Filter(2, -1, Set.of(35061L));
        assertThat(SocratesParser.parse(fixture(), watch).records()).singleElement()
                .satisfies(r -> assertThat(r.payload().get("norad2")).isEqualTo(35061L));
    }

    @Test
    @DisplayName("a missing required column is a format error; an unreadable row is skipped")
    void malformed() {
        assertThatThrownBy(() -> SocratesParser.parse(new ByteArrayInputStream(
                "NORAD_CAT_ID_1,TCA\r\n1,2\r\n".getBytes(StandardCharsets.UTF_8)), KEEP_ALL))
                .isInstanceOf(UpstreamFormatException.class);
        String body = "NORAD_CAT_ID_1,OBJECT_NAME_1,DSE_1,NORAD_CAT_ID_2,OBJECT_NAME_2,DSE_2,TCA,TCA_RANGE,"
                + "TCA_RELATIVE_SPEED,MAX_PROB,DILUTION\r\nx,A,1,2,B,1,2026-10-16 10:59:33.430,0.1,1,1,0\r\n";
        SocratesParser.Parsed parsed = SocratesParser.parse(new ByteArrayInputStream(
                body.getBytes(StandardCharsets.UTF_8)), KEEP_ALL);
        assertThat(parsed.records()).isEmpty();
        assertThat(parsed.skipped()).isEqualTo(1);
        assertThat(SocratesParser.looksLike(body.getBytes(StandardCharsets.UTF_8))).isTrue();
        assertThat(SocratesParser.looksLike("<html>".getBytes(StandardCharsets.UTF_8))).isFalse();
    }

    @Test
    @DisplayName("a 12 MB synthetic file streams within the heap budget and is capped at 2,000 records")
    void streamingCap() {
        long before = usedHeap();
        SocratesParser.Parsed parsed = SocratesParser.parse(new SyntheticSocrates(12L * 1024 * 1024), KEEP_ALL);
        long growth = usedHeap() - before;
        assertThat(parsed.records()).hasSize(SocratesParser.MAX_RECORDS);
        assertThat(parsed.capped()).isTrue();
        SocratesParser.Parsed strict = SocratesParser.parse(new SyntheticSocrates(12L * 1024 * 1024),
                new SocratesParser.Filter(2, -1, Set.of(999_999_999L)));
        assertThat(strict.records()).isEmpty();
        assertThat(strict.rows()).isGreaterThan(80_000);
        assertThat(growth).as("heap growth while parsing 12 MB").isLessThan(64L * 1024 * 1024);
    }

    private static long usedHeap() {
        System.gc();
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }

    /** Generates SOCRATES-shaped CSV rows on the fly, so the test itself holds no 12 MB buffer. */
    static final class SyntheticSocrates extends InputStream {
        private final long size;
        private long produced;
        private byte[] chunk = ("NORAD_CAT_ID_1,OBJECT_NAME_1,DSE_1,NORAD_CAT_ID_2,OBJECT_NAME_2,DSE_2,TCA,TCA_RANGE,"
                + "TCA_RELATIVE_SPEED,MAX_PROB,DILUTION\r\n").getBytes(StandardCharsets.US_ASCII);
        private int pos;
        private long row;

        SyntheticSocrates(long size) {
            this.size = size;
        }

        @Override
        public int read() {
            if (pos >= chunk.length) {
                if (produced >= size) {
                    return -1;
                }
                row++;
                chunk = (10_000 + row % 90_000 + ",STARLINK-" + row + " [+]," + (row % 7) + ".123," + (100_000 + row)
                        + ",COSMOS DEB [-],1.500,2026-10-1" + (row % 9) + " 12:34:56.789," + (row % 1000) / 100.0
                        + ",12.345," + (row % 3 == 0 ? "1.000E-03" : "2.000E-07") + ",0.010\r\n")
                        .getBytes(StandardCharsets.US_ASCII);
                pos = 0;
            }
            produced++;
            return chunk[pos++] & 0xff;
        }
    }
}
