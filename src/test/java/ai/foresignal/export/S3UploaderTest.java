package ai.foresignal.export;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.testsupport.net.TestOutbound;
import ai.foresignal.test.WireMockSupport;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The SigV4 S3 client of the cold archive (prompt 111) against a stubbed S3: signing, a single
 * {@code PutObject} with its ETag check, a multipart upload with part checks and the abort on a bad
 * part, and the outbound URL policy refusing a metadata address.
 */
class S3UploaderTest extends WireMockSupport {

    static final Clock CLOCK = Clock.fixed(Instant.parse("2026-03-01T12:00:00Z"), ZoneOffset.UTC);
    static final Map<String, String> SECRETS = Map.of("accessKey", "AKIDEXAMPLE", "secretKey", "s3cr3t");

    @TempDir Path dir;

    private static OutboundUrlPolicy policy() {
        return OutboundUrlPolicy.withoutOverrides(InetAddress::getAllByName);
    }

    private static ArchiveSettings.S3 s3() {
        return new ArchiveSettings.S3(baseUrl(), "bucket", "pre", "us-east-1", true);
    }

    private static String md5(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("MD5").digest(bytes));
    }

    private static String sha256(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }

    @Test
    @DisplayName("the SigV4 signing key matches AWS's published derivation example")
    void signingKeyVector() {
        byte[] key = S3Uploader.signingKey("wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY", "20120215", "us-east-1", "iam");
        assertThat(HexFormat.of().formatHex(key))
                .isEqualTo("f4780e2d9f65fa895f9c67b32ce1baf0b0d8a43505a000a1a9e090d414db404d");
        assertThat(S3Uploader.canonicalQuery(Map.of("uploadId", "a b", "partNumber", "2")))
                .isEqualTo("partNumber=2&uploadId=a%20b");
        assertThat(S3Uploader.encodePath("/bucket/a b/c~d*")).isEqualTo("/bucket/a%20b/c~d%2A");
    }

    @Test
    @DisplayName("a small file is one signed PutObject, verified by its MD5 ETag")
    void singlePut() throws Exception {
        byte[] body = "parquet bytes".getBytes(StandardCharsets.UTF_8);
        Path file = Files.write(dir.resolve("part.parquet"), body);
        stubFor(put(urlPathEqualTo("/bucket/pre/record_version/part-1-2.parquet"))
                .willReturn(aResponse().withStatus(200).withHeader("ETag", "\"" + md5(body) + "\"")));
        S3Uploader.Result result = new S3Uploader(policy(), TestOutbound.pinnedHttp(), CLOCK).upload(s3(), SECRETS,
                "record_version/part-1-2.parquet", file);
        assertThat(result.verified()).isTrue();
        assertThat(result.parts()).isEqualTo(1);
        assertThat(result.key()).isEqualTo("pre/record_version/part-1-2.parquet");
        verify(putRequestedFor(urlPathEqualTo("/bucket/pre/record_version/part-1-2.parquet"))
                .withHeader("x-amz-content-sha256", equalTo(sha256(body)))
                .withHeader("x-amz-date", equalTo("20260301T120000Z"))
                .withHeader("Content-MD5", equalTo(java.util.Base64.getEncoder()
                        .encodeToString(MessageDigest.getInstance("MD5").digest(body))))
                .withHeader("Authorization", matching("AWS4-HMAC-SHA256 Credential=AKIDEXAMPLE/20260301/us-east-1/s3/"
                        + "aws4_request, SignedHeaders=content-md5;host;x-amz-content-sha256;x-amz-date,"
                        + " Signature=[0-9a-f]{64}")));
    }

    @Test
    @DisplayName("a mismatched ETag leaves the upload unverified (the local copy is then kept)")
    void unverifiedPut() throws Exception {
        Path file = Files.writeString(dir.resolve("x.parquet"), "abc");
        stubFor(put(urlPathEqualTo("/bucket/pre/x.parquet"))
                .willReturn(aResponse().withStatus(200).withHeader("ETag", "\"deadbeef\"")));
        assertThat(new S3Uploader(policy(), TestOutbound.pinnedHttp(), CLOCK).upload(s3(), SECRETS, "x.parquet", file)
                .verified()).isFalse();
    }

    @Test
    @DisplayName("past the threshold the file goes up in parts and the result is checked against the MD5 of MD5s")
    void multipart() throws Exception {
        byte[] body = new byte[25];
        Arrays.fill(body, (byte) 7);
        body[24] = 9;
        Path file = Files.write(dir.resolve("big.parquet"), body);
        String path = "/bucket/pre/big.parquet";
        stubFor(post(urlPathEqualTo(path)).withQueryParam("uploads", equalTo(""))
                .willReturn(aResponse().withStatus(200)
                        .withBody("<InitiateMultipartUploadResult><UploadId>U1</UploadId></InitiateMultipartUploadResult>")));
        MessageDigest md5OfMd5s = MessageDigest.getInstance("MD5");
        for (int part = 1; part <= 3; part++) {
            byte[] slice = Arrays.copyOfRange(body, (part - 1) * 10, Math.min(25, part * 10));
            md5OfMd5s.update(MessageDigest.getInstance("MD5").digest(slice));
            stubFor(put(urlPathEqualTo(path)).withQueryParam("partNumber", equalTo(Integer.toString(part)))
                    .withQueryParam("uploadId", equalTo("U1"))
                    .willReturn(aResponse().withStatus(200).withHeader("ETag", "\"" + md5(slice) + "\"")));
        }
        String expected = HexFormat.of().formatHex(md5OfMd5s.digest()) + "-3";
        stubFor(post(urlPathEqualTo(path)).withQueryParam("uploadId", equalTo("U1"))
                .willReturn(aResponse().withStatus(200).withBody("<CompleteMultipartUploadResult><ETag>&quot;" + expected
                        + "&quot;</ETag></CompleteMultipartUploadResult>")));
        S3Uploader.Result result = new S3Uploader(policy(), TestOutbound.pinnedHttp(), CLOCK, 10, 10).upload(s3(), SECRETS,
                "big.parquet", file);
        assertThat(result.parts()).isEqualTo(3);
        assertThat(result.etag()).isEqualTo(expected);
        assertThat(result.verified()).isTrue();
    }

    @Test
    @DisplayName("a part whose ETag is not its MD5 aborts the multipart upload")
    void multipartAbort() throws Exception {
        Path file = Files.write(dir.resolve("big.parquet"), new byte[25]);
        String path = "/bucket/pre/big.parquet";
        stubFor(post(urlPathEqualTo(path)).withQueryParam("uploads", equalTo(""))
                .willReturn(aResponse().withStatus(200)
                        .withBody("<InitiateMultipartUploadResult><UploadId>U2</UploadId></InitiateMultipartUploadResult>")));
        stubFor(put(urlPathEqualTo(path)).withQueryParam("uploadId", equalTo("U2"))
                .willReturn(aResponse().withStatus(200).withHeader("ETag", "\"0000\"")));
        stubFor(delete(urlPathEqualTo(path)).withQueryParam("uploadId", equalTo("U2"))
                .willReturn(aResponse().withStatus(204)));
        assertThatThrownBy(() -> new S3Uploader(policy(), TestOutbound.pinnedHttp(), CLOCK, 10, 10)
                .upload(s3(), SECRETS, "big.parquet", file))
                .isInstanceOf(java.io.UncheckedIOException.class);
        verify(deleteRequestedFor(urlPathEqualTo(path)).withQueryParam("uploadId", equalTo("U2")));
    }

    @Test
    @DisplayName("a server error fails the upload; missing secrets and a metadata endpoint are refused")
    void refusals() throws Exception {
        Path file = Files.writeString(dir.resolve("y.parquet"), "abc");
        stubFor(put(urlPathEqualTo("/bucket/pre/y.parquet")).willReturn(aResponse().withStatus(403).withBody("denied")));
        S3Uploader uploader = new S3Uploader(policy(), TestOutbound.pinnedHttp(), CLOCK);
        assertThatThrownBy(() -> uploader.upload(s3(), SECRETS, "y.parquet", file))
                .isInstanceOf(java.io.UncheckedIOException.class).hasMessageContaining("upload");
        assertThatThrownBy(() -> uploader.upload(s3(), Map.of(), "y.parquet", file))
                .isInstanceOf(IllegalStateException.class);
        ArchiveSettings.S3 metadata = new ArchiveSettings.S3("http://169.254.169.254", "bucket", "", null, true);
        assertThatThrownBy(() -> uploader.upload(metadata, SECRETS, "y.parquet", file))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("not allowed");
        S3Uploader loopback = new S3Uploader(OutboundUrlPolicy.withoutOverrides(
                host -> new InetAddress[] {InetAddress.getLoopbackAddress()}),
                TestOutbound.pinnedHttp(), CLOCK);
        // Virtual-hosted style needs a DNS name: "bkt.127.0.0.1" is not a valid host, so the endpoint here is a name
        // the stub resolver maps to loopback.
        S3Uploader.Target hosted = loopback.target(new ArchiveSettings.S3("http://minio.test:9", "bkt", "/p/", null,
                false), "k");
        assertThat(hosted.host()).isEqualTo("bkt.minio.test:9");
        assertThat(hosted.path()).isEqualTo("/p/k");
    }
}
