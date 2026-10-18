/**
 * Chronicles (prompt 131, Intelligence-Design §12.3): named, saved, shareable monitoring threads that bundle the
 * layers a person chooses and, while active, record their own history of everything that matches, independent of
 * global retention.
 *
 * <ul>
 *   <li><b>Model:</b> {@link ai.foresignal.chronicle.ChronicleEntity} (table {@code chronicle}),
 *       {@link ai.foresignal.chronicle.ChronicleLayerEntity} ({@code chronicle_layer}: a
 *       {@link ai.foresignal.chronicle.ChronicleSelector}, an optional area override and a
 *       {@link ai.foresignal.chronicle.CaptureMode}), the thread {@code chronicle_event}, per-principal read cursors,
 *       captured series points, rollups and frame pins.</li>
 *   <li><b>Storage:</b> {@code chronicle_version}, partitioned by chronicle (LIST on {@code chronicle_id}, one leaf
 *       per chronicle created on activation and dropped on deletion), created by bootstrap SQL
 *       ({@link ai.foresignal.chronicle.ChronicleDdl}), never by Hibernate. A chronicle owns its copy: global
 *       retention never deletes it (CHR-1).</li>
 *   <li><b>Capture:</b> {@link ai.foresignal.chronicle.ChronicleCaptureService} follows {@code record_version} from
 *       one durable cursor per active chronicle ({@code chronicle.cursor.{id}}, in the retention consumer clamp), so
 *       ingest is never blocked (CHR-2); a size cap degrades full and downsampled layers to events-only, visibly.</li>
 *   <li><b>Replay:</b> {@link ai.foresignal.chronicle.ChronicleReplayService} reads chronicle storage only: state at
 *       a time, vector tiles, tracks, series and a frame index, keyed by transaction and observation time with no
 *       look-ahead.</li>
 *   <li><b>Work product:</b> chronicles carry an owner, an access list and a TLP label (130), own a 129 workspace,
 *       and are proposed by the analysis cycles ({@code propose_chronicle}, 128).</li>
 * </ul>
 */
package ai.foresignal.chronicle;
