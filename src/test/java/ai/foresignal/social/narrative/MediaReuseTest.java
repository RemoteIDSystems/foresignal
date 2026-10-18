package ai.foresignal.social.narrative;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.analytics.finding.AnalyticFindingEntity;
import ai.foresignal.analytics.finding.AnalyticFindingRepository;
import ai.foresignal.test.IntegrationTest;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Media reuse (prompt 136): an image first seen three years earlier in another place yields a {@code media-reuse}
 * finding naming both narratives with the {@code REFERENCE_WINDOW} caveat and the store's start date; fingerprinting a
 * post image records its use and matches a later copy of the same picture.
 */
@IntegrationTest
class MediaReuseTest {

    static final Instant NOW = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    static final Instant THREE_YEARS_AGO = NOW.minus(Duration.ofDays(3 * 365));

    @Autowired NarrativeStore store;
    @Autowired MediaReuseDetector reuse;
    @Autowired MediaFingerprinter fingerprinter;
    @Autowired AnalyticFindingRepository findings;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        fingerprinter.reset();
    }

    private void narrative(String id, Instant first, List<String> regions) {
        store.insertNarrative(id, "Narrative " + id, null, null, List.of(), List.of(), List.of(), regions, List.of(),
                first, "CLEAR", null, first);
    }

    @Test
    @DisplayName("an image first seen 3 years earlier elsewhere yields a reuse finding with the REFERENCE_WINDOW caveat")
    void reuseFinding() {
        narrative("01JA8Q3V7S2K9W4T6Y1B5N0M3A", THREE_YEARS_AGO, List.of("SY"));
        narrative("01JA8Q3V7S2K9W4T6Y1B5N0M3B", NOW.minus(Duration.ofHours(2)), List.of("UA"));
        store.insertFingerprint(new NarrativeStore.Fingerprint("01JA8Q3V7S2K9W4T6Y1B5N0M3F",
                "f8f8f0cee0f4a84f06370a22038f63f0b36e2ed596621e1d33e6b39c4e9c9b22", 100, THREE_YEARS_AGO,
                "record:bluesky-jetstream/bluesky%3Aold", "bluesky:old", 0, List.of("bluesky"),
                List.of("01JA8Q3V7S2K9W4T6Y1B5N0M3A"), null, List.of(), THREE_YEARS_AGO, THREE_YEARS_AGO));
        store.addNarrative("01JA8Q3V7S2K9W4T6Y1B5N0M3F", "01JA8Q3V7S2K9W4T6Y1B5N0M3B", NOW);
        NarrativeStore.Fingerprint fp = store.fingerprint("01JA8Q3V7S2K9W4T6Y1B5N0M3F").orElseThrow();
        MediaReuseDetector.Reuse judged = reuse.judge(fp, store.narrative("01JA8Q3V7S2K9W4T6Y1B5N0M3B").orElseThrow());
        assertThat(judged.reason()).isEqualTo("earlier");
        assertThat(judged.earlier().id()).isEqualTo("01JA8Q3V7S2K9W4T6Y1B5N0M3A");
        String findingId = reuse.check(fp, "01JA8Q3V7S2K9W4T6Y1B5N0M3B");
        assertThat(findingId).isNotNull();
        AnalyticFindingEntity f = findings.findById(findingId).orElseThrow();
        assertThat(f.getFindingType()).isEqualTo(NarrativeVocabulary.MEDIA_REUSE_FINDING);
        assertThat(f.getStats().get("earlierNarrativeId")).isEqualTo("01JA8Q3V7S2K9W4T6Y1B5N0M3A");
        assertThat(f.getStats().get("earlierRegions")).isEqualTo(List.of("SY"));
        assertThat(f.getStats().get("narrativeRegions")).isEqualTo(List.of("UA"));
        Map<String, Object> caveat = f.getCaveats().get(0);
        assertThat(caveat.get("code")).isEqualTo(NarrativeVocabulary.REFERENCE_WINDOW);
        assertThat(caveat.get("text").toString()).contains("only knows media it has fingerprinted");
        assertThat(store.fingerprint(fp.id()).orElseThrow().reuseFindingIds()).containsExactly(findingId);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM social_event_log WHERE event = 'media.reuse'",
                Integer.class)).isEqualTo(1);
        assertThat(reuse.check(fp, "01JA8Q3V7S2K9W4T6Y1B5N0M3B")).as("one finding per fingerprint and narrative")
                .isEqualTo(findingId);
    }

    @Test
    @DisplayName("a recent image in the same place is no reuse")
    void noReuse() {
        narrative("01JA8Q3V7S2K9W4T6Y1B5N0M3C", NOW.minus(Duration.ofHours(5)), List.of("YE"));
        narrative("01JA8Q3V7S2K9W4T6Y1B5N0M3D", NOW.minus(Duration.ofHours(2)), List.of("YE"));
        NarrativeStore.Fingerprint fp = new NarrativeStore.Fingerprint("01JA8Q3V7S2K9W4T6Y1B5N0M3G",
                "0000000000000000000000000000000000000000000000000000000000000001", 80, NOW.minus(Duration.ofHours(5)),
                "record:x/x%3A1", "x:1", 1, List.of("x"), List.of("01JA8Q3V7S2K9W4T6Y1B5N0M3C",
                        "01JA8Q3V7S2K9W4T6Y1B5N0M3D"), null, List.of(), NOW, NOW);
        assertThat(reuse.judge(fp, store.narrative("01JA8Q3V7S2K9W4T6Y1B5N0M3D").orElseThrow())).isNull();
    }

    private static byte[] png(int shade) throws IOException {
        BufferedImage img = new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 240; y++) {
            for (int x = 0; x < 320; x++) {
                int v = ((x / 40 + y / 30) % 2 == 0) ? 30 : Math.min(255, 200 + shade);
                img.setRGB(x, y, v << 16 | v << 8 | v);
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "png", out);
        return out.toByteArray();
    }

    @Test
    @DisplayName("fingerprinting records uses and matches a near-identical copy to the first sighting")
    void fingerprinting() throws IOException {
        NarrativeStore.QueueItem first = new NarrativeStore.QueueItem("q1", "https://cdn.example/a.png",
                "bluesky-jetstream", "bluesky:p1", "bluesky:p1", "bluesky", null, NOW.minusSeconds(600), 0);
        NarrativeStore.QueueItem copy = new NarrativeStore.QueueItem("q2", "https://cdn.example/b.png",
                "x", "x:p2", "x:p2", "x", null, NOW, 0);
        String a = fingerprinter.process(first, png(0));
        String b = fingerprinter.process(copy, png(8));
        assertThat(b).isEqualTo(a);
        NarrativeStore.Fingerprint fp = store.fingerprint(a).orElseThrow();
        assertThat(fp.useCount()).isEqualTo(2);
        assertThat(fp.platforms()).containsExactlyInAnyOrder("bluesky", "x");
        assertThat(fp.firstPostKey()).isEqualTo("bluesky:p1");
        assertThat(fp.quality()).isGreaterThanOrEqualTo(NarrativeVocabulary.PDQ_MIN_QUALITY);
        assertThat(fp.mediaId()).as("social.keepMedia is off by default").isNull();
        assertThat(fingerprinter.search(Pdq.parse(fp.pdq()), 31, 5)).extracting(PdqIndex.Match::id).contains(a);
        assertThat(store.storeStart()).isEqualTo(NOW.minusSeconds(600));
    }
}
