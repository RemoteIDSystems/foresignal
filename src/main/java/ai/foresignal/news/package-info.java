/**
 * News coverage (prompt 133, Intelligence-Design §12.5): articles clustered into stories, coverage balance, blindspots
 * and divergence, and their use as evidence quality and caveats in situations, hypotheses, assessments and reports.
 *
 * <ul>
 *   <li>{@link ai.foresignal.news.NewsStoryResolver}: the 116 {@code news-story} family; articles join a story by title
 *       embedding cosine (027) or, without embeddings, normalized-title token Jaccard; wire reprints collapse into one
 *       evidence group ({@link ai.foresignal.news.NewsGrouper}).
 *   <li>{@link ai.foresignal.news.CoverageAnalyzer} 1.0.0: balance by lean, country, outlet type and factuality,
 *       state-affiliated share, blindspot and skew.
 *   <li>{@link ai.foresignal.news.NewsCoverageService} and {@link ai.foresignal.news.NewsCoverageJob}: story,
 *       topic, situation and hypothesis coverage, corroboration by non-news evidence, daily coverage series and the
 *       {@code coverage-blindspot} and {@code coverage-divergence} findings.
 * </ul>
 *
 * <p><strong>Coverage describes reporting, never truth:</strong> balance and blindspots never raise or lower the truth
 * of a claim; corroboration still comes from independent, non-news evidence groups.
 */
package ai.foresignal.news;
