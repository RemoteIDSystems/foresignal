/**
 * Reports (prompt 132, Intelligence-Design §12.4): analyst reports built from a selection, a tag, an investigation, a
 * chronicle, a situation or a workspace, edited by their authors and published as immutable revisions.
 *
 * <ul>
 *   <li><b>Model:</b> {@link ai.foresignal.report.ReportEntity} (table {@code report}: the draft, its ordered blocks in
 *       {@code sections}, the optimistic-lock {@code version}), {@link ai.foresignal.report.ReportRevisionEntity}
 *       ({@code report_revision}: a frozen revision with pinned references, stored snapshots and its Markdown, HTML
 *       and JSON bundle renditions) and {@link ai.foresignal.report.ReportSendEntity} ({@code report_send}).</li>
 *   <li><b>Drafts:</b> {@link ai.foresignal.report.ReportDrafter} builds deterministic blocks from the source with a
 *       template; {@link ai.foresignal.report.ReportNarrativeWriter} optionally adds a labeled LLM narrative block
 *       with verified citations. No LLM is needed for anything else.</li>
 *   <li><b>Publishing:</b> {@link ai.foresignal.report.ReportPublisher} pins every reference ({@code @seq} for records,
 *       envelope snapshots for the rest), renders 055 maps as attachments and writes the renditions with
 *       {@link ai.foresignal.report.ReportRenderer} (TLP banner on every page, the automated-analysis footer when a
 *       block is machine-generated). Published revisions never change (RPT-1).</li>
 *   <li><b>Sharing:</b> reports are 130 work products (owner, authors, access list, TLP inherited from the strictest
 *       input); {@link ai.foresignal.report.ReportSender} sends them to principals and groups here, the 112 share
 *       target serves CLEAR (or allowed GREEN) renditions read-only, and prompt 132's {@code exchange} package sends
 *       them to other instances.</li>
 * </ul>
 */
package ai.foresignal.report;
