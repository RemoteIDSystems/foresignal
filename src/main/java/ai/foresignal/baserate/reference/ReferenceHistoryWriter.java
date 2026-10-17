package ai.foresignal.baserate.reference;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Writes {@code reference_history} (prompt 121). A chunk is replaced as a whole in one transaction: the source's rows
 * in the chunk's weeks (and countries, when the chunk names them) are deleted and the new weekly rows inserted, so a
 * re-import of the same chunk leaves exactly the same rows (idempotent) and a resumed import never double counts.
 */
@Component
public class ReferenceHistoryWriter {

    private final NamedParameterJdbcTemplate jdbc;
    private final TransactionTemplate tx;

    /** Creates the writer. */
    public ReferenceHistoryWriter(NamedParameterJdbcTemplate jdbc, TransactionTemplate tx) {
        this.jdbc = jdbc;
        this.tx = tx;
    }

    /**
     * Replaces one chunk.
     *
     * @param source the reference source ({@link ReferenceFamilies#UCDP}, …)
     * @param importId the import writing it, or null for the own-history fold
     * @param from the first week (a Monday, inclusive)
     * @param to the end week (a Monday, exclusive)
     * @param countries the countries the chunk covers (their admin1 rows included), or empty for every region
     * @param rows the weekly rows (rows outside the chunk are ignored)
     * @return the rows written
     */
    public int replace(String source, String importId, LocalDate from, LocalDate to, Collection<String> countries,
            List<WeeklyCounts.Row> rows) {
        String license = ReferenceFamilies.LICENSES.getOrDefault(source, "unknown");
        String egress = ReferenceFamilies.EGRESS.getOrDefault(source, "LOCAL_ONLY");
        Integer written = tx.execute(status -> {
            MapSqlParameterSource p = new MapSqlParameterSource().addValue("source", source)
                    .addValue("from", Date.valueOf(from)).addValue("to", Date.valueOf(to));
            StringBuilder delete = new StringBuilder("DELETE FROM reference_history WHERE source = :source AND"
                    + " week_start >= :from AND week_start < :to");
            if (countries != null && !countries.isEmpty()) {
                delete.append(" AND substr(region_code, 1, 2) IN (:countries)");
                p.addValue("countries", List.copyOf(countries));
            }
            jdbc.update(delete.toString(), p);
            List<MapSqlParameterSource> batch = new ArrayList<>();
            for (WeeklyCounts.Row row : rows) {
                if (row.weekStart().isBefore(from) || !row.weekStart().isBefore(to)) {
                    continue;
                }
                if (countries != null && !countries.isEmpty()
                        && !countries.contains(row.regionCode().substring(0, Math.min(2, row.regionCode().length())))) {
                    continue;
                }
                batch.add(new MapSqlParameterSource().addValue("family", row.family())
                        .addValue("region", row.regionCode()).addValue("week", Date.valueOf(row.weekStart()))
                        .addValue("source", source).addValue("count", row.count())
                        .addValue("fatalities", row.fatalities()).addValue("licence", license)
                        .addValue("egress", egress).addValue("importId", importId));
            }
            if (!batch.isEmpty()) {
                jdbc.batchUpdate("INSERT INTO reference_history (family, region_code, week_start, source, count,"
                        + " fatalities, licence, egress, import_id) VALUES (:family, :region, :week, :source, :count,"
                        + " :fatalities, :licence, :egress, :importId) ON CONFLICT (family, region_code, week_start,"
                        + " source) DO UPDATE SET count = EXCLUDED.count, fatalities = EXCLUDED.fatalities, licence ="
                        + " EXCLUDED.licence, egress = EXCLUDED.egress, import_id = EXCLUDED.import_id",
                        batch.toArray(MapSqlParameterSource[]::new));
            }
            return batch.size();
        });
        return written == null ? 0 : written;
    }

    /**
     * Deletes the rows an import wrote.
     *
     * @return the rows deleted
     */
    public int deleteImport(String importId) {
        return jdbc.update("DELETE FROM reference_history WHERE import_id = :id",
                new MapSqlParameterSource("id", importId));
    }
}
