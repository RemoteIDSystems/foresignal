package ai.foresignal.store.history;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers {@link RawHistoryFidelity} when no other {@link HistoryFidelity} bean exists (prompt
 * 037). Prompt 044 declares the downsampling-aware implementation as a component, which wins.
 */
@Configuration(proxyBeanMethods = false)
public class HistoryFidelityConfiguration {

    /** The raw fallback. */
    @Bean
    @ConditionalOnMissingBean(HistoryFidelity.class)
    public HistoryFidelity rawHistoryFidelity() {
        return new RawHistoryFidelity();
    }
}
