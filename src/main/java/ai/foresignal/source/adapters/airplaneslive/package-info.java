/**
 * The airplanes.live aircraft source (prompt 080): {@code airplaneslive-mil}, aircraft flagged military,
 * worldwide.
 *
 * <p><strong>Access:</strong> verified 2026-10-13, an anonymous {@code GET https://api.airplanes.live/v2/mil}
 * answers HTTP 403 with {@code {"error": "Please contact us at contact@airplanes.live. Your email MUST include any
 * links, a description of the project, and any information you deem appropriate."}}. The descriptor sets
 * {@code accessDeniedIsTerminal} and the refusal reason "access approval required (contact airplanes.live)", so
 * the source fails with that reason and is retried hourly, never faster, and never with other headers. No key or
 * header mechanism is published (the API guide page is behind a browser challenge), so the source has no
 * credential; {@code adsbfi-mil} is the keyless alternative.
 *
 * <p>Terms (from the prompt, verified by the planner 2026-10): non-commercial, no SLA, attribution
 * "airplanes.live", one request per second: {@code hostPacing} 1.1 s, budget 2/min and 2,880/day. The source is
 * {@code LOCAL_ONLY} by default and disabled.
 */
package ai.foresignal.source.adapters.airplaneslive;
