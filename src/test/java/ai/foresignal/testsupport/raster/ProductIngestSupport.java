package ai.foresignal.testsupport.raster;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import ai.foresignal.collect.UpstreamClient;
import ai.foresignal.raster.render.WebMercator;
import ai.foresignal.source.spi.SourceDescriptor;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Shared plumbing of the prompt 046 ingest ITs ({@code MrmsIngestIT}, {@code GoesIngestIT}): the real
 * product adapters run against a stubbed S3 through the context's own {@link UpstreamClient} (pointed
 * at WireMock for the test) and commit through the real raster pipeline, in 042's shared raster
 * context.
 */
public abstract class ProductIngestSupport extends RasterApiSupport {

    /** The stubbed S3 endpoint. */
    @RegisterExtension
    protected static final WireMockExtension S3 = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().gzipDisabled(true))
            .failOnUnmatchedRequests(true)
            .resetOnEachTest(true)
            .build();

    @Autowired protected UpstreamClient upstream;

    /** Points the context's upstream client at the stub. */
    @BeforeEach
    void pointAtStub() {
        upstream.setS3Endpoint(S3.baseUrl());
    }

    /** Restores the real S3 endpoint for the rest of the shared context. */
    @AfterEach
    void restoreEndpoint() {
        upstream.setS3Endpoint("");
    }

    /** A fetch context for {@code descriptor} with the real pipeline as {@code ctx.rasters()}. */
    protected ProductAdapterHarness.Context context(SourceDescriptor descriptor) {
        return new ProductAdapterHarness.Context(descriptor, upstream.forSource(descriptor.id(), descriptor), clock,
                new ProductAdapterHarness.MapState(), pipeline.forSource(descriptor, false));
    }

    /** Lists nothing for any listing of {@code bucket} not stubbed more specifically. */
    protected static void stubAnyListing(String bucket) {
        S3.stubFor(WireMock.get(WireMock.urlPathEqualTo("/" + bucket + "/")).atPriority(10)
                .willReturn(WireMock.aResponse().withHeader("Content-Type", "application/xml")
                        .withBody(ProductAdapterHarness.listing(bucket, "", Map.of(), false))));
    }

    /** Lists {@code objects} for listings of {@code bucket} under {@code prefix}. */
    protected static void stubListing(String bucket, String prefix, Map<String, Long> objects) {
        S3.stubFor(WireMock.get(WireMock.urlPathEqualTo("/" + bucket + "/"))
                .withQueryParam("prefix", WireMock.equalTo(prefix))
                .atPriority(1)
                .willReturn(WireMock.aResponse().withHeader("Content-Type", "application/xml")
                        .withBody(ProductAdapterHarness.listing(bucket, prefix, objects, false))));
    }

    /** Serves {@code body} for {@code key}. */
    protected static void stubObject(String bucket, String key, byte[] body) {
        S3.stubFor(WireMock.get(WireMock.urlPathEqualTo("/" + bucket + "/" + key))
                .willReturn(WireMock.aResponse().withBody(body)));
    }

    /** The tile {@code {x, y}} holding {@code lon, lat} at zoom {@code z}. */
    protected static int[] tile(int z, double lon, double lat) {
        int n = 1 << z;
        int x = (int) Math.floor((lon + 180.0) / 360.0 * n);
        double rad = Math.toRadians(lat);
        int y = (int) Math.floor((1.0 - Math.log(Math.tan(rad) + 1.0 / Math.cos(rad)) / Math.PI) / 2.0 * n);
        return new int[] {x, y};
    }

    /** The pixel of {@code lon, lat} inside tile {@code z/x/y} of {@code size} pixels. */
    protected static int[] pixel(int z, int x, int y, int size, double lon, double lat) {
        double[] m = WebMercator.tileMeters(z, x, y);
        double step = (m[2] - m[0]) / size;
        return new int[] {(int) ((WebMercator.x(lon) - m[0]) / step), (int) ((m[3] - WebMercator.y(lat)) / step)};
    }
}
