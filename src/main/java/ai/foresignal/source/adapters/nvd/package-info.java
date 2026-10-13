/**
 * NVD CVEs with EPSS exploit-probability bands ({@code nvd-cves}, prompt 076): {@code vulnerability} records from
 * the NVD CVE API 2.0, filtered by CVSS score and CPE vendor, and {@code cve-epss} records holding FIRST.org EPSS
 * scores as bands only, so daily score jitter never creates a version.
 *
 * <p>"This product uses the NVD API but is not endorsed or certified by the NVD." EPSS scores from FIRST.org
 * (first.org/epss).
 */
package ai.foresignal.source.adapters.nvd;
