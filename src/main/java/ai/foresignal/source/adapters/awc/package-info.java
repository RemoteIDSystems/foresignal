/**
 * The Aviation Weather Center sources: METARs ({@code awc-metars}, prompt 047, the only METAR source) and, from prompt
 * 087, SIGMETs and AIRMETs ({@code awc-sigmets}), G-AIRMETs ({@code awc-gairmets}), Center Weather Advisories
 * ({@code awc-cwas}), pilot reports ({@code awc-pireps}) and TAFs ({@code awc-tafs}). {@link
 * ai.foresignal.source.adapters.awc.AwcSupport} holds what they share (the station scope and allowlist, the cache-file
 * GET, AWC's 100 requests/minute limit).
 */
package ai.foresignal.source.adapters.awc;
