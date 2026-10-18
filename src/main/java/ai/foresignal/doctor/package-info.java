/**
 * The adapter doctor (prompt 140, Product Charter FR-57): when a source breaks, it probes the live upstream through the
 * budgeted client, compares the response with the adapter's recorded fixture structurally ({@link
 * ai.foresignal.doctor.StructureExtractor}, {@link ai.foresignal.doctor.StructureDiff}), runs deterministic diagnosis
 * rules ({@link ai.foresignal.doctor.DiagnosisRules}) and, when enabled, asks the {@code chat} LLM to explain the likely
 * change and propose a parser patch as text ({@link ai.foresignal.doctor.DoctorLlm}). Patches are validated in memory
 * and never written, compiled or applied. Everything except the explanation works without an LLM.
 *
 * <p>No raw upstream body is persisted: a {@code doctor_report} holds findings, the structure diff (paths, types and
 * capped examples), the probe summary and the LLM output.
 */
package ai.foresignal.doctor;
