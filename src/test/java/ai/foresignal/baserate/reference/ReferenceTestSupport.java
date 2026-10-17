package ai.foresignal.baserate.reference;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Shared helpers of the reference-import unit tests (prompt 121): an in-memory writer, a plain fetcher, fixtures. */
final class ReferenceTestSupport {

    private ReferenceTestSupport() {}

    /** A {@link ReferenceHistoryWriter} that keeps rows in memory, replacing chunks exactly as the SQL one does. */
    static final class MemoryWriter extends ReferenceHistoryWriter {

        /** The rows by {@code family|region|week|source}. */
        final Map<String, Object[]> rows = new TreeMap<>();

        /** The chunks replaced, in order. */
        final List<String> chunks = new ArrayList<>();

        MemoryWriter() {
            super(null, null);
        }

        @Override
        public int replace(String source, String importId, LocalDate from, LocalDate to, Collection<String> countries,
                List<WeeklyCounts.Row> in) {
            chunks.add(source + ":" + from + ":" + to);
            rows.entrySet().removeIf(e -> {
                Object[] r = e.getValue();
                LocalDate week = (LocalDate) r[2];
                String region = (String) r[1];
                return source.equals(r[3]) && !week.isBefore(from) && week.isBefore(to)
                        && (countries == null || countries.isEmpty() || countries.contains(region.substring(0, 2)));
            });
            int n = 0;
            for (WeeklyCounts.Row row : in) {
                if (row.weekStart().isBefore(from) || !row.weekStart().isBefore(to)) {
                    continue;
                }
                rows.put(row.family() + "|" + row.regionCode() + "|" + row.weekStart() + "|" + source, new Object[] {
                    row.family(), row.regionCode(), row.weekStart(), source, row.count(), row.fatalities(),
                    ReferenceFamilies.LICENSES.get(source), ReferenceFamilies.EGRESS.get(source), importId});
                n++;
            }
            return n;
        }

        @Override
        public int deleteImport(String importId) {
            int before = rows.size();
            rows.values().removeIf(r -> importId.equals(r[8]));
            return before - rows.size();
        }

        /** The count of one row, or null. */
        Long count(String family, String region, LocalDate week, String source) {
            Object[] r = rows.get(family + "|" + region + "|" + week + "|" + source);
            return r == null ? null : (Long) r[4];
        }

        /** One row, or null. */
        Object[] row(String family, String region, LocalDate week, String source) {
            return rows.get(family + "|" + region + "|" + week + "|" + source);
        }
    }

    /** A plain HTTP fetcher (WireMock tests). */
    static ReferenceHttp.Fetcher fetcher() {
        HttpClient client = HttpClient.newHttpClient();
        return (uri, headers) -> {
            HttpRequest.Builder b = HttpRequest.newBuilder(uri).GET();
            headers.forEach(b::header);
            HttpResponse<byte[]> response = client.send(b.build(), HttpResponse.BodyHandlers.ofByteArray());
            return new ReferenceHttp.Response(response.statusCode(), response.body());
        };
    }

    /** A fixture's bytes. */
    static byte[] fixture(String name) {
        try (InputStream in = ReferenceTestSupport.class.getResourceAsStream("/fixtures/baserate/" + name)) {
            if (in == null) {
                throw new IllegalStateException("missing fixture " + name);
            }
            return in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** A new import row with normalized parameters. */
    static ReferenceImportEntity entity(String dataset, Map<String, Object> params) {
        ReferenceImportEntity e = new ReferenceImportEntity("01JA8Q3V7S2K9W4T6Y1B5N0M3C", dataset, "key", "test",
                Instant.parse("2026-10-17T00:00:00Z"));
        e.setParams(params);
        return e;
    }
}
