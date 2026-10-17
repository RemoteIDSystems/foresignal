/**
 * The series forecaster (prompt 126, {@code series-forecast} 1.0.0): seasonal naive, ETS(A,N,N), ETS(A,A,A), an NB
 * GLM for counts and a damped trend for continuous series, each a pure class; a rolling-origin backtest over the last
 * eight weekly origins picks the method with the lowest mean pinball loss at the 7-day horizon; quantile forecasts
 * for 1, 7 and 28 days are stored nightly, opened as ledger questions for the configured series, and actuals outside
 * the 1-step p05–p95 band become {@code forecast-exceedance} findings.
 */
package ai.foresignal.outlook.series;
