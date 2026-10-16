package ai.foresignal.source.adapters.satdump;

import ai.foresignal.source.InboxFiles;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * The DCP location table (prompt 107): DCS addresses to positions, loaded from the operator's
 * {@code dcs/dcp_locations.csv} under the inbox ({@code address,lat,lon,name,agency}) and cached in
 * memory. The file is re-read when its size or modification time changes; rows are also mirrored to the
 * {@code dcp_location} table. A row with an invalid address or position is skipped and counted.
 */
@Component
public class DcpLocationStore {

    private static final Logger log = LoggerFactory.getLogger(DcpLocationStore.class);

    /** The CSV path under the inbox. */
    public static final String CSV_PATH = "dcs/dcp_locations.csv";

    /**
     * One location.
     *
     * @param address the DCP address, normalized
     * @param lat the latitude, or null
     * @param lon the longitude, or null
     * @param name the platform name, or null
     * @param agency the operating agency, or null
     */
    public record DcpLocation(String address, Double lat, Double lon, String name, String agency) {}

    private final DcpLocationRepository repository;
    private final InboxFiles inbox;
    private final Clock clock;
    private volatile Map<String, DcpLocation> cache = Map.of();
    private volatile String signature;

    /** Creates the store. */
    public DcpLocationStore(DcpLocationRepository repository, InboxFiles inbox, Clock clock) {
        this.repository = repository;
        this.inbox = inbox;
        this.clock = clock;
    }

    /** The location of {@code address}, if known. */
    public Optional<DcpLocation> find(String address) {
        return Optional.ofNullable(cache.get(normalize(address)));
    }

    /** The number of loaded locations. */
    public int size() {
        return cache.size();
    }

    /**
     * Re-reads the CSV when it changed.
     *
     * @return true when the cache was replaced
     */
    public synchronized boolean load() {
        if (inbox == null) {
            return false;
        }
        Path file;
        try {
            file = inbox.requireInside(inbox.root().resolve(CSV_PATH));
        } catch (RuntimeException e) {
            return false;
        }
        if (!Files.isRegularFile(file)) {
            if (!cache.isEmpty()) {
                cache = Map.of();
                return true;
            }
            return false;
        }
        String current;
        try {
            current = Files.size(file) + ":" + Files.getLastModifiedTime(file).toMillis();
        } catch (IOException e) {
            return false;
        }
        if (current.equals(signature)) {
            return false;
        }
        List<DcpLocation> locations;
        try (InputStream in = inbox.open(file)) {
            locations = parse(in);
        } catch (IOException | RuntimeException e) {
            log.warn("DCP locations {} could not be read: {}", CSV_PATH, e.getMessage());
            return false;
        }
        Map<String, DcpLocation> map = new LinkedHashMap<>();
        Instant now = clock.instant();
        for (DcpLocation location : locations) {
            map.put(location.address(), location);
            upsert(location, now);
        }
        cache = Map.copyOf(map);
        signature = current;
        log.info("DCP locations loaded: {} rows from {}", map.size(), CSV_PATH);
        return true;
    }

    private void upsert(DcpLocation location, Instant now) {
        if (repository == null) {
            return;
        }
        try {
            DcpLocationEntity entity = repository.findById(location.address())
                    .orElseGet(() -> new DcpLocationEntity(location.address(), null, null, null, null, "csv", now));
            entity.setLat(location.lat());
            entity.setLon(location.lon());
            entity.setName(location.name());
            entity.setAgency(location.agency());
            entity.setSource("csv");
            entity.setUpdatedAt(now);
            repository.save(entity);
        } catch (RuntimeException e) {
            log.debug("DCP location {} not mirrored: {}", location.address(), e.getMessage());
        }
    }

    /** Parses the CSV rows, skipping the header and invalid rows. */
    public static List<DcpLocation> parse(InputStream in) {
        List<DcpLocation> out = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || line.stripLeading().startsWith("#")) {
                    continue;
                }
                String[] parts = line.split(",", -1);
                if (first) {
                    first = false;
                    if (parts.length > 0 && parts[0].strip().equalsIgnoreCase("address")) {
                        continue;
                    }
                }
                if (parts.length < 3) {
                    continue;
                }
                String address = normalize(parts[0]);
                if (address.isEmpty()) {
                    continue;
                }
                Double lat = number(parts.length > 1 ? parts[1] : null);
                Double lon = number(parts.length > 2 ? parts[2] : null);
                if (lat != null && (lat < -90 || lat > 90)) {
                    continue;
                }
                if (lon != null && (lon < -180 || lon > 180)) {
                    continue;
                }
                out.add(new DcpLocation(address, lat, lon, text(parts.length > 3 ? parts[3] : null),
                        text(parts.length > 4 ? parts[4] : null)));
            }
        } catch (IOException e) {
            return List.of();
        }
        return out;
    }

    /** Upper-cased hexadecimal (or decimal) without leading zeros. */
    public static String normalize(String address) {
        if (address == null) {
            return "";
        }
        String s = address.strip().toUpperCase(Locale.ROOT).replace("0X", "");
        int i = 0;
        while (i < s.length() - 1 && s.charAt(i) == '0') {
            i++;
        }
        return s.substring(i);
    }

    private static Double number(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            double v = Double.parseDouble(text.strip());
            return Double.isFinite(v) ? v : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String text(String value) {
        if (value == null) {
            return null;
        }
        String s = value.strip();
        return s.isEmpty() ? null : s;
    }
}
