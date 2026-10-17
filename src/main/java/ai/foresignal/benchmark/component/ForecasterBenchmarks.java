package ai.foresignal.benchmark.component;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** The four forecaster benchmarks as beans (prompt 123), so the synthetic suite finds them with the others. */
@Configuration
public class ForecasterBenchmarks {

    /** The base-rate forecaster's benchmark. */
    @Bean
    public ForecasterBenchmark baseRateForecasterBenchmark() {
        return new ForecasterBenchmark(ForecasterBenchmark.Kind.BASE_RATE);
    }

    /** The analog forecaster's benchmark. */
    @Bean
    public ForecasterBenchmark analogForecasterBenchmark() {
        return new ForecasterBenchmark(ForecasterBenchmark.Kind.ANALOG);
    }

    /** The precursor forecaster's benchmark. */
    @Bean
    public ForecasterBenchmark precursorForecasterBenchmark() {
        return new ForecasterBenchmark(ForecasterBenchmark.Kind.PRECURSOR);
    }

    /** The ensemble's benchmark. */
    @Bean
    public ForecasterBenchmark ensembleForecasterBenchmark() {
        return new ForecasterBenchmark(ForecasterBenchmark.Kind.ENSEMBLE);
    }
}
