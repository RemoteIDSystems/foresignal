package ai.foresignal.watch;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares {@code inWatchlist} and {@code notInWatchlist} as {@code Operator} beans (prompt 036), so
 * the 017 {@code OperatorRegistry} offers them to rules and views without touching the engine.
 */
@Configuration
public class WatchlistOperators {

    /** {@code inWatchlist}. */
    @Bean
    public WatchlistOperator inWatchlistOperator(WatchlistSnapshot snapshot, IdentityResolver identities) {
        return new WatchlistOperator(false, snapshot, identities);
    }

    /** {@code notInWatchlist}. */
    @Bean
    public WatchlistOperator notInWatchlistOperator(WatchlistSnapshot snapshot, IdentityResolver identities) {
        return new WatchlistOperator(true, snapshot, identities);
    }
}
