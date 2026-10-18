package ai.foresignal.social;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * How a catalog quota ({@link SocialPlatformCatalog.QuotaDef}) is read from the {@link QuotaLedger} (prompt 135):
 * its window as a {@link Duration} (a month counts as 30 days), its ledger unit, and its use (distinct keys for
 * hashtags, a sum otherwise).
 */
public final class QuotaUnits {

    private static final Pattern MONTHS = Pattern.compile("^P(\\d+)M$");

    private QuotaUnits() {}

    /** The window of a quota. */
    public static Duration window(SocialPlatformCatalog.QuotaDef q) {
        Matcher m = MONTHS.matcher(q.window());
        if (m.matches()) {
            return Duration.ofDays(30L * Integer.parseInt(m.group(1)));
        }
        return Duration.parse(q.window());
    }

    /** The ledger unit of a quota. */
    public static String unitKey(SocialPlatformCatalog.QuotaDef q) {
        return q.unit();
    }

    /** The use of a quota in its window ending now. */
    public static long used(QuotaLedger ledger, String sourceId, SocialPlatformCatalog.QuotaDef q, Duration window) {
        if ("hashtags".equals(q.unit())) {
            return ledger.keys(sourceId, q.unit(), window).size();
        }
        return ledger.used(sourceId, unitKey(q), window);
    }
}
