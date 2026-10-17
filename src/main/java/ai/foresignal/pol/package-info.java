/**
 * Pattern of life (prompt 114, task 1): opt-in subjects ({@link ai.foresignal.pol.PolSubjectEntity}), stay-point
 * extraction ({@link ai.foresignal.pol.StayPointExtractor}), in-house DBSCAN ({@link ai.foresignal.pol.Dbscan}),
 * learned places and stays that outlive the TRACK window, and deviation records ({@code NEW_PLACE},
 * {@code UNUSUAL_TIME}, {@code EXPECTED_ABSENT}) on the derived sources {@code pattern-of-life} and
 * {@code pattern-of-life-private}, so trigger rules match them like any other record.
 */
package ai.foresignal.pol;
