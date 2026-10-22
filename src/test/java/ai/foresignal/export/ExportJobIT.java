package ai.foresignal.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.export.ExportItSupport;
import ai.foresignal.testsupport.retention.RetentionTestSources;
import java.io.ByteArrayInputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Bulk exports end to end (prompt 111): submit, run on the bounded worker after commit, list and
 * poll with ownership, download (single file, bundle zip, {@code Range}, {@code ETag}), the signed
 * manifest, cancellation, the disk quota, keyset streaming of 50,000 rows in bounded pages, expiry
 * to {@code 410}, the interrupted-job sweep and the orphan sweep.
 */
@IntegrationTest
@Import(RetentionTestSources.class)
class ExportJobIT extends ExportItSupport {

    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired ExportJobService exports;
    @Autowired ExportQueryService query;
    @Autowired ExportStore store;
    @Autowired ExportWorker worker;
    @Autowired ExportRetentionStep exportRetention;

    private void seed(int count) {
        List<NormalizedRecord> batch = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            batch.add(point("e" + i, STD_KIND, T0, -97.0 + (i % 100) * 0.001, 30.0 + (i / 100) * 0.001, i));
            if (batch.size() == 2_000) {
                ingestAt(STD, T0, batch.toArray(NormalizedRecord[]::new));
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            ingestAt(STD, T0, batch.toArray(NormalizedRecord[]::new));
        }
    }

    private String submit(String bearer, String body) throws Exception {
        MvcResult result = mvc.perform(post("/v1/exports").header(HttpHeaders.AUTHORIZATION, bearer)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.kind").value("EXPORT"))
                .andReturn();
        return JSON.readTree(result.getResponse().getContentAsString()).get("id").asString();
    }

    private JsonNode job(String bearer, String id) throws Exception {
        return JSON.readTree(mvc.perform(get("/v1/exports/" + id).header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }

    @Test
    @DisplayName("lifecycle: 202, DONE with rows and files, owner-only visibility, download, manifest, delete")
    void lifecycle() throws Exception {
        seed(3);
        String id = submit(agent, "{\"sources\":[\"" + STD + "\"],\"format\":\"geoparquet\",\"name\":\"three\"}");
        ExportJobEntity done = awaitTerminal(id);
        assertThat(done.getStatus()).isEqualTo(ExportJobEntity.DONE);
        assertThat(done.getRowCount()).isEqualTo(3L);
        JsonNode view = job(agent, id);
        assertThat(view.get("status").asString()).isEqualTo("DONE");
        assertThat(view.get("files").get(0).get("name").asString()).isEqualTo(id + ".parquet");
        assertThat(view.get("downloadUrl").asString()).endsWith("/v1/exports/" + id + "/download");
        assertThat(view.get("licenceSummary").get("licences").get(0).get("rows").asLong()).isEqualTo(3L);

        mvc.perform(get("/v1/exports/" + id).header(HttpHeaders.AUTHORIZATION, otherAgent)).andExpect(status().isNotFound());
        mvc.perform(get("/v1/exports/" + id).header(HttpHeaders.AUTHORIZATION, admin)).andExpect(status().isOk());
        String mine = mvc.perform(get("/v1/exports").header(HttpHeaders.AUTHORIZATION, agent))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(mine).contains(id);
        String theirs = mvc.perform(get("/v1/exports?kind=export").header(HttpHeaders.AUTHORIZATION, otherAgent))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(theirs).doesNotContain(id);
        mvc.perform(get("/v1/exports?status=nope").header(HttpHeaders.AUTHORIZATION, agent))
                .andExpect(status().isBadRequest());

        MvcResult download = mvc.perform(get("/v1/exports/" + id + "/download").header(HttpHeaders.AUTHORIZATION, agent))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ETAG, "\"" + view.get("files").get(0).get("sha256").asString() + "\""))
                .andExpect(header().string(HttpHeaders.ACCEPT_RANGES, "bytes"))
                .andReturn();
        Path copy = Files.createTempFile("export", ".parquet");
        Files.write(copy, download.getResponse().getContentAsByteArray());
        assertThat(GeoParquetFiles.rowCount(copy)).isEqualTo(3);
        Files.delete(copy);

        MvcResult manifest = mvc.perform(get("/v1/exports/" + id + "/manifest").header(HttpHeaders.AUTHORIZATION, agent))
                .andExpect(status().isOk()).andReturn();
        assertThat(manifest.getResponse().getHeader("Foresignal-Signature")).contains("..");
        JsonNode body = JSON.readTree(manifest.getResponse().getContentAsString());
        assertThat(body.get("manifestVersion").asInt()).isEqualTo(1);
        assertThat(body.get("files").get(0).get("rows").asLong()).isEqualTo(3L);

        mvc.perform(delete("/v1/exports/" + id).header(HttpHeaders.AUTHORIZATION, otherAgent))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/v1/exports/" + id).header(HttpHeaders.AUTHORIZATION, agent))
                .andExpect(status().isNoContent());
        mvc.perform(get("/v1/exports/" + id).header(HttpHeaders.AUTHORIZATION, agent)).andExpect(status().isNotFound());
        assertThat(Files.exists(store.dirOf(ExportJobEntity.EXPORT, id))).isFalse();
    }

    @Test
    @DisplayName("each format writes its file: .gpkg, .flatgeobuf and a history-mode .gpx with track points")
    void formats() throws Exception {
        seed(2);
        ingestAt(TRK, T0, point("plane", TRK_KIND, T0, -97.0, 30.0, 1));
        Instant later = T0.plusSeconds(30);
        ingestAt(TRK, later, point("plane", TRK_KIND, later, -97.1, 30.1, 2));
        for (String[] format : new String[][] {{"geopackage", "gpkg"}, {"flatgeobuf", "flatgeobuf"}}) {
            String id = submit(agent, "{\"sources\":[\"" + STD + "\"],\"format\":\"" + format[0] + "\"}");
            ExportJobEntity done = awaitTerminal(id);
            assertThat(done.getStatus()).as(format[0]).isEqualTo(ExportJobEntity.DONE);
            assertThat(Files.isRegularFile(store.dirOf(done).resolve(id + "." + format[1]))).isTrue();
        }
        String gpx = submit(agent, "{\"sources\":[\"" + TRK + "\",\"" + STD + "\"],\"format\":\"gpx\",\"mode\":\"history\"}");
        ExportJobEntity done = awaitTerminal(gpx);
        assertThat(done.getStatus()).isEqualTo(ExportJobEntity.DONE);
        String xml = Files.readString(store.dirOf(done).resolve(gpx + ".gpx"));
        assertThat(xml).contains("<trk>").contains("<name>" + TRK + "/plane</name>").contains("<ele>101</ele>")
                .contains("<wpt ");
        assertThat(xml.split("<trkpt ", -1)).hasSize(3);
    }

    @Test
    @DisplayName("bundle=zip holds the data and the four sidecars; Range answers 206 and If-None-Match 304")
    void bundleRangeAndEtag() throws Exception {
        seed(5);
        String id = submit(agent, "{\"sources\":[\"" + STD + "\"]}");
        assertThat(awaitTerminal(id).getStatus()).isEqualTo(ExportJobEntity.DONE);
        MvcResult zip = mvc.perform(get("/v1/exports/" + id + "/download?bundle=zip").header(HttpHeaders.AUTHORIZATION, agent))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/zip"))
                .andReturn();
        List<String> names = new ArrayList<>();
        try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip.getResponse().getContentAsByteArray()))) {
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                names.add(entry.getName());
            }
        }
        assertThat(names).containsExactlyInAnyOrder(id + ".parquet", "manifest.json", "manifest.json.jws",
                "licence.json", "ATTRIBUTION.txt");
        String etag = zip.getResponse().getHeader(HttpHeaders.ETAG);
        MvcResult again = mvc.perform(get("/v1/exports/" + id + "/download?bundle=zip")
                .header(HttpHeaders.AUTHORIZATION, agent)).andReturn();
        assertThat(again.getResponse().getHeader(HttpHeaders.ETAG)).as("a stable bundle ETag").isEqualTo(etag);

        MvcResult range = mvc.perform(get("/v1/exports/" + id + "/download").header(HttpHeaders.AUTHORIZATION, agent)
                        .header(HttpHeaders.RANGE, "bytes=0-9"))
                .andExpect(status().isPartialContent())
                .andReturn();
        assertThat(range.getResponse().getContentAsByteArray()).hasSize(10);
        assertThat(new String(range.getResponse().getContentAsByteArray(), 0, 4)).isEqualTo("PAR1");
        assertThat(range.getResponse().getHeader(HttpHeaders.CONTENT_RANGE)).startsWith("bytes 0-9/");
        String fileEtag = mvc.perform(get("/v1/exports/" + id + "/download").header(HttpHeaders.AUTHORIZATION, agent))
                .andReturn().getResponse().getHeader(HttpHeaders.ETAG);
        mvc.perform(get("/v1/exports/" + id + "/download").header(HttpHeaders.AUTHORIZATION, agent)
                .header(HttpHeaders.IF_NONE_MATCH, fileEtag)).andExpect(status().isNotModified());
        mvc.perform(get("/v1/exports/" + id + "/download?file=licence.json").header(HttpHeaders.AUTHORIZATION, agent))
                .andExpect(status().isOk());
        mvc.perform(get("/v1/exports/" + id + "/download?file=nope.bin").header(HttpHeaders.AUTHORIZATION, agent))
                .andExpect(status().isNotFound());
        mvc.perform(get("/v1/exports/" + id + "/download?bundle=tar").header(HttpHeaders.AUTHORIZATION, agent))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("a queued job is canceled before it runs; a download before DONE is 409 EXPORT_NOT_READY")
    void cancellationAndNotReady() throws Exception {
        seed(2);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch occupied = new CountDownLatch(1);
        worker.submit(() -> {
            occupied.countDown();
            try {
                release.await(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        try {
            assertThat(occupied.await(30, TimeUnit.SECONDS)).isTrue();
            String id = submit(agent, "{\"sources\":[\"" + STD + "\"]}");
            mvc.perform(get("/v1/exports/" + id + "/download").header(HttpHeaders.AUTHORIZATION, agent))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("EXPORT_NOT_READY"));
            mvc.perform(delete("/v1/exports/" + id).header(HttpHeaders.AUTHORIZATION, agent))
                    .andExpect(status().isNoContent());
            release.countDown();
            assertThat(awaitTerminal(id).getStatus()).isEqualTo(ExportJobEntity.CANCELED);
        } finally {
            release.countDown();
        }
    }

    @Test
    @DisplayName("a running job stops at the next page when canceled")
    void cancelWhileRunning() throws Exception {
        seed(25_000);
        String id = submit(agent, "{\"sources\":[\"" + STD + "\"],\"mode\":\"history\"}");
        org.awaitility.Awaitility.await().atMost(Duration.ofSeconds(60))
                .until(() -> jobs.findById(id).map(j -> !ExportJobEntity.QUEUED.equals(j.getStatus())).orElse(false));
        exports.delete(id);
        String status = awaitTerminal(id).getStatus();
        assertThat(status).isIn(ExportJobEntity.CANCELED, ExportJobEntity.DONE);
        if (ExportJobEntity.CANCELED.equals(status)) {
            assertThat(Files.exists(store.dirOf(ExportJobEntity.EXPORT, id))).isFalse();
        }
    }

    @Test
    @DisplayName("over the disk quota a new export is 409 EXPORT_QUOTA")
    void quota() throws Exception {
        Path filler = store.exportsRoot().resolve("quota-filler");
        Files.createDirectories(filler);
        Path sparse = filler.resolve("sparse.bin");
        try (RandomAccessFile file = new RandomAccessFile(sparse.toFile(), "rw")) {
            file.setLength(21L * 1024 * 1024 * 1024);
        }
        try {
            mvc.perform(post("/v1/exports").header(HttpHeaders.AUTHORIZATION, agent)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"sources\":[\"" + STD + "\"]}"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("EXPORT_QUOTA"));
        } finally {
            ExportStore.deleteTree(filler);
        }
    }

    @Test
    @DisplayName("50,000 rows stream in keyset pages of at most 10,000 and export completely")
    void keysetStreaming() throws Exception {
        seed(50_000);
        for (String mode : List.of("current", "history")) {
            AtomicLong rows = new AtomicLong();
            AtomicInteger pages = new AtomicInteger();
            AtomicLong inPage = new AtomicLong();
            AtomicLong maxInPage = new AtomicLong();
            ExportSpec spec = ExportSpec.parse(JSON.readTree("{\"mode\":\"" + mode + "\"}"));
            query.stream(spec, List.of(STD), ReadRestriction.NONE, null, new ExportQueryService.Sink() {
                @Override
                public void begin(long watermarkSeq) {
                    assertThat(watermarkSeq).isPositive();
                }

                @Override
                public void row(Map<String, Object> row) {
                    rows.incrementAndGet();
                    maxInPage.accumulateAndGet(inPage.incrementAndGet(), Math::max);
                }

                @Override
                public void page() {
                    pages.incrementAndGet();
                    inPage.set(0);
                }
            });
            assertThat(rows.get()).as(mode).isEqualTo(50_000L);
            assertThat(maxInPage.get()).as(mode).isLessThanOrEqualTo(ExportProperties.DEFAULT_PAGE_SIZE);
            assertThat(pages.get()).as(mode).isGreaterThanOrEqualTo(5);
        }
        String id = submit(agent, "{\"sources\":[\"" + STD + "\"],\"mode\":\"history\"}");
        ExportJobEntity done = awaitTerminal(id);
        assertThat(done.getStatus()).isEqualTo(ExportJobEntity.DONE);
        assertThat(done.getRowCount()).isEqualTo(50_000L);
        assertThat(GeoParquetFiles.footer(store.dirOf(done).resolve(id + ".parquet")).getRow_groups()).hasSize(1);
    }

    @Test
    @DisplayName("past expiresAt retention expires the job and downloads answer 410")
    void expiry() throws Exception {
        seed(1);
        String id = submit(agent, "{\"sources\":[\"" + STD + "\"]}");
        ExportJobEntity done = awaitTerminal(id);
        done.setExpiresAt(Instant.parse("2020-01-01T00:00:00Z"));
        jobs.save(done);
        assertThat(exportRetention.expire(Instant.now())).isGreaterThanOrEqualTo(1);
        assertThat(jobs.findById(id).orElseThrow().getStatus()).isEqualTo(ExportJobEntity.EXPIRED);
        mvc.perform(get("/v1/exports/" + id + "/download").header(HttpHeaders.AUTHORIZATION, agent))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.code").value("EXPORT_EXPIRED"));
        mvc.perform(get("/v1/exports/" + id + "/manifest").header(HttpHeaders.AUTHORIZATION, agent))
                .andExpect(status().isGone());
        assertThat(job(agent, id).get("status").asString()).isEqualTo("EXPIRED");
    }

    @Test
    @DisplayName("a job a restart left queued fails on start-up; an old directory without a job is swept")
    void sweeps() throws Exception {
        ExportJobEntity orphanJob = new ExportJobEntity("01JZZZZZZZZZZZZZZZZZZZZZZZ", ExportJobEntity.EXPORT,
                "geoparquet", Instant.now());
        orphanJob.setSpec(Map.of());
        jobs.save(orphanJob);
        exports.failInterrupted();
        ExportJobEntity failed = jobs.findById(orphanJob.getId()).orElseThrow();
        assertThat(failed.getStatus()).isEqualTo(ExportJobEntity.FAILED);
        assertThat(failed.getError()).contains("restart");

        Path stray = store.exportsRoot().resolve("01JSTRAYSTRAYSTRAYSTRAYSTR");
        Files.createDirectories(stray);
        Files.writeString(stray.resolve("x"), "x");
        Path fresh = store.exportsRoot().resolve("01JFRESHFRESHFRESHFRESHFRE");
        Files.createDirectories(fresh);
        Files.setLastModifiedTime(stray, FileTime.from(Instant.now().minus(Duration.ofHours(2))));
        exportRetention.sweep(store.exportsRoot(), Instant.now());
        assertThat(Files.exists(stray)).isFalse();
        assertThat(Files.exists(fresh)).as("younger than an hour").isTrue();
        ExportStore.deleteTree(fresh);
    }

    @Test
    @DisplayName("an unknown source is 400 EXPORT_SPEC_INVALID and includePrivate without admin is 403")
    void refusals() throws Exception {
        mvc.perform(post("/v1/exports").header(HttpHeaders.AUTHORIZATION, agent).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sources\":[\"no-such-source\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("EXPORT_SPEC_INVALID"));
        mvc.perform(post("/v1/exports").header(HttpHeaders.AUTHORIZATION, agent).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sources\":[\"" + SECRET + "\"],\"includePrivate\":true}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/v1/exports").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
