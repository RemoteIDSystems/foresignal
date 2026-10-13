package ai.foresignal.source.adapters.satdump;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.source.InboxFiles;
import ai.foresignal.source.spi.NormalizedRecord;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * {@link InboxScanner} and the station's inbox loop (prompt 049, Weather-Imagery-Design §4.3):
 * readiness after 2 stable scans, links and escapes refused ({@code NOFOLLOW_LINKS} and the per-file
 * realpath re-check), {@code .cadu} never copied, one budget unit per scan, and the missing
 * {@code dataset.json} health reason 20 minutes after LOS.
 */
class InboxScannerTest {

    static final Instant T = Instant.parse("2026-10-12T18:20:00Z");

    @TempDir Path temp;

    Path inboxRoot() throws Exception {
        return Files.createDirectories(temp.resolve("inbox"));
    }

    InboxFiles inbox(Path root) {
        return new InboxFiles(new ForesignalProperties(temp, root, false, null));
    }

    @Test
    @DisplayName("a pass folder is ready after its files were unchanged for 2 scans, then never again")
    void readiness() throws Exception {
        Path root = inboxRoot();
        Path station = root.resolve("satdump/home");
        SatDumpFixtures.copy(SatDumpFixtures.PASS, station.resolve("pass-a"));
        InboxScanner scanner = new InboxScanner();
        InboxFiles inbox = inbox(root);

        assertThat(scanner.scan(inbox, station, T).ready()).isEmpty();
        assertThat(scanner.scan(inbox, station, T.plusSeconds(30)).ready()).isEmpty();
        InboxScanner.Scan third = scanner.scan(inbox, station, T.plusSeconds(60));
        assertThat(third.ready()).extracting(InboxScanner.Folder::relative).containsExactly("pass-a");
        scanner.markProcessed("pass-a", third.ready().get(0).signature());
        assertThat(scanner.scan(inbox, station, T.plusSeconds(90)).ready()).isEmpty();
    }

    @Test
    @DisplayName("a file still growing resets the count")
    void growingFile() throws Exception {
        Path root = inboxRoot();
        Path station = root.resolve("satdump/home");
        SatDumpFixtures.copy(SatDumpFixtures.PASS, station.resolve("pass-a"));
        InboxScanner scanner = new InboxScanner();
        InboxFiles inbox = inbox(root);
        scanner.scan(inbox, station, T);
        scanner.scan(inbox, station, T.plusSeconds(30));
        Path composite = station.resolve("pass-a/MSU-MR/rgb_MSU-MR_221_Composite.png");
        Files.setLastModifiedTime(composite, FileTime.from(T.plusSeconds(55)));
        assertThat(scanner.scan(inbox, station, T.plusSeconds(60)).ready()).isEmpty();
        assertThat(scanner.scan(inbox, station, T.plusSeconds(90)).ready()).isEmpty();
        assertThat(scanner.scan(inbox, station, T.plusSeconds(120)).ready()).hasSize(1);
    }

    @Test
    @DisplayName("a symlink is never followed: neither a linked folder nor a linked file is read")
    void symlinks() throws Exception {
        Path root = inboxRoot();
        Path station = root.resolve("satdump/home");
        Path outside = Files.createDirectories(temp.resolve("outside"));
        SatDumpFixtures.copy(SatDumpFixtures.PASS, outside.resolve("secret-pass"));
        Files.createDirectories(station);
        Files.createSymbolicLink(station.resolve("linked-pass"), outside.resolve("secret-pass"));
        SatDumpFixtures.copy(SatDumpFixtures.PASS, station.resolve("pass-a"));
        Files.delete(station.resolve("pass-a/dataset.json"));
        Files.createSymbolicLink(station.resolve("pass-a/dataset.json"), outside.resolve("secret-pass/dataset.json"));
        InboxScanner scanner = new InboxScanner();
        InboxFiles inbox = inbox(root);
        for (int i = 0; i < 3; i++) {
            assertThat(scanner.scan(inbox, station, T.plusSeconds(30L * i)).ready()).isEmpty();
        }
        assertThatThrownBy(() -> inbox.open(station.resolve("pass-a/dataset.json")))
                .hasMessageContaining("symbolic link");
        assertThatThrownBy(() -> inbox.stat(station.resolve("pass-a/dataset.json")))
                .hasMessageContaining("symbolic link");
    }

    @Test
    @DisplayName("the realpath is re-checked: an inbox path outside the inbox is refused at scan time")
    void escape() throws Exception {
        Path root = inboxRoot();
        Path outside = Files.createDirectories(temp.resolve("outside"));
        InboxScanner scanner = new InboxScanner();
        assertThatThrownBy(() -> scanner.scan(inbox(root), outside, T)).hasMessageContaining("outside the inbox");
        // A station directory replaced by a link after validation resolves outside too.
        Files.createDirectories(root.resolve("satdump"));
        Files.createSymbolicLink(root.resolve("satdump/home"), outside);
        assertThatThrownBy(() -> scanner.scan(inbox(root), root.resolve("satdump/home"), T))
                .hasMessageContaining("outside the inbox");
    }

    @Test
    @DisplayName("the station copies dataset.json, product.cbor and the images, never the .cadu; one unit per scan")
    void processing() throws Exception {
        Path root = inboxRoot();
        Path station = root.resolve("satdump/home");
        SatDumpFixtures.copy(SatDumpFixtures.PASS, station.resolve("2026-10-12_18-02_meteor"));
        SatDumpTestContext ctx = new SatDumpTestContext(root, T);
        ctx.params.put(SatDumpDescriptor.INBOX_PATH, station.toRealPath());
        SatDumpStation s = ctx.station();

        s.scanInboxOnce();
        ctx.clock.advance(Duration.ofSeconds(30));
        s.scanInboxOnce();
        assertThat(ctx.drain()).isEmpty();
        ctx.clock.advance(Duration.ofSeconds(30));
        s.scanInboxOnce();

        List<NormalizedRecord> records = ctx.drain();
        assertThat(records).hasSize(1);
        NormalizedRecord pass = records.get(0);
        assertThat(pass.kind()).isEqualTo(PassBook.KIND);
        assertThat(pass.externalId()).isEqualTo("home:meteor-m2-3:" + Instant.parse("2026-10-12T18:02:10Z").getEpochSecond() / 60);
        assertThat(pass.payload()).containsEntry("status", "decoded").containsEntry("instruments", List.of("msu_mr"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> products = (List<Map<String, Object>>) pass.payload().get("products");
        assertThat(products).hasSize(1);
        assertThat(products.get(0)).containsEntry("composite", "rgb_MSU-MR_221_Composite.png")
                .containsEntry("instrument", "msu_mr").containsKeys("mediaId", "thumbMediaId");
        assertThat(products.get(0).get("thumbMediaId").toString()).endsWith("S");
        assertThat(ctx.media.stored).hasSize(1);
        assertThat(ctx.media.stored.get(0).recordExternalId()).isEqualTo(pass.externalId());
        assertThat(ctx.media.stored.get(0).licence()).isEqualTo("operator-owned");

        String dir = "home/" + pass.externalId().replace(':', '_') + "/";
        assertThat(ctx.rasters.provenance.keySet()).containsExactlyInAnyOrder(dir + "dataset.json",
                dir + "MSU-MR/product.cbor", dir + "MSU-MR/MSU-MR-1.png", dir + "MSU-MR/MSU-MR-2.png",
                dir + "MSU-MR/rgb_MSU-MR_221_Composite.png");
        assertThat(ctx.rasters.provenance.keySet()).noneMatch(k -> k.endsWith(".cadu"));
        assertThat(ctx.charged.get()).isEqualTo(3);

        ctx.clock.advance(Duration.ofSeconds(30));
        s.scanInboxOnce();
        assertThat(ctx.drain()).isEmpty();
        assertThat(ctx.charged.get()).isEqualTo(4);
    }

    @Test
    @DisplayName("a spent budget skips the scan without reading anything")
    void budget() throws Exception {
        Path root = inboxRoot();
        Path station = Files.createDirectories(root.resolve("satdump/home"));
        SatDumpTestContext ctx = new SatDumpTestContext(root, T);
        ctx.params.put(SatDumpDescriptor.INBOX_PATH, station.toRealPath());
        ctx.budgetSpent = true;
        SatDumpStation s = ctx.station();
        s.scanInboxOnce();
        assertThat(ctx.charged.get()).isZero();
        assertThat(ctx.state.get(StationStore.WATCHES)).contains("{}");
    }

    @Test
    @DisplayName("20 minutes after LOS with lock and no dataset.json: failed, and the health reason")
    void missingDataset() throws Exception {
        Path root = inboxRoot();
        Path station = Files.createDirectories(root.resolve("satdump/home"));
        SatDumpTestContext ctx = new SatDumpTestContext(root, T);
        ctx.params.put(SatDumpDescriptor.INBOX_PATH, station.toRealPath());
        SatDumpStation s = ctx.station();
        Instant aos = T;
        RfSession session = new RfSession(s.book().passId("meteor-m2-3", aos), "meteor-m2-3", "METEOR-M2 3",
                ApiSnapshot.AUTOTRACK, aos);
        session.sample(aos.plusSeconds(60), new ApiSnapshot(ApiSnapshot.AUTOTRACK, 137_900_000L, "METEOR-M2 3", 1.0,
                20.0, null, null, true, List.of("m"), 9.0, 10.0, 0.1, "SYNCED", true, 1.0, null));
        session.close(aos.plusSeconds(600));
        s.book().open(session, null);
        s.book().close(session, null);

        ctx.clock.setInstant(aos.plusSeconds(600).plus(Duration.ofMinutes(19)));
        s.scanInboxOnce();
        assertThat(ctx.drain()).isEmpty();
        assertThat(ctx.state.get("health.degraded")).isEmpty();

        ctx.clock.setInstant(aos.plusSeconds(600).plus(Duration.ofMinutes(20)));
        s.scanInboxOnce();
        assertThat(ctx.drain()).singleElement().satisfies(r -> assertThat(r.payload()).containsEntry("status", "failed"));
        assertThat(ctx.state.get("health.degraded")).contains("Pass ended " + aos.plusSeconds(600)
                + " but no dataset.json after 20 min. Is `finish_processing: true` set, and server_mode off?");
    }
}
