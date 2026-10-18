package ai.foresignal.social;

import ai.foresignal.api.error.BudgetExceededProblem;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.collect.BudgetExceededException;
import ai.foresignal.collect.SourceSpendLookup;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Money spend of paid social calls (prompt 135, invariant SOC-4): every paid call is <strong>estimated before it is
 * made</strong>, refused when {@code spent + estimate} would exceed the platform's monthly cap, and charged with its
 * actual cost afterwards, in {@code social_spend} (the 005 budget in money as well as calls).
 *
 * <p>X prices are the operator-editable {@code social.x.prices} of the platform settings ({@link
 * SocialPlatformSettings#prices}), seeded from the pricing page with its date: a search call can cost at most
 * {@code max_results × post-read}; a counts request costs {@code counts-recent-request} (or
 * {@code counts-all-request}) whatever it returns.
 */
@Component
public class XSpend implements SourceSpendLookup {

    /** Hours in an average month (365 × 24 / 12), for monthly estimates. */
    public static final int HOURS_PER_MONTH = 730;

    /**
     * The current month of a source.
     *
     * @param spentUsd spent this month
     * @param calls paid calls this month
     * @param refused calls refused this month
     */
    public record Month(BigDecimal spentUsd, long calls, long refused) {

        /** Nothing spent. */
        public static final Month ZERO = new Month(BigDecimal.ZERO, 0, 0);
    }

    private final NamedParameterJdbcTemplate jdbc;
    private final Clock clock;

    /** Creates the ledger. */
    public XSpend(NamedParameterJdbcTemplate jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    /** The month key of an instant (UTC). */
    public static String monthOf(Instant at) {
        return YearMonth.from(at.atZone(ZoneOffset.UTC)).toString();
    }

    /** The unit price of an item, or zero when none is set. */
    public static BigDecimal unitPrice(SocialPlatformSettings settings, String item) {
        SocialPlatformCatalog.Price p = settings == null ? null : settings.price(item);
        if (p == null || p.usd() == null) {
            return BigDecimal.ZERO;
        }
        return p.usd().divide(BigDecimal.valueOf(Math.max(1, p.per())), 8, RoundingMode.HALF_UP);
    }

    /** The most a recent-search call returning up to {@code maxResults} posts can cost. */
    public static BigDecimal estimateSearch(SocialPlatformSettings settings, int maxResults) {
        return unitPrice(settings, SocialPlatformCatalog.POST_READ).multiply(BigDecimal.valueOf(Math.max(0,
                maxResults)));
    }

    /** What one counts request costs ({@code counts/all} when {@code fullArchive}). */
    public static BigDecimal estimateCounts(SocialPlatformSettings settings, boolean fullArchive) {
        return unitPrice(settings, fullArchive ? SocialPlatformCatalog.COUNTS_ALL_REQUEST
                : SocialPlatformCatalog.COUNTS_RECENT_REQUEST);
    }

    /** What a lookup of {@code posts} posts by id costs (deletion checks). */
    public static BigDecimal estimateLookup(SocialPlatformSettings settings, int posts) {
        return unitPrice(settings, SocialPlatformCatalog.POST_READ).multiply(BigDecimal.valueOf(Math.max(0, posts)));
    }

    /** The monthly cost of hourly counts for {@code standingTopics} topics, shown before they are enabled. */
    public static BigDecimal standingCountsMonthly(SocialPlatformSettings settings, int standingTopics) {
        return estimateCounts(settings, false).multiply(BigDecimal.valueOf((long) standingTopics * HOURS_PER_MONTH))
                .setScale(4, RoundingMode.HALF_UP);
    }

    /** This month's spend of a source. */
    public Month month(String sourceId) {
        return month(sourceId, monthOf(clock.instant()));
    }

    /** One month's spend of a source. */
    public Month month(String sourceId, String month) {
        List<Month> rows = jdbc.query("SELECT spent_usd, calls, refused FROM social_spend WHERE id = :id",
                new MapSqlParameterSource().addValue("id", sourceId + "|" + month),
                (rs, n) -> new Month(rs.getBigDecimal("spent_usd"), rs.getLong("calls"), rs.getLong("refused")));
        return rows.isEmpty() ? Month.ZERO : rows.get(0);
    }

    /**
     * Checks a paid call before it is made.
     *
     * @param sourceId the source
     * @param settings the platform settings (cap and prices)
     * @param estimate the most the call can cost
     * @throws BudgetExceededException when {@code spent + estimate} exceeds the monthly cap (the refusal is counted)
     */
    public void reserve(String sourceId, SocialPlatformSettings settings, BigDecimal estimate)
            throws BudgetExceededException {
        BigDecimal cap = settings == null ? null : settings.monthlyCapUsd();
        if (cap == null || estimate == null || estimate.signum() <= 0) {
            return;
        }
        Month m = month(sourceId);
        if (m.spentUsd().add(estimate).compareTo(cap) > 0) {
            refused(sourceId);
            throw new BudgetExceededException(sourceId, sourceId + ": the monthly cap of $" + cap.toPlainString()
                    + " would be exceeded (spent $" + m.spentUsd().setScale(4, RoundingMode.HALF_UP).toPlainString()
                    + ", this call up to $" + estimate.setScale(4, RoundingMode.HALF_UP).toPlainString() + ")");
        }
    }

    /**
     * The API-path refusal of a paid call (137 cues): {@code 429 BUDGET_EXCEEDED} with the cap and the spend.
     *
     * @return the problem, or null when the call fits under the cap
     */
    public ForesignalException refusal(String sourceId, SocialPlatformSettings settings, BigDecimal estimate) {
        try {
            reserve(sourceId, settings, estimate);
            return null;
        } catch (BudgetExceededException e) {
            Month m = month(sourceId);
            return new BudgetExceededProblem(e.getMessage())
                    .withProperty("monthlyCapUsd", settings.monthlyCapUsd())
                    .withProperty("spentThisMonth", m.spentUsd())
                    .withProperty("estimateUsd", estimate);
        }
    }

    /** Charges the actual cost of a call made. */
    public void charge(String sourceId, BigDecimal actual) {
        Instant now = clock.instant();
        String month = monthOf(now);
        jdbc.update("INSERT INTO social_spend (id, source_id, month, spent_usd, calls, refused, updated_at) VALUES (:id,"
                + " :s, :m, :usd, 1, 0, :at) ON CONFLICT (id) DO UPDATE SET spent_usd = social_spend.spent_usd +"
                + " EXCLUDED.spent_usd, calls = social_spend.calls + 1, updated_at = EXCLUDED.updated_at",
                new MapSqlParameterSource().addValue("id", sourceId + "|" + month).addValue("s", sourceId)
                        .addValue("m", month).addValue("usd", actual == null ? BigDecimal.ZERO : actual.max(
                                BigDecimal.ZERO)).addValue("at", Timestamp.from(now)));
    }

    private void refused(String sourceId) {
        Instant now = clock.instant();
        String month = monthOf(now);
        jdbc.update("INSERT INTO social_spend (id, source_id, month, spent_usd, calls, refused, updated_at) VALUES (:id,"
                + " :s, :m, 0, 0, 1, :at) ON CONFLICT (id) DO UPDATE SET refused = social_spend.refused + 1, updated_at"
                + " = EXCLUDED.updated_at", new MapSqlParameterSource().addValue("id", sourceId + "|" + month)
                .addValue("s", sourceId).addValue("m", month).addValue("at", Timestamp.from(now)));
    }

    /** The projected month-end spend at this month's rate, or null early in the month. */
    public BigDecimal projected(String sourceId) {
        Instant now = clock.instant();
        Instant start = YearMonth.from(now.atZone(ZoneOffset.UTC)).atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = YearMonth.from(now.atZone(ZoneOffset.UTC)).plusMonths(1).atDay(1).atStartOfDay()
                .toInstant(ZoneOffset.UTC);
        long elapsed = Duration.between(start, now).toMinutes();
        if (elapsed < 60) {
            return null;
        }
        BigDecimal spent = month(sourceId).spentUsd();
        return spent.multiply(BigDecimal.valueOf(ChronoUnit.MINUTES.between(start, end)))
                .divide(BigDecimal.valueOf(elapsed), 4, RoundingMode.HALF_UP);
    }

    @Override
    public Double spentUsdThisMonth(String sourceId) {
        if (!SocialVocabulary.X_ADAPTER.equals(sourceId)) {
            return null;
        }
        try {
            return month(sourceId).spentUsd().doubleValue();
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** The spend members of every paid source (unused keys omitted), for views. */
    public Map<String, Object> summary(String sourceId) {
        Month m = month(sourceId);
        return Map.of("spentThisMonth", m.spentUsd(), "refusedThisMonth", m.refused());
    }
}
