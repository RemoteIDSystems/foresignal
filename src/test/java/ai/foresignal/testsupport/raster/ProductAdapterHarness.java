package ai.foresignal.testsupport.raster;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.DecodedBands;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FrameInput;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.RasterSink;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.ValidatedParams;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hand-built plumbing for the prompt 046 product adapter tests: a {@link FetchContext} over a real
 * {@link SourceHttp}, an in-memory adapter state, a {@link RasterSink} that records (and optionally
 * decodes) what an adapter commits, and S3 ListObjectsV2 bodies.
 */
public final class ProductAdapterHarness {

    private ProductAdapterHarness() {}

    /** In-memory adapter state. */
    public static final class MapState implements AdapterStateStore {

        private final Map<String, String> values = new ConcurrentHashMap<>();

        @Override
        public Optional<String> get(String key) {
            return Optional.ofNullable(values.get(key));
        }

        @Override
        public void put(String key, String value) {
            values.put(key, value);
        }

        @Override
        public void remove(String key) {
            values.remove(key);
        }

        /** Everything stored. */
        public Map<String, String> asMap() {
            return Map.copyOf(values);
        }
    }

    /** A fetch context over {@code http} and {@code sink}. */
    public record Context(SourceDescriptor descriptor, SourceHttp http, Clock clock, MapState state, RasterSink sink)
            implements FetchContext {

        @Override
        public String sourceId() {
            return descriptor.id();
        }

        @Override
        public ValidatedParams params() {
            return ValidatedParams.empty();
        }

        @Override
        public Map<String, String> credentials() {
            return Map.of();
        }

        @Override
        public Optional<Instant> lastSuccessAt() {
            return Optional.empty();
        }

        @Override
        public RasterSink rasters() {
            return sink;
        }
    }

    /** A fetch context with params (prompt 051: the model sources' {@code pathTemplate}, {@code steps}, …). */
    public record ParamContext(SourceDescriptor descriptor, SourceHttp http, Clock clock, MapState state,
            RasterSink sink, ValidatedParams params) implements FetchContext {

        @Override
        public String sourceId() {
            return descriptor.id();
        }

        @Override
        public Map<String, String> credentials() {
            return Map.of();
        }

        @Override
        public Optional<Instant> lastSuccessAt() {
            return Optional.empty();
        }

        @Override
        public RasterSink rasters() {
            return sink;
        }
    }

    /** A committed frame, with its bands when the sink decoded it. */
    public record Committed(FrameInput frame, DecodedBands bands) {}

    /**
     * A sink that records commits. With {@code decode} it runs the frame's decoder on its temporary
     * file, as the pipeline would on the decode pool; either way it deletes the file afterwards.
     */
    public static final class RecordingSink implements RasterSink {

        private final List<Committed> committed = new ArrayList<>();
        private final List<String> completedRuns = new ArrayList<>();
        private final Map<String, String> formatChanges = new LinkedHashMap<>();
        private final Set<String> disabled = new HashSet<>();
        private boolean decode;
        private boolean probing;

        /** Decodes each committed frame. */
        public RecordingSink decoding() {
            this.decode = true;
            return this;
        }

        /** Acts as a probe's sink. */
        public RecordingSink asProbe() {
            this.probing = true;
            return this;
        }

        /** Disables products. */
        public RecordingSink disable(String... productIds) {
            disabled.addAll(List.of(productIds));
            return this;
        }

        @Override
        public synchronized NormalizedRecord commit(FrameInput frame) throws AdapterException {
            DecodedBands bands = frame.bands();
            try {
                if (decode && !probing && bands == null) {
                    bands = frame.decoder().decode(frame.tempFile());
                }
            } catch (IOException e) {
                throw new AdapterException("decode failed: " + e.getMessage(), e);
            } finally {
                if (frame.tempFile() != null) {
                    try {
                        Files.deleteIfExists(frame.tempFile());
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            }
            committed.add(new Committed(frame, bands));
            return NormalizedRecord.builder(frame.productId(), ProductSpec.RECORD_KIND)
                    .observedAt(frame.validTime())
                    .put("productId", frame.productId())
                    .put("time", frame.validTime().toString())
                    .put("probe", probing ? Boolean.TRUE : null)
                    .build();
        }

        /**
         * Records the completion (prompt 051) and returns one run record; MRMS and GOES never call it.
         */
        @Override
        public synchronized List<NormalizedRecord> completeRun(String productId, Instant referenceTime) {
            completedRuns.add(productId + "@" + referenceTime);
            return List.of(NormalizedRecord.builder(productId, ProductSpec.RECORD_KIND)
                    .observedAt(referenceTime)
                    .put("productId", productId)
                    .put("run", referenceTime.toString())
                    .build());
        }

        /** The completed runs, as {@code productId@referenceTime}, in order. */
        public synchronized List<String> completedRuns() {
            return List.copyOf(completedRuns);
        }

        @Override
        public synchronized void reportFormatChange(String productId, String reason) {
            formatChanges.put(productId, reason);
        }

        @Override
        public boolean probing() {
            return probing;
        }

        @Override
        public boolean disabled(String productId) {
            return disabled.contains(productId);
        }

        /** Every commit, in order. */
        public synchronized List<Committed> committed() {
            return List.copyOf(committed);
        }

        /** The commits of {@code productId}. */
        public synchronized List<Committed> committed(String productId) {
            return committed.stream().filter(c -> c.frame().productId().equals(productId)).toList();
        }

        /** Reported format changes by product. */
        public synchronized Map<String, String> formatChanges() {
            return Map.copyOf(formatChanges);
        }
    }

    /**
     * An S3 ListObjectsV2 body.
     *
     * @param objects key to size, in key order
     * @param truncated whether more pages follow
     */
    public static String listing(String bucket, String prefix, Map<String, Long> objects, boolean truncated) {
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                .append("<ListBucketResult xmlns=\"http://s3.amazonaws.com/doc/2006-03-01/\">")
                .append("<Name>").append(bucket).append("</Name><Prefix>").append(prefix).append("</Prefix>")
                .append("<KeyCount>").append(objects.size()).append("</KeyCount><MaxKeys>1000</MaxKeys>")
                .append("<IsTruncated>").append(truncated).append("</IsTruncated>");
        objects.forEach((key, size) -> xml.append("<Contents><Key>").append(key).append("</Key>")
                .append("<LastModified>2026-10-12T12:01:00.000Z</LastModified>")
                .append("<ETag>&quot;").append(Integer.toHexString(key.hashCode())).append("&quot;</ETag>")
                .append("<Size>").append(size).append("</Size><StorageClass>STANDARD</StorageClass></Contents>"));
        return xml.append("</ListBucketResult>").toString();
    }
}
