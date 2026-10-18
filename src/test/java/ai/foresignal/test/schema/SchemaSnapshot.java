package ai.foresignal.test.schema;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The comparable shape of one database schema (prompt 144): columns, key constraints, indexes and sequences, each as
 * a normalized line, so two schemas built different ways can be compared as sets.
 *
 * <p>What is deliberately left out, because it is not part of what must agree:
 *
 * <ul>
 *   <li>{@code flyway_schema_history} and objects owned by an extension (PostGIS's {@code spatial_ref_sys});
 *   <li>column order (Hibernate 7 orders columns by size; the baseline lists them in declaration order);
 *   <li>constraint and index names (Hibernate's are hashed; the baseline's are descriptive), so constraints are
 *       compared by {@code pg_get_constraintdef} and indexes by their definition with the name removed;
 *   <li>CHECK constraints: Hibernate adds one per enum column, and the baseline deliberately has none (Architecture
 *       §25); the application enforces enum values.
 * </ul>
 *
 * @param columns {@code table.column type [not null] [default ...] [identity ...] [generated ...]}
 * @param constraints {@code table contype definition} for primary key, unique, foreign key and exclusion constraints
 * @param indexes {@code table normalized-definition}
 * @param sequences {@code name increment}
 */
public record SchemaSnapshot(Set<String> columns, Set<String> constraints, Set<String> indexes, Set<String> sequences) {

    /** Tables never compared. */
    public static final List<String> IGNORED_TABLES = List.of("flyway_schema_history", "spatial_ref_sys");

    private static final Pattern INDEX_NAME = Pattern.compile("^CREATE (UNIQUE )?INDEX \\S+ ON (ONLY )?");

    /** Relations in the current schema that no extension owns. */
    private static final String NOT_EXTENSION_OWNED =
            "not exists (select 1 from pg_depend dep where dep.objid = c.oid and dep.deptype = 'e')";

    /**
     * Reads the current schema of the database behind {@code jdbc}.
     *
     * @param jdbc a template over the database to read
     * @return the snapshot
     */
    public static SchemaSnapshot read(JdbcTemplate jdbc) {
        String ignored = "('" + String.join("', '", IGNORED_TABLES) + "')";
        Set<String> columns = new TreeSet<>(jdbc.query("""
                select c.relname || '.' || a.attname || ' ' || format_type(a.atttypid, a.atttypmod)
                       || case when a.attnotnull then ' not null' else '' end
                       || coalesce(' default ' || case when a.attgenerated = '' then pg_get_expr(d.adbin, d.adrelid) end, '')
                       || case when a.attidentity <> '' then ' identity ' || a.attidentity else '' end
                       || case when a.attgenerated <> '' then ' generated ' || pg_get_expr(d.adbin, d.adrelid) else '' end
                  from pg_attribute a
                  join pg_class c on c.oid = a.attrelid
                  join pg_namespace n on n.oid = c.relnamespace
                  left join pg_attrdef d on d.adrelid = a.attrelid and d.adnum = a.attnum
                 where n.nspname = current_schema() and c.relkind in ('r', 'p') and a.attnum > 0 and not a.attisdropped
                   and c.relname not in %s and %s
                """.formatted(ignored, NOT_EXTENSION_OWNED), (rs, i) -> rs.getString(1)));
        Set<String> constraints = new TreeSet<>(jdbc.query("""
                select c.relname || ' ' || con.contype || ' ' || pg_get_constraintdef(con.oid)
                  from pg_constraint con
                  join pg_class c on c.oid = con.conrelid
                  join pg_namespace n on n.oid = c.relnamespace
                 where n.nspname = current_schema() and con.contype in ('p', 'u', 'f', 'x')
                   and c.relname not in %s and %s
                """.formatted(ignored, NOT_EXTENSION_OWNED), (rs, i) -> rs.getString(1)));
        String schema = jdbc.queryForObject("select current_schema()", String.class);
        Set<String> indexes = new TreeSet<>(jdbc.query("""
                select i.tablename, i.indexdef
                  from pg_indexes i
                  join pg_class c on c.relname = i.indexname
                  join pg_namespace n on n.oid = c.relnamespace and n.nspname = i.schemaname
                 where i.schemaname = current_schema() and i.tablename not in %s and %s
                """.formatted(ignored, NOT_EXTENSION_OWNED),
                (rs, i) -> rs.getString(1) + " " + normalizeIndex(rs.getString(2), schema)));
        Set<String> sequences = new TreeSet<>(jdbc.query("""
                select s.sequencename || ' ' || s.increment_by
                  from pg_sequences s
                 where s.schemaname = current_schema()
                """, (rs, i) -> rs.getString(1)));
        return new SchemaSnapshot(columns, constraints, indexes, sequences);
    }

    /**
     * An index definition without its name, schema qualifier or {@code ONLY}.
     *
     * @param indexdef {@code pg_indexes.indexdef}
     * @param schema the schema to strip
     * @return the normalized definition
     */
    static String normalizeIndex(String indexdef, String schema) {
        String unnamed = INDEX_NAME.matcher(indexdef).replaceFirst(m -> "CREATE " + (m.group(1) == null ? "" : m.group(1))
                + "INDEX ON ");
        return unnamed.replace(schema + ".", "");
    }

    /**
     * The lines that differ from {@code other}, prefixed {@code "- "} (only here) or {@code "+ "} (only in
     * {@code other}) with the category, minus every line an allowlist pattern matches.
     *
     * @param other the schema to compare with
     * @param allowlist regular expressions for known, accepted differences (each matched against the whole line)
     * @return the unexplained differences, empty when the schemas agree
     */
    public List<String> differences(SchemaSnapshot other, List<Pattern> allowlist) {
        List<String> out = new java.util.ArrayList<>();
        compare("column", columns, other.columns, allowlist, out);
        compare("constraint", constraints, other.constraints, allowlist, out);
        compare("index", indexes, other.indexes, allowlist, out);
        compare("sequence", sequences, other.sequences, allowlist, out);
        return out;
    }

    private static void compare(String kind, Set<String> mine, Set<String> theirs, List<Pattern> allowlist,
            List<String> out) {
        for (String line : mine) {
            if (!theirs.contains(line)) {
                add("- " + kind + " " + line, allowlist, out);
            }
        }
        for (String line : theirs) {
            if (!mine.contains(line)) {
                add("+ " + kind + " " + line, allowlist, out);
            }
        }
    }

    private static void add(String line, List<Pattern> allowlist, List<String> out) {
        for (Pattern pattern : allowlist) {
            if (pattern.matcher(line).matches()) {
                return;
            }
        }
        out.add(line);
    }
}
