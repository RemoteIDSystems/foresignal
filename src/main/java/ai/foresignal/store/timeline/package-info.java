/**
 * The event-density timeline for scrubbers (prompt 037): {@code GET /v1/timeline}.
 * {@link ai.foresignal.store.timeline.TimelineService} validates and chooses the bucket; a
 * {@link ai.foresignal.store.timeline.TimelineBackend} counts. {@link
 * ai.foresignal.store.timeline.RawTimelineBackend} counts {@code record_version} rows; prompt 038
 * adds a backend that serves ranges older than retention from {@code activity_rollup}.
 */
package ai.foresignal.store.timeline;
