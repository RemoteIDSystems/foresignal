package ai.foresignal.llm;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@code getMyLlmBudget} (prompt R2-011): a user's spend this UTC month on every provider they can use, from the
 * per-user rollup ({@code llm_user_usage}) and its unflushed counters.
 *
 * <p>The providers are the enabled shared ones and the user's personal ones, by id. On a shared provider the cap is
 * its {@code perUserMonthlyCapUsd}, the user's share; on a personal provider it is the provider's own
 * {@code monthlyCapUsd}. Spend made with an API token is the machine's and is never counted here.
 */
@Service
public class LlmUserBudgetService {

    private final LlmProviderService providers;
    private final LlmUsageMeter usage;
    private final Clock clock;

    /**
     * Creates the service.
     *
     * @param providers the providers
     * @param usage the usage meter
     * @param clock the clock
     */
    public LlmUserBudgetService(LlmProviderService providers, LlmUsageMeter usage, Clock clock) {
        this.providers = providers;
        this.usage = usage;
        this.clock = clock;
    }

    /**
     * The {@code MeLlmBudget} of {@code principalId}.
     *
     * @param principalId the user's principal
     * @return the budget
     */
    @Transactional(readOnly = true)
    public Map<String, Object> budget(String principalId) {
        Instant now = clock.instant();
        Instant monthStart = LlmSpendWindow.MONTH.start(now);
        List<LlmProviderEntity> usable = new ArrayList<>();
        providers.shared().stream().filter(LlmProviderEntity::isEnabled).forEach(usable::add);
        usable.addAll(providers.personal(principalId));
        usable.sort(java.util.Comparator.comparing(LlmProviderEntity::getId));
        List<Map<String, Object>> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (LlmProviderEntity provider : usable) {
            BigDecimal spent = usage.userCostSince(provider.getId(), principalId, monthStart);
            total = total.add(spent);
            BigDecimal cap = provider.isShared() ? provider.getPerUserSpendCaps().monthlyUsd()
                    : provider.getSpendCaps().monthlyUsd();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("providerId", provider.getId());
            item.put("name", provider.getId());
            item.put("shared", provider.isShared());
            item.put("capUsd", plain(cap));
            item.put("spentUsd", money(spent));
            item.put("remainingUsd", cap == null ? null : money(cap.subtract(spent).max(BigDecimal.ZERO)));
            items.add(item);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("providers", items);
        out.put("totalSpentUsd", money(total));
        out.put("asOf", now.toString());
        return out;
    }

    /** USD rounded to 6 decimal places, without trailing zeros (the {@code getLlmProviderBudget} form). */
    static BigDecimal money(BigDecimal value) {
        return plain(value == null ? BigDecimal.ZERO : value.setScale(6, RoundingMode.HALF_UP));
    }

    private static BigDecimal plain(BigDecimal value) {
        if (value == null) {
            return null;
        }
        BigDecimal stripped = value.stripTrailingZeros();
        return stripped.scale() < 0 ? stripped.setScale(0) : stripped;
    }
}
