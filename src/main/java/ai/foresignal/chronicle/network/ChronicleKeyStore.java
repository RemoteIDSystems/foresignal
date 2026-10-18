package ai.foresignal.chronicle.network;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * The native SQL of the chronicle network's bulk tables (prompt 134): {@code chronicle_key},
 * {@code chronicle_fingerprint}, {@code chronicle_sketch_state} and {@code chronicle_link_disclosure}. Hibernate
 * defines these tables; every read and write goes through here.
 */
@Component
public class ChronicleKeyStore {

    private final JdbcTemplate jdbc;

    /** Creates the store. */
    public ChronicleKeyStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ------------------------------------------------------------------------- keys

    /**
     * One keyed key to store.
     *
     * @param keyType the key type
     * @param hkey the keyed hash
     * @param firstSeen when the chronicle first saw it
     * @param keyText the key text (instance salt only), or null
     * @param ref the object it came from (instance salt only), or null
     */
    public record KeyRow(String keyType, long hkey, Instant firstSeen, String keyText, String ref) {}

    /** Stores keys (an existing key keeps the earlier first-seen time). */
    public void insertKeys(String chronicleId, String saltId, Collection<KeyRow> rows) {
        if (rows.isEmpty()) {
            return;
        }
        List<Object[]> args = new ArrayList<>(rows.size());
        for (KeyRow r : rows) {
            args.add(new Object[] {chronicleId, saltId, r.keyType(), r.hkey(), Timestamp.from(r.firstSeen()),
                    cut(r.keyText(), 700), cut(r.ref(), 200)});
        }
        jdbc.batchUpdate("INSERT INTO chronicle_key (chronicle_id, salt_id, key_type, hkey, first_seen, key_text, ref)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT (chronicle_id, salt_id, key_type, hkey) DO UPDATE SET"
                + " first_seen = LEAST(chronicle_key.first_seen, EXCLUDED.first_seen)", args);
    }

    /** The exact key count per key type of a chronicle under a salt. */
    public Map<String, Long> sizes(String chronicleId, String saltId) {
        Map<String, Long> out = new LinkedHashMap<>();
        jdbc.query("SELECT key_type, count(*) FROM chronicle_key WHERE chronicle_id = ? AND salt_id = ?"
                + " GROUP BY key_type", rs -> {
                    out.put(rs.getString(1), rs.getLong(2));
                }, chronicleId, saltId);
        return out;
    }

    /** The exact number of shared keys per key type of two local chronicles under a salt. */
    public Map<String, Long> shared(String a, String b, String saltId) {
        Map<String, Long> out = new LinkedHashMap<>();
        jdbc.query("SELECT x.key_type, count(*) FROM chronicle_key x JOIN chronicle_key y ON y.chronicle_id = ?"
                + " AND y.salt_id = x.salt_id AND y.key_type = x.key_type AND y.hkey = x.hkey"
                + " WHERE x.chronicle_id = ? AND x.salt_id = ? GROUP BY x.key_type", rs -> {
                    out.put(rs.getString(1), rs.getLong(2));
                }, b, a, saltId);
        return out;
    }

    /**
     * How many shared keys each chronicle saw clearly first (more than an hour before the other).
     *
     * @return {@code [a first, b first]}
     */
    public long[] firstSeen(String a, String b, String saltId) {
        long[] out = new long[2];
        jdbc.query("SELECT coalesce(sum(CASE WHEN x.first_seen < y.first_seen - interval '1 hour' THEN 1 ELSE 0 END),"
                + " 0), coalesce(sum(CASE WHEN y.first_seen < x.first_seen - interval '1 hour' THEN 1 ELSE 0 END), 0)"
                + " FROM chronicle_key x JOIN chronicle_key y ON y.chronicle_id = ? AND y.salt_id = x.salt_id"
                + " AND y.key_type = x.key_type AND y.hkey = x.hkey WHERE x.chronicle_id = ? AND x.salt_id = ?", rs -> {
                    out[0] = rs.getLong(1);
                    out[1] = rs.getLong(2);
                }, b, a, saltId);
        return out;
    }

    /**
     * The objects behind the keys two local chronicles share, of one key type (instance salt rows only), earliest
     * first.
     *
     * @param limit the most to return
     */
    public List<String> sharedRefs(String a, String b, String saltId, String keyType, int limit) {
        return jdbc.queryForList("SELECT x.ref FROM chronicle_key x JOIN chronicle_key y ON y.chronicle_id = ?"
                + " AND y.salt_id = x.salt_id AND y.key_type = x.key_type AND y.hkey = x.hkey"
                + " WHERE x.chronicle_id = ? AND x.salt_id = ? AND x.key_type = ? AND x.ref IS NOT NULL"
                + " GROUP BY x.ref ORDER BY min(x.first_seen), x.ref LIMIT ?", String.class, b, a, saltId, keyType,
                limit);
    }

    /**
     * One deduplicated evidence key held by some of a set of chronicles.
     *
     * @param hkey the keyed hash
     * @param keyText the key text
     * @param ref the canonical event
     * @param at the earliest first-seen time among the holders
     * @param heldBy the chronicles holding it
     */
    public record HeldKey(long hkey, String keyText, String ref, Instant at, List<String> heldBy) {}

    /**
     * The evidence keys of a set of chronicles, one row per key (deduplicated by global evidence key) with the
     * chronicles that hold it, earliest first.
     *
     * @param chronicleIds the chronicles
     * @param saltId the instance salt
     * @param from only keys first seen at or after this, or null
     * @param to only keys first seen before this, or null
     * @param limit the most rows
     */
    public List<HeldKey> heldEvidence(Collection<String> chronicleIds, String saltId, Instant from, Instant to,
            int limit) {
        if (chronicleIds.isEmpty()) {
            return List.of();
        }
        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT hkey, min(key_text), min(ref), min(first_seen),"
                + " array_agg(DISTINCT chronicle_id ORDER BY chronicle_id) FROM chronicle_key"
                + " WHERE chronicle_id = ANY(?) AND salt_id = ? AND key_type = ?");
        args.add(chronicleIds.toArray(String[]::new));
        args.add(saltId);
        args.add(NetworkVocabulary.EVIDENCE);
        sql.append(" GROUP BY hkey");
        List<String> having = new ArrayList<>();
        if (from != null) {
            having.add("min(first_seen) >= ?");
            args.add(Timestamp.from(from));
        }
        if (to != null) {
            having.add("min(first_seen) < ?");
            args.add(Timestamp.from(to));
        }
        if (!having.isEmpty()) {
            sql.append(" HAVING ").append(String.join(" AND ", having));
        }
        sql.append(" ORDER BY min(first_seen), hkey LIMIT ?");
        args.add(limit);
        return jdbc.query(sql.toString(), (rs, i) -> {
            String[] held = (String[]) rs.getArray(5).getArray();
            return new HeldKey(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getTimestamp(4).toInstant(),
                    List.of(held));
        }, args.toArray());
    }

    // ------------------------------------------------------------------------- fingerprints

    /**
     * A stored fingerprint.
     *
     * @param chronicleId the chronicle
     * @param keyType the key type
     * @param sketch the signature
     * @param size the exact set size
     */
    public record Fingerprint(String chronicleId, String keyType, MinHashSketch sketch, long size) {}

    /** The fingerprint of one chronicle and key type under a salt. */
    public Optional<Fingerprint> fingerprint(String chronicleId, String keyType, String saltId) {
        List<Fingerprint> rows = jdbc.query("SELECT chronicle_id, key_type, minhash, size FROM chronicle_fingerprint"
                + " WHERE chronicle_id = ? AND key_type = ? AND salt_id = ?", (rs, i) -> new Fingerprint(
                        rs.getString(1), rs.getString(2), MinHashSketch.fromBytes(rs.getBytes(3)), rs.getLong(4)),
                chronicleId, keyType, saltId);
        return rows.stream().findFirst();
    }

    /** Every fingerprint under a salt, by chronicle and key type. */
    public Map<String, Map<String, Fingerprint>> fingerprints(String saltId) {
        Map<String, Map<String, Fingerprint>> out = new LinkedHashMap<>();
        jdbc.query("SELECT chronicle_id, key_type, minhash, size FROM chronicle_fingerprint WHERE salt_id = ?"
                + " ORDER BY chronicle_id, key_type", rs -> {
                    Fingerprint f = new Fingerprint(rs.getString(1), rs.getString(2),
                            MinHashSketch.fromBytes(rs.getBytes(3)), rs.getLong(4));
                    out.computeIfAbsent(f.chronicleId(), k -> new LinkedHashMap<>()).put(f.keyType(), f);
                }, saltId);
        return out;
    }

    /** Stores a fingerprint. */
    public void upsertFingerprint(String chronicleId, String keyType, String saltId, MinHashSketch sketch, long size,
            Instant now) {
        jdbc.update("INSERT INTO chronicle_fingerprint (chronicle_id, key_type, salt_id, minhash, size, updated_at)"
                + " VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT (chronicle_id, key_type, salt_id) DO UPDATE SET"
                + " minhash = EXCLUDED.minhash, size = EXCLUDED.size, updated_at = EXCLUDED.updated_at",
                chronicleId, keyType, saltId, sketch.toBytes(), size, Timestamp.from(now));
    }

    // ------------------------------------------------------------------------- state

    /**
     * The sketching state of a chronicle under a salt.
     *
     * @param chronicleId the chronicle
     * @param saltId the salt
     * @param keySeq the last seq turned into keys
     * @param totalKeys the keys, all types together
     * @param discoveredKeys the total at the last discovery
     * @param discoveryDue whether discovery is due
     * @param sketchId the random sketch id under a network salt, or null
     * @param sentAt when it was last sent, or null
     * @param withdrawnAt when it was withdrawn, or null
     */
    public record State(String chronicleId, String saltId, long keySeq, long totalKeys, long discoveredKeys,
            boolean discoveryDue, String sketchId, Instant sentAt, Instant withdrawnAt) {}

    private static final String STATE_COLUMNS = "chronicle_id, salt_id, key_seq, total_keys, discovered_keys,"
            + " discovery_due, sketch_id, sent_at, withdrawn_at";

    private static State state(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new State(rs.getString(1), rs.getString(2), rs.getLong(3), rs.getLong(4), rs.getLong(5),
                rs.getBoolean(6), rs.getString(7), instant(rs.getTimestamp(8)), instant(rs.getTimestamp(9)));
    }

    /** The state of a chronicle under a salt, created (with {@code sketchId}) when missing. */
    public State state(String chronicleId, String saltId, String sketchId) {
        jdbc.update("INSERT INTO chronicle_sketch_state (chronicle_id, salt_id, key_seq, total_keys, discovered_keys,"
                + " discovery_due, sketch_id) VALUES (?, ?, 0, 0, 0, false, ?) ON CONFLICT (chronicle_id, salt_id)"
                + " DO NOTHING", chronicleId, saltId, sketchId);
        return jdbc.query("SELECT " + STATE_COLUMNS + " FROM chronicle_sketch_state WHERE chronicle_id = ?"
                + " AND salt_id = ?", (rs, i) -> state(rs), chronicleId, saltId).get(0);
    }

    /** Every state under a salt. */
    public List<State> states(String saltId) {
        return jdbc.query("SELECT " + STATE_COLUMNS + " FROM chronicle_sketch_state WHERE salt_id = ?"
                + " ORDER BY chronicle_id", (rs, i) -> state(rs), saltId);
    }

    /** The state of the sketch a peer knows as {@code sketchId}, if it is ours. */
    public Optional<State> stateBySketchId(String sketchId) {
        return jdbc.query("SELECT " + STATE_COLUMNS + " FROM chronicle_sketch_state WHERE sketch_id = ?",
                (rs, i) -> state(rs), sketchId).stream().findFirst();
    }

    /** Records a sketching pass. */
    public void sketched(String chronicleId, String saltId, long keySeq, long totalKeys, boolean due, Instant now) {
        jdbc.update("UPDATE chronicle_sketch_state SET key_seq = ?, total_keys = ?, discovery_due = discovery_due OR ?,"
                + " sketched_at = ? WHERE chronicle_id = ? AND salt_id = ?", keySeq, totalKeys, due,
                Timestamp.from(now), chronicleId, saltId);
    }

    /** Records that discovery ran for a chronicle with {@code totalKeys} keys. */
    public void discovered(String chronicleId, String saltId, long totalKeys) {
        jdbc.update("UPDATE chronicle_sketch_state SET discovered_keys = ?, discovery_due = false"
                + " WHERE chronicle_id = ? AND salt_id = ?", totalKeys, chronicleId, saltId);
    }

    /** True when some chronicle's sketch changed enough to trigger discovery. */
    public boolean anyDiscoveryDue() {
        Integer n = jdbc.queryForObject("SELECT count(*) FROM chronicle_sketch_state WHERE discovery_due",
                Integer.class);
        return n != null && n > 0;
    }

    /** Records that a sketch was sent to peers (and is no longer withdrawn). */
    public void sent(String chronicleId, String saltId, Instant now) {
        jdbc.update("UPDATE chronicle_sketch_state SET sent_at = ?, withdrawn_at = NULL WHERE chronicle_id = ?"
                + " AND salt_id = ?", Timestamp.from(now), chronicleId, saltId);
    }

    /** Records that a sent sketch was withdrawn. */
    public void withdrawn(String chronicleId, String saltId, Instant now) {
        jdbc.update("UPDATE chronicle_sketch_state SET withdrawn_at = ? WHERE chronicle_id = ? AND salt_id = ?"
                + " AND sent_at IS NOT NULL AND withdrawn_at IS NULL", Timestamp.from(now), chronicleId, saltId);
    }

    /** The sketch ids of a salt withdrawn since {@code since}. */
    public List<String> withdrawnSince(String saltId, Instant since) {
        return jdbc.queryForList("SELECT sketch_id FROM chronicle_sketch_state WHERE salt_id = ? AND sketch_id IS NOT"
                + " NULL AND withdrawn_at >= ? ORDER BY sketch_id", String.class, saltId, Timestamp.from(since));
    }

    // ------------------------------------------------------------------------- cleanup

    /** Forgets everything a salt keyed (a rotated instance salt or a stale network salt). */
    public void dropSalt(String saltId) {
        jdbc.update("DELETE FROM chronicle_key WHERE salt_id = ?", saltId);
        jdbc.update("DELETE FROM chronicle_fingerprint WHERE salt_id = ?", saltId);
        jdbc.update("DELETE FROM chronicle_sketch_state WHERE salt_id = ?", saltId);
    }

    /** Forgets a chronicle's keys and fingerprints under one salt, keeping its state row (sketch id, withdrawal). */
    public void dropChronicleSalt(String chronicleId, String saltId) {
        jdbc.update("DELETE FROM chronicle_key WHERE chronicle_id = ? AND salt_id = ?", chronicleId, saltId);
        jdbc.update("DELETE FROM chronicle_fingerprint WHERE chronicle_id = ? AND salt_id = ?", chronicleId, saltId);
        jdbc.update("UPDATE chronicle_sketch_state SET key_seq = 0, total_keys = 0, discovered_keys = 0,"
                + " discovery_due = false WHERE chronicle_id = ? AND salt_id = ?", chronicleId, saltId);
    }

    /** Forgets everything about a deleted chronicle. */
    public void forget(String chronicleId) {
        jdbc.update("DELETE FROM chronicle_key WHERE chronicle_id = ?", chronicleId);
        jdbc.update("DELETE FROM chronicle_fingerprint WHERE chronicle_id = ?", chronicleId);
        jdbc.update("DELETE FROM chronicle_sketch_state WHERE chronicle_id = ?", chronicleId);
    }

    /** The salts a chronicle has state under. */
    public List<String> saltsOf(String chronicleId) {
        return jdbc.queryForList("SELECT salt_id FROM chronicle_sketch_state WHERE chronicle_id = ?", String.class,
                chronicleId);
    }

    // ------------------------------------------------------------------------- disclosures

    /** The links already shown to a principal, among {@code linkIds}. */
    public List<String> disclosed(String principalId, Collection<String> linkIds) {
        if (linkIds.isEmpty()) {
            return List.of();
        }
        return jdbc.queryForList("SELECT link_id FROM chronicle_link_disclosure WHERE principal_id = ?"
                + " AND link_id = ANY(?)", String.class, principalId, linkIds.toArray(String[]::new));
    }

    /** How many new opaque links a principal was shown since {@code since}. */
    public long disclosedSince(String principalId, Instant since) {
        Long n = jdbc.queryForObject("SELECT count(*) FROM chronicle_link_disclosure WHERE principal_id = ?"
                + " AND disclosed_at >= ?", Long.class, principalId, Timestamp.from(since));
        return n == null ? 0 : n;
    }

    /** Records that a principal was shown a link. */
    public void disclose(String principalId, String linkId, Instant now) {
        jdbc.update("INSERT INTO chronicle_link_disclosure (principal_id, link_id, disclosed_at) VALUES (?, ?, ?)"
                + " ON CONFLICT (principal_id, link_id) DO NOTHING", principalId, linkId, Timestamp.from(now));
    }

    /** Forgets the disclosures of removed links. */
    public void forgetDisclosures(Collection<String> linkIds) {
        if (!linkIds.isEmpty()) {
            jdbc.update("DELETE FROM chronicle_link_disclosure WHERE link_id = ANY(?)",
                    (Object) linkIds.toArray(String[]::new));
        }
    }

    private static String cut(String s, int max) {
        return s == null || s.length() <= max ? s : s.substring(0, max);
    }

    private static Instant instant(Timestamp t) {
        return t == null ? null : t.toInstant();
    }
}
