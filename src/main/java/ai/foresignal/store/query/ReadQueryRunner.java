package ai.foresignal.store.query;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.support.ApiProperties;
import java.sql.SQLException;
import java.time.Duration;
import java.util.function.Function;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Runs the read API's queries: each call is one read-only transaction that starts with
 * {@code SET LOCAL statement_timeout} ({@code foresignal.api.query.statementTimeout}, default 30 s).
 *
 * <p>A query cancelled by the timeout (SQLState {@code 57014}) becomes {@code 503 QUERY_TIMEOUT}.
 * {@link #stream} hands out a template with fetch size 1,000, so streaming formats read the result
 * through a server-side cursor instead of loading it whole; that only works inside a transaction,
 * which this class always opens.
 */
@Component
public class ReadQueryRunner {

    /** Rows fetched per round trip by {@link #stream}. */
    public static final int STREAM_FETCH_SIZE = 1000;

    private static final String QUERY_CANCELED = "57014";

    /** The {@code Retry-After} of a timeout under an explicit statement timeout (prompt 037). */
    static final String RETRY_AFTER_SECONDS = "5";

    private final NamedParameterJdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate streaming;
    private final TransactionTemplate transactions;
    private final long timeoutMillis;

    /** Creates the runner. */
    public ReadQueryRunner(
            NamedParameterJdbcTemplate jdbc,
            DataSource dataSource,
            PlatformTransactionManager transactionManager,
            ApiProperties properties) {
        this.jdbc = jdbc;
        JdbcTemplate streamingTemplate = new JdbcTemplate(dataSource);
        streamingTemplate.setFetchSize(STREAM_FETCH_SIZE);
        this.streaming = new NamedParameterJdbcTemplate(streamingTemplate);
        this.transactions = new TransactionTemplate(transactionManager);
        this.transactions.setReadOnly(true);
        this.transactions.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        this.timeoutMillis = properties.query().statementTimeout().toMillis();
    }

    /** Runs {@code work} in a read-only transaction with the statement timeout. */
    public <T> T read(Function<NamedParameterJdbcTemplate, T> work) {
        return run(jdbc, work);
    }

    /** Like {@link #read}, with a template whose fetch size is {@value #STREAM_FETCH_SIZE}. */
    public <T> T stream(Function<NamedParameterJdbcTemplate, T> work) {
        return run(streaming, work);
    }

    /**
     * Like {@link #read}, with its own statement timeout (prompt 037: 15 s for asOf tiles, 10 s for
     * the timeline). A timeout becomes {@code 503 QUERY_TIMEOUT} with {@code Retry-After: 5}.
     */
    public <T> T read(Duration timeout, Function<NamedParameterJdbcTemplate, T> work) {
        return run(jdbc, timeout.toMillis(), work, true);
    }

    /** {@link #stream} with its own statement timeout, as {@link #read(Duration, Function)}. */
    public <T> T stream(Duration timeout, Function<NamedParameterJdbcTemplate, T> work) {
        return run(streaming, timeout.toMillis(), work, true);
    }

    /** The configured timeout in milliseconds. */
    public long timeoutMillis() {
        return timeoutMillis;
    }

    private <T> T run(NamedParameterJdbcTemplate template, Function<NamedParameterJdbcTemplate, T> work) {
        return run(template, timeoutMillis, work, false);
    }

    private <T> T run(NamedParameterJdbcTemplate template, long millis, Function<NamedParameterJdbcTemplate, T> work,
            boolean retryAfter) {
        try {
            return transactions.execute(status -> {
                template.getJdbcTemplate().execute("SET LOCAL statement_timeout = " + millis);
                return work.apply(template);
            });
        } catch (DataAccessException e) {
            if (isQueryCanceled(e)) {
                ForesignalException timeout = new ForesignalException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        ErrorCodes.QUERY_TIMEOUT,
                        "The query ran longer than " + millis + " ms; narrow the filters or retry.",
                        e);
                throw retryAfter ? timeout.withHeader(HttpHeaders.RETRY_AFTER, RETRY_AFTER_SECONDS) : timeout;
            }
            throw e;
        }
    }

    /** True when any cause in the chain is an SQL exception with SQLState {@code 57014}. */
    static boolean isQueryCanceled(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof SQLException sql && QUERY_CANCELED.equals(sql.getSQLState())) {
                return true;
            }
            if (t.getCause() == t) {
                break;
            }
        }
        return false;
    }
}
