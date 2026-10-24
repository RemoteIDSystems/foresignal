package ai.foresignal.config;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.schema.ScratchDatabase;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * {@code V12__external_forecaster_id_separator.sql}: external forecaster ids stored as
 * {@code edp:{sourceId}/{streamId}} become {@code edp:{sourceId}:{streamId}} in every table and JSON document that
 * names them, and no other id changes. Runs on a scratch database migrated to V11, seeded, then migrated on.
 */
@IntegrationTest
class ExternalForecasterIdMigrationIT {

    static final String OLD = "edp:ext-markets/kalshi";
    static final String NEW = "edp:ext-markets:kalshi";
    static final String Q = "01JA8Q3V7S2K9W4T6Y1B5N0M3C";

    @Autowired JdbcTemplate admin;

    ScratchDatabase scratch;

    @AfterEach
    void drop() {
        if (scratch != null) {
            scratch.drop(admin);
        }
    }

    private Flyway flyway(String target) {
        return Flyway.configure().dataSource(scratch.dataSource()).locations("classpath:db/migration").target(target)
                .load();
    }

    @Test
    @DisplayName("the slash form is rewritten everywhere it is stored; other forecasters are untouched")
    void rewritesStoredIds() {
        scratch = ScratchDatabase.recreate(admin, "external_forecaster_id_it");
        flyway("11").migrate();
        JdbcTemplate db = scratch.jdbc();
        for (String id : new String[] {OLD, "base-rate"}) {
            db.update("insert into forecaster (active, created_at, type, version, id, name, config) "
                    + "values (true, now(), ?, '1.0.0', ?, ?, '{}'::jsonb)", id.startsWith("edp:") ? "external" : id,
                    id, id);
        }
        db.update("insert into forecast (recalibrated, asof_seq, made_at, probability, id, question_id, forecaster_type,"
                + " forecaster_id, inputs) values (false, 0, now(), 0.6, 'F1', ?, 'ensemble', 'ensemble', ?::jsonb)", Q,
                "{\"members\":[{\"forecasterId\":\"" + OLD + "\"},{\"forecasterId\":\"base-rate\"}],"
                        + "\"weights\":{\"" + OLD + "\":0.4,\"base-rate\":0.6}}");
        db.update("insert into forecast (recalibrated, asof_seq, made_at, probability, id, question_id, forecaster_type,"
                + " forecaster_id) values (false, 0, now(), 0.6, 'F2', ?, 'external', ?)", Q, OLD);
        db.update("insert into forecast_calibration (platt_n, weight_n, updated_at, category, forecaster_id, id) "
                + "values (0, 0, now(), 'markets', ?, ?)", OLD, OLD + "|markets");
        db.update("insert into forecast_score (hedged_forecasts, n_days, n_forecasts, computed_at, resolved_at, "
                + "question_type, scorer_version, question_id, forecaster_type, category, forecaster_id, id) "
                + "values (0, 1, 1, now(), now(), 'binary', '1', ?, 'external', 'markets', ?, ?)", Q, OLD, Q + "|" + OLD);
        db.update("insert into external_question_link (id, source_id, stream_id, external_question_id, question_id, "
                + "orientation, forecaster_id, created_by, created_at) values ('L1', 'ext-markets', 'kalshi', 'K1', ?, "
                + "'same', ?, 'test', now())", Q, OLD);
        db.update("insert into analysis_correlation (id, source_id, stream_id, question_id, forecaster_id, created_by, "
                + "created_at) values ('C1', 'ext-markets', 'kalshi', ?, ?, 'test', now())", Q, OLD);

        flyway("12").migrate();

        assertThat(db.queryForList("select id from forecaster order by id", String.class))
                .containsExactly("base-rate", NEW);
        assertThat(db.queryForObject("select forecaster_id from forecast where id = 'F2'", String.class)).isEqualTo(NEW);
        assertThat(db.queryForObject("select inputs::text from forecast where id = 'F1'", String.class))
                .contains("\"" + NEW + "\"").doesNotContain(OLD).contains("\"base-rate\"");
        assertThat(db.queryForObject("select forecaster_id || ' ' || id from forecast_calibration", String.class))
                .isEqualTo(NEW + " " + NEW + "|markets");
        assertThat(db.queryForObject("select forecaster_id || ' ' || id from forecast_score", String.class))
                .isEqualTo(NEW + " " + Q + "|" + NEW);
        assertThat(db.queryForObject("select forecaster_id from external_question_link", String.class)).isEqualTo(NEW);
        assertThat(db.queryForObject("select forecaster_id from analysis_correlation", String.class)).isEqualTo(NEW);
    }
}
