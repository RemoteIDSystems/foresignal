package ai.foresignal.store.history;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.store.RecordVersionEntity;
import ai.foresignal.test.IntegrationTest;
import jakarta.persistence.Column;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The Flyway-built {@code record_version} and the {@link RecordVersionEntity} mapping agree (Architecture §9; the
 * Flyway variant of the old Core-Data-Model §17 parity test, prompt 144): the entity's mapped columns equal the
 * table's columns by name, type and nullability, {@link HistoryDdl#COLUMNS} lists them in the entity's order, and the
 * table is the partitioned parent.
 */
@IntegrationTest
class SchemaParityIT {

    @Autowired private JdbcTemplate jdbc;

    @Test
    @DisplayName("record_version is a partitioned table (relkind 'p')")
    void recordVersionIsPartitioned() {
        assertThat(jdbc.queryForObject(
                        "select relkind::text from pg_class c join pg_namespace n on n.oid = c.relnamespace"
                                + " where n.nspname = 'public' and c.relname = 'record_version'",
                        String.class))
                .isEqualTo("p");
    }

    @Test
    @DisplayName("the migrated table's columns equal the entity's mapped columns by name, nullability and type")
    void columnsMatchTheEntity() {
        assertThat(tableColumns()).isEqualTo(expectedColumns());
    }

    @Test
    @DisplayName("HistoryDdl.COLUMNS describes the migrated table: same names, types and nullability")
    void ddlConstantsDescribeTheTable() {
        Map<String, String> fromConstants = new TreeMap<>();
        for (HistoryDdl.Column column : HistoryDdl.COLUMNS) {
            fromConstants.put(column.name(), describeDeclared(column.type(), column.notNull()));
        }
        assertThat(tableColumns()).isEqualTo(fromConstants);
    }

    @Test
    @DisplayName("the column list in the DDL constants matches the entity, in order")
    void ddlConstantsFollowEntityOrder() {
        List<String> entityOrder = new ArrayList<>();
        for (Field field : RecordVersionEntity.class.getDeclaredFields()) {
            Column column = field.getAnnotation(Column.class);
            if (column != null) {
                entityOrder.add(column.name());
            }
        }

        assertThat(HistoryDdl.COLUMNS).extracting(HistoryDdl.Column::name).containsExactlyElementsOf(entityOrder);
    }

    @Test
    @DisplayName("the prompt-002 unique constraint does not exist")
    void noLegacyUniqueConstraint() {
        assertThat(jdbc.queryForObject(
                        "select count(*) from pg_constraint where conname = 'record_version_source_external_version_key'",
                        Long.class))
                .isZero();
    }

    // ---------------------------------------------------------------- helpers

    /** The table's columns as {@code udt(length) [not] null}, read from the catalog. */
    private Map<String, String> tableColumns() {
        Map<String, String> actual = new TreeMap<>();
        jdbc.query(
                """
                select column_name, is_nullable, udt_name, character_maximum_length
                  from information_schema.columns
                 where table_schema = 'public' and table_name = 'record_version'
                """,
                rs -> {
                    Object length = rs.getObject(4);
                    actual.put(
                            rs.getString(1),
                            describe(rs.getString(3), length == null ? null : ((Number) length).intValue(),
                                    "NO".equals(rs.getString(2))));
                });
        return actual;
    }

    /** Expected columns derived from the entity's {@code @Column} and {@code @JdbcTypeCode}. */
    private static Map<String, String> expectedColumns() {
        Map<String, String> expected = new TreeMap<>();
        for (Field field : RecordVersionEntity.class.getDeclaredFields()) {
            Column column = field.getAnnotation(Column.class);
            if (column == null) {
                continue;
            }
            boolean notNull = !column.nullable() || field.getType().isPrimitive();
            String udt;
            Integer length = null;
            JdbcTypeCode code = field.getAnnotation(JdbcTypeCode.class);
            if (column.columnDefinition().startsWith("geometry")) {
                udt = "geometry";
            } else if (code != null && code.value() == SqlTypes.SMALLINT) {
                udt = "int2";
            } else if (code != null && code.value() == SqlTypes.JSON) {
                udt = "jsonb";
            } else if (code != null && code.value() == SqlTypes.TIMESTAMP_UTC) {
                udt = "timestamptz";
            } else if (field.getType() == Long.class || field.getType() == long.class) {
                udt = "int8";
            } else if (field.getType() == Integer.class || field.getType() == int.class) {
                udt = "int4";
            } else if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                udt = "bool";
            } else if (field.getType() == String.class) {
                udt = "varchar";
                length = column.length();
            } else {
                throw new AssertionError("No expected SQL type for " + field);
            }
            expected.put(column.name(), describe(udt, length, notNull));
        }
        return expected;
    }

    /** A {@link HistoryDdl.Column} type, as the catalog reports it. */
    private static String describeDeclared(String type, boolean notNull) {
        if (type.startsWith("varchar(")) {
            return describe("varchar", Integer.parseInt(type.substring(8, type.length() - 1)), notNull);
        }
        String udt = switch (type) {
            case "bigint" -> "int8";
            case "integer" -> "int4";
            case "smallint" -> "int2";
            case "boolean" -> "bool";
            case "timestamptz" -> "timestamptz";
            case "jsonb" -> "jsonb";
            default -> type.startsWith("geometry") ? "geometry" : type;
        };
        return describe(udt, null, notNull);
    }

    private static String describe(String udt, Integer length, boolean notNull) {
        return udt + (length == null ? "" : "(" + length + ")") + (notNull ? " not null" : " null");
    }
}
