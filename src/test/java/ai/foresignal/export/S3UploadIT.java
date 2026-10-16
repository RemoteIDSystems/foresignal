package ai.foresignal.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.source.spi.HistoryTier;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The cold archive's S3 client against a real S3-compatible server in a container (prompt 111):
 * {@code PutObject}, a multipart upload in 5 MiB parts, the ETag checks, the object's bytes read back,
 * and the {@code keepLocal=false} cleanup of the archive service after a verified upload.
 *
 * <p>The server is Adobe's S3Mock: MinIO's community images are no longer anonymously pullable from
 * Docker Hub (the registry answered 401 for {@code minio/minio} on 2026-10-16), while
 * {@code adobe/s3mock:4.8.0} is. S3Mock does not check SigV4 signatures; the signing is covered by
 * {@code S3UploaderTest}'s published vector and header checks.
 */
class S3UploadIT {

    static final DockerImageName S3MOCK = DockerImageName.parse("adobe/s3mock:4.8.0");
    static final JsonMapper JSON = JsonMapper.builder().build();
    static final Map<String, String> SECRETS = Map.of("accessKey", "AK", "secretKey", "SK");

    static GenericContainer<?> s3mock;
    static String endpoint;

    @TempDir Path dir;

    @BeforeAll
    @SuppressWarnings("resource")
    static void start() throws Exception {
        s3mock = new GenericContainer<>(S3MOCK).withExposedPorts(9090).waitingFor(Wait.forListeningPort());
        s3mock.start();
        endpoint = "http://127.0.0.1:" + s3mock.getMappedPort(9090);
        HttpResponse<String> created = HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create(endpoint
                + "/archive")).PUT(HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString());
        assertThat(created.statusCode() / 100).as(created.body()).isEqualTo(2);
    }

    @AfterAll
    static void stop() {
        if (s3mock != null) {
            s3mock.stop();
        }
    }

    private static OutboundUrlPolicy policy() {
        return OutboundUrlPolicy.withoutOverrides(InetAddress::getAllByName);
    }

    private static ArchiveSettings.S3 s3() {
        return new ArchiveSettings.S3(endpoint, "archive", "it", "us-east-1", true);
    }

    private static byte[] get(String key) throws Exception {
        return HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create(endpoint + "/archive/" + key)).GET()
                .build(), HttpResponse.BodyHandlers.ofByteArray()).body();
    }

    @Test
    @DisplayName("a small file is one verified PutObject whose bytes read back")
    void put() throws Exception {
        byte[] body = "a small archive part".getBytes(StandardCharsets.UTF_8);
        Path file = Files.write(dir.resolve("small.parquet"), body);
        S3Uploader.Result result = new S3Uploader(policy(), Clock.systemUTC()).upload(s3(), SECRETS, "p/small.parquet",
                file);
        assertThat(result.verified()).isTrue();
        assertThat(result.parts()).isEqualTo(1);
        assertThat(get("it/p/small.parquet")).isEqualTo(body);
    }

    @Test
    @DisplayName("a file past the threshold goes up in parts and the multipart ETag verifies")
    void multipart() throws Exception {
        byte[] body = new byte[11 * 1024 * 1024 + 123];
        new Random(7).nextBytes(body);
        Path file = Files.write(dir.resolve("big.parquet"), body);
        long fiveMiB = 5L * 1024 * 1024;
        S3Uploader.Result result = new S3Uploader(policy(), Clock.systemUTC(), fiveMiB, fiveMiB).upload(s3(), SECRETS,
                "p/big.parquet", file);
        assertThat(result.parts()).isEqualTo(3);
        assertThat(result.verified()).isTrue();
        assertThat(get("it/p/big.parquet")).isEqualTo(body);
    }

    @Test
    @DisplayName("with keepLocal=false the archive deletes the local part after a verified upload and records it")
    void keepLocalFalseCleanup() throws Exception {
        ArchiveService archive = new ArchiveService(mock(ai.foresignal.store.query.ReadQueryRunner.class),
                mock(ExportStore.class), mock(ArchiveSettingsService.class), mock(ArchiveWatermarkRepository.class),
                new S3Uploader(policy(), Clock.systemUTC()), ExportProperties.defaults(), JSON, Clock.systemUTC());
        String partition = ArchiveService.partitionPath(HistoryTier.STANDARD, "src", LocalDate.parse("2026-03-01"));
        Path partitionDir = Files.createDirectories(dir.resolve(partition));
        Path file = Files.write(partitionDir.resolve("part-1-2.parquet"), "parquet".getBytes(StandardCharsets.UTF_8));
        Files.writeString(partitionDir.resolve(ArchiveService.MANIFEST), "{\"rows\":1,\"files\":[{\"name\":"
                + "\"part-1-2.parquet\",\"fromSeq\":1,\"toSeq\":2,\"rows\":1,\"bytes\":7,\"sha256\":\"x\",\"local\":true,"
                + "\"uploadedAt\":null,\"s3Key\":null}],\"license\":null}");
        ArchiveSettings settings = new ArchiveSettings(true, java.util.List.of(), dir.toString(), "s3", s3(), null, null,
                null, null, false);
        ArchiveService.ArchivedFile archived = new ArchiveService.ArchivedFile("part-1-2.parquet", 1, 2, 1, 7, "x",
                true, null, null);
        archive.upload(settings, SECRETS, partitionDir, partition + "/part-1-2.parquet", file, archived);
        assertThat(Files.exists(file)).isFalse();
        JsonNode manifest = JSON.readTree(Files.readAllBytes(partitionDir.resolve(ArchiveService.MANIFEST)));
        JsonNode entry = manifest.get("files").get(0);
        assertThat(entry.get("local").asBoolean()).isFalse();
        assertThat(entry.get("s3Key").asString()).isEqualTo("it/" + partition + "/part-1-2.parquet");
        assertThat(entry.get("uploadedAt").isNull()).isFalse();
        assertThat(get("it/" + partition + "/part-1-2.parquet")).isEqualTo("parquet".getBytes(StandardCharsets.UTF_8));
        archive.shutdown();
    }
}
