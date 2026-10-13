package ai.foresignal.source.adapters.satdump;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link PassBook} (prompt 049, Weather-Imagery-Design §4.4): pass ids, the join window
 * [firstLock − 5 min, LOS + 5 min] at its edges, minting a pass from an unmatched product, the version
 * sequence and {@code failed} at LOS + 20 min.
 */
class PassJoinTest {

    static final Instant AOS = Instant.parse("2026-10-12T18:02:10Z");
    static final double[] HOME = {-95.37, 29.76};

    final PassBook book = new PassBook("home", SatNames.bundled());

    /** A session from AOS with lock from +60 s to +600 s and LOS at +780 s. */
    RfSession session(boolean lock) {
        RfSession s = new RfSession(book.passId("meteor-m2-3", AOS), "meteor-m2-3", "METEOR-M2 3",
                ApiSnapshot.AUTOTRACK, AOS);
        for (int t = 0; t <= 780; t += 20) {
            boolean locked = lock && t >= 60 && t <= 600;
            double el = 54.0 * Math.sin(Math.PI * t / 780.0);
            s.sample(AOS.plusSeconds(t), new ApiSnapshot(ApiSnapshot.AUTOTRACK, 137_900_000L, "METEOR-M2 3", 180.0, el,
                    null, null, true, List.of("psk_demod"), 5.0 + el / 6, 7.0, 0.1, locked ? "SYNCED" : "NOSYNC",
                    locked, 2.0, null));
        }
        s.close(AOS.plusSeconds(780));
        return s;
    }

    @Test
    @DisplayName("ids are {station}:{satKey}:{aosEpochMinute}")
    void ids() {
        assertThat(book.passId("meteor-m2-3", AOS)).isEqualTo("home:meteor-m2-3:" + AOS.getEpochSecond() / 60);
        assertThat(book.passId(null, AOS)).startsWith("home:unknown:");
    }

    @Test
    @DisplayName("receiving at AOS, recorded at LOS with rf{}, decoded when the product joins")
    void lifecycle() {
        RfSession s = session(true);
        NormalizedRecord receiving = book.open(s, HOME);
        assertThat(receiving.kind()).isEqualTo(PassBook.KIND);
        assertThat(receiving.payload()).containsEntry("status", "receiving").containsEntry("satKey", "meteor-m2-3")
                .containsEntry("norad", 57166).containsEntry("satellite", "Meteor-M2-3");
        assertThat(receiving.observedAt()).isEqualTo(AOS);
        assertThat(receiving.eventTime()).isEqualTo(AOS);
        assertThat(receiving.geometry().getCoordinate().x).isEqualTo(-95.37);

        NormalizedRecord recorded = book.close(s, HOME).orElseThrow();
        assertThat(recorded.payload()).containsEntry("status", "recorded").containsKey("rf")
                .containsEntry("los", AOS.plusSeconds(780).toString()).containsEntry("maxElevationDeg", 54.0)
                .containsEntry("title", "Meteor-M2-3 pass, max el 54°");
        assertThat(recorded.observedAt()).isEqualTo(AOS.plusSeconds(780));

        Instant now = AOS.plusSeconds(1000);
        PassState joined = book.join("meteor-m2-3", "METEOR-M2 3", AOS, now);
        assertThat(joined.externalId).isEqualTo(s.passId());
        NormalizedRecord decoded = book.decoded(joined, "pass-1", List.of("msu_mr"),
                List.of(Map.of("instrument", "msu_mr", "composite", "a.png", "thumbMediaId", "T")), null, now, HOME);
        assertThat(decoded.externalId()).isEqualTo(s.passId());
        assertThat(decoded.payload()).containsEntry("status", "decoded").containsKey("rf").containsKey("products");
        assertThat(decoded.observedAt()).isEqualTo(now);
        assertThat(book.expire(AOS.plusSeconds(780).plus(Duration.ofMinutes(25)), HOME)).isEmpty();
    }

    @Test
    @DisplayName("window edges: firstLock − 5 min and LOS + 5 min join; one second outside mints a new pass")
    void windowEdges() {
        RfSession s = session(true);
        book.open(s, HOME);
        book.close(s, HOME);
        Instant firstLock = AOS.plusSeconds(60);
        Instant los = AOS.plusSeconds(780);
        Instant now = los.plus(Duration.ofMinutes(10));

        assertThat(book.join("meteor-m2-3", "x", firstLock.minus(Duration.ofMinutes(5)), now).externalId)
                .isEqualTo(s.passId());
        assertThat(book.join("meteor-m2-3", "x", los.plus(Duration.ofMinutes(5)), now).externalId)
                .isEqualTo(s.passId());

        PassState early = book.join("meteor-m2-3", "x", firstLock.minus(Duration.ofMinutes(5)).minusSeconds(1), now);
        assertThat(early.externalId).isNotEqualTo(s.passId());
        assertThat(early.fromProduct).isTrue();

        PassState late = book.join("meteor-m2-3", "x", los.plus(Duration.ofMinutes(5)).plusSeconds(1), now);
        assertThat(late.externalId).isNotEqualTo(s.passId()).isNotEqualTo(early.externalId);
    }

    @Test
    @DisplayName("another satellite never joins; a session without a satellite (live mode) joins any")
    void satKeys() {
        RfSession s = session(true);
        book.open(s, HOME);
        book.close(s, HOME);
        PassState other = book.join("meteor-m2-4", "METEOR-M2 4", AOS.plusSeconds(120), AOS.plusSeconds(900));
        assertThat(other.externalId).isNotEqualTo(s.passId());

        PassBook liveBook = new PassBook("home", SatNames.bundled());
        RfSession live = new RfSession(liveBook.passId(null, AOS), null, null, ApiSnapshot.LIVE, AOS);
        live.sample(AOS, new ApiSnapshot(ApiSnapshot.LIVE, null, null, null, null, null, null, null, List.of("m"), 3.0,
                null, null, null, false, null, null));
        live.close(AOS.plusSeconds(600));
        liveBook.open(live, HOME);
        liveBook.close(live, HOME);
        PassState joined = liveBook.join("meteor-m2-3", "METEOR-M2 3", AOS.plusSeconds(30), AOS.plusSeconds(900));
        assertThat(joined.externalId).isEqualTo(live.passId());
        assertThat(joined.satKey).isEqualTo("meteor-m2-3");
        assertThat(joined.norad).isEqualTo(57166);
    }

    @Test
    @DisplayName("an unmatched product mints a decoded pass from the dataset timestamp; a second dataset joins it")
    void mintFromProduct() {
        Instant t = Instant.parse("2026-10-12T20:00:00Z");
        PassState minted = book.join("metop-b", "METOP-B", t, t.plusSeconds(600));
        assertThat(minted.externalId).isEqualTo(book.passId("metop-b", t));
        assertThat(minted.fromProduct).isTrue();
        NormalizedRecord r = book.decoded(minted, "d1", List.of("avhrr_3"), List.of(), null, t.plusSeconds(600), HOME);
        assertThat(r.payload()).containsEntry("status", "decoded").doesNotContainKey("los")
                .containsEntry("title", "Metop-B pass");
        assertThat(r.eventTime()).isEqualTo(t);

        PassState second = book.join("metop-b", "METOP-B", t.plusSeconds(90), t.plusSeconds(700));
        assertThat(second.externalId).isEqualTo(minted.externalId);
    }

    @Test
    @DisplayName("failed at LOS + 20 min with lock and no products, with the missing dataset.json reason")
    void failed() {
        RfSession s = session(true);
        book.open(s, HOME);
        book.close(s, HOME);
        Instant los = AOS.plusSeconds(780);

        assertThat(book.expire(los.plus(Duration.ofMinutes(20)).minusSeconds(1), HOME)).isEmpty();
        assertThat(book.missingDatasetReason()).isNull();

        List<NormalizedRecord> failed = book.expire(los.plus(Duration.ofMinutes(20)), HOME);
        assertThat(failed).hasSize(1);
        assertThat(failed.get(0).payload()).containsEntry("status", "failed");
        assertThat(failed.get(0).observedAt()).isEqualTo(los.plus(Duration.ofMinutes(20)));
        assertThat(book.missingDatasetReason()).isEqualTo("Pass ended " + los
                + " but no dataset.json after 20 min. Is `finish_processing: true` set, and server_mode off?");
        assertThat(book.expire(los.plus(Duration.ofMinutes(30)), HOME)).isEmpty();

        // A later dataset still decodes it and clears the reason.
        PassState late = book.join("meteor-m2-3", "METEOR-M2 3", AOS, los.plus(Duration.ofMinutes(31)));
        book.decoded(late, "d", List.of(), List.of(), null, los.plus(Duration.ofMinutes(31)), HOME);
        assertThat(book.missingDatasetReason()).isNull();
    }

    @Test
    @DisplayName("without lock a pass with no products stays recorded")
    void noLockNoFailure() {
        RfSession s = session(false);
        book.open(s, HOME);
        book.close(s, HOME);
        assertThat(book.expire(AOS.plus(Duration.ofHours(1)), HOME)).isEmpty();
        assertThat(book.find(s.passId()).orElseThrow().status).isEqualTo(PassBook.RECORDED);
    }

    @Test
    @DisplayName("finished passes are kept a day, at most 50")
    void pruning() {
        for (int i = 0; i < 60; i++) {
            Instant t = AOS.plus(Duration.ofMinutes(25L * i));
            book.join("goes-19", "GOES-19", t, t);
        }
        assertThat(book.passes()).hasSize(PassBook.MAX_PASSES);
        book.prune(AOS.plus(Duration.ofDays(3)));
        assertThat(book.passes()).isEmpty();
    }
}
