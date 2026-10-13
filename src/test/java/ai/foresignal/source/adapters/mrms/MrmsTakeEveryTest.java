package ai.foresignal.source.adapters.mrms;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.S3Object;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The fixed {@code takeEvery} thinning of the MRMS second wave (prompt 053): the keys a listing yields at each
 * setting, independent of what the files hold, plus the parameters and product specs that go with it.
 */
class MrmsTakeEveryTest {

    private static List<MrmsAdapter.Frame> listing(MrmsAdapter.Upstream upstream, Instant from, int files, int skip) {
        List<MrmsAdapter.Frame> out = new ArrayList<>();
        for (int i = 0; i < files; i++) {
            if (skip >= 0 && i == skip) {
                continue;
            }
            Instant t = from.plus(Duration.ofMinutes(2L * i));
            String key = upstream.key(t);
            out.add(MrmsAdapter.frame(upstream, new S3Object(key, 700_000L, null, t)).orElseThrow());
        }
        return out;
    }

    private static List<String> stamps(List<MrmsAdapter.Frame> frames) {
        return frames.stream().map(f -> f.time().toString().substring(11, 16)).toList();
    }

    @Test
    @DisplayName("takeEvery 5 keeps the first file of each 10-minute bucket, aligned to :00, :10, :20")
    void tenMinuteBuckets() {
        List<MrmsAdapter.Frame> frames = listing(MrmsAdapter.QPE_1H_UPSTREAM, Instant.parse("2026-10-13T04:02:00Z"), 15, -1);
        assertThat(stamps(MrmsAdapter.thin(frames, null, 5))).containsExactly("04:02", "04:10", "04:20", "04:30");
        assertThat(stamps(MrmsAdapter.thin(frames, Instant.parse("2026-10-13T04:00:00Z"), 5)))
                .as("the cursor's bucket was already taken").containsExactly("04:10", "04:20", "04:30");
        assertThat(stamps(MrmsAdapter.thin(frames, Instant.parse("2026-10-13T04:12:00Z"), 5)))
                .as("a thinned-out cursor in 04:10 keeps that bucket closed").containsExactly("04:20", "04:30");
    }

    @Test
    @DisplayName("a missing upstream file moves the bucket's pick to the next file, never to another bucket")
    void missingFile() {
        // 04:10 is absent upstream: 04:12 represents its bucket.
        List<MrmsAdapter.Frame> frames = listing(MrmsAdapter.FFG_UPSTREAM, Instant.parse("2026-10-13T04:00:00Z"), 11, 5);
        assertThat(stamps(MrmsAdapter.thin(frames, null, 5))).containsExactly("04:00", "04:12", "04:20");
    }

    @Test
    @DisplayName("takeEvery 1 keeps every file; the choice depends only on the keys (same listing, same picks)")
    void everyFileAndDeterministic() {
        List<MrmsAdapter.Frame> frames = listing(MrmsAdapter.ROTATION_UPSTREAM, Instant.parse("2026-10-13T04:00:00Z"), 6, -1);
        assertThat(MrmsAdapter.thin(frames, null, 1)).isEqualTo(frames);
        List<MrmsAdapter.Frame> qpe = listing(MrmsAdapter.QPE_1H_UPSTREAM, Instant.parse("2026-10-13T00:00:00Z"), 30, -1);
        assertThat(MrmsAdapter.thin(qpe, null, 5)).isEqualTo(MrmsAdapter.thin(List.copyOf(qpe), null, 5)).hasSize(6);
        assertThat(MrmsAdapter.thin(qpe, null, 15)).as("30-minute buckets").hasSize(2);
    }

    @Test
    @DisplayName("parameters: four fixed takeEvery integers with the prompt's defaults, checked 1..30")
    void parameters() {
        MrmsAdapter adapter = new MrmsAdapter(null, "");
        SourceDescriptor d = adapter.descriptor();
        Map<String, Object> defaults = new java.util.HashMap<>();
        for (ParamSpec p : d.params()) {
            defaults.put(p.name(), p.defaultValue());
        }
        assertThat(defaults).containsEntry(MrmsAdapter.ROTATION_TAKE_EVERY, 1).containsEntry(MrmsAdapter.QPE_1H_TAKE_EVERY, 5)
                .containsEntry(MrmsAdapter.QPE_24H_TAKE_EVERY, 1).containsEntry(MrmsAdapter.FFG_TAKE_EVERY, 5);
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(MrmsAdapter.QPE_1H_TAKE_EVERY, 10)))).isEmpty();
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(MrmsAdapter.FFG_TAKE_EVERY, 0))))
                .containsKey(MrmsAdapter.FFG_TAKE_EVERY);
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(MrmsAdapter.ROTATION_TAKE_EVERY, 31))))
                .containsKey(MrmsAdapter.ROTATION_TAKE_EVERY);
        MrmsAdapter.SecondWave qpe = MrmsAdapter.SECOND_WAVE.get(1);
        assertThat(qpe.takeEvery(ValidatedParams.empty())).isEqualTo(5);
        assertThat(qpe.takeEvery(ValidatedParams.of(Map.of(MrmsAdapter.QPE_1H_TAKE_EVERY, 3)))).isEqualTo(3);
        assertThat(qpe.takeEvery(ValidatedParams.of(Map.of(MrmsAdapter.QPE_1H_TAKE_EVERY, 99)))).isEqualTo(30);
    }

    @Test
    @DisplayName("products: units, styles, gaps and ladders of the four second-wave products")
    void products() {
        Map<String, ProductSpec> specs = new java.util.HashMap<>();
        for (ProductSpec spec : new MrmsAdapter(null, "").descriptor().products()) {
            specs.put(spec.id(), spec);
        }
        assertThat(specs).containsKeys(MrmsAdapter.ROTATION, MrmsAdapter.QPE_1H, MrmsAdapter.QPE_24H, MrmsAdapter.FFG_RATIO);
        assertThat(specs.get(MrmsAdapter.ROTATION).defaultStyle()).isEqualTo("rotation@1");
        assertThat(specs.get(MrmsAdapter.ROTATION).units()).isEqualTo("s-1");
        assertThat(specs.get(MrmsAdapter.ROTATION).temporal().maxGap()).isEqualTo(Duration.ofMinutes(6));
        assertThat(specs.get(MrmsAdapter.QPE_1H).defaultStyle()).isEqualTo("qpe@1");
        assertThat(specs.get(MrmsAdapter.QPE_1H).temporal().cadence()).isEqualTo(Duration.ofMinutes(10));
        assertThat(specs.get(MrmsAdapter.QPE_1H).temporal().maxGap()).isEqualTo(Duration.ofMinutes(30));
        assertThat(specs.get(MrmsAdapter.QPE_24H).temporal().maxGap()).isEqualTo(Duration.ofMinutes(150));
        assertThat(specs.get(MrmsAdapter.QPE_24H).retention().summary()).isEqualTo("every frame for 48h");
        assertThat(specs.get(MrmsAdapter.FFG_RATIO).defaultStyle()).isEqualTo("ffg-ratio@1");
        assertThat(specs.get(MrmsAdapter.FFG_RATIO).units()).isEqualTo("%");
        for (String id : List.of(MrmsAdapter.ROTATION, MrmsAdapter.QPE_1H, MrmsAdapter.QPE_24H, MrmsAdapter.FFG_RATIO)) {
            assertThat(specs.get(id).spatial().coverage()).isEqualTo("CONUS");
            assertThat(specs.get(id).spatial().nativeMaxZoom()).isEqualTo(8);
            assertThat(specs.get(id).spatial().maxZoomForAnimation()).as("warmed like reflectivity").isEqualTo(7);
        }
        assertThat(new MrmsAdapter(null, "").decoderRequirements()).containsKeys(MrmsAdapter.ROTATION,
                MrmsAdapter.QPE_1H, MrmsAdapter.QPE_24H, MrmsAdapter.FFG_RATIO);
    }
}
