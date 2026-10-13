/**
 * Certificate Transparency monitoring for operator-chosen domains ({@code ct-domain-watch}, prompt 076) through the
 * SSLMate Cert Spotter CT Search API v1.
 *
 * <p>crt.sh polling is rejected (Gap-Analysis §9) and Foresignal ships no CT log client: no adapter here contacts
 * crt.sh or a CT log. The self-hosted alternative (SSLMate's open-source {@code certspotter} monitor posting to a
 * custom PUSH source) is described in the Operator Guide.
 */
package ai.foresignal.source.adapters.ct;
