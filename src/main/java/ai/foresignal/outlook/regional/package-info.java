/**
 * Regional outlooks (prompt 126, {@code regional-outlook} 1.0.0): per region and event family, an NB GLM on weekly
 * counts with lags, a 13-week average, annual seasonality, neighbors' counts, the region's index and a partially
 * pooled intercept; published weekly for the next six 4-week windows, each window a numeric ledger question whose
 * accuracy travels with the outlook.
 */
package ai.foresignal.outlook.regional;
