/**
 * STIX 2.1 export and a read-only TAXII 2.1 server (prompt 141) over the cyber kinds of prompt 076.
 *
 * <ul>
 *   <li>{@link ai.foresignal.stix.StixMapper} maps records to STIX objects: {@code vulnerability} records
 *       ({@code cisa-kev}, {@code nvd-cves}) to {@code vulnerability} with a CVE external reference;
 *       {@code malware-url} (abuse.ch URLhaus) and {@code malware-c2} (abuse.ch Feodo Tracker) to {@code indicator}
 *       with a STIX pattern built from the IOC type; plus one {@code identity} for Foresignal as the creator.
 *   <li>{@link ai.foresignal.stix.StixIds} derives every id as a UUIDv5 of the record's object reference, so the same
 *       record always exports under the same id.
 *   <li>{@link ai.foresignal.stix.StixService} reads records through the normal query service and lets
 *       {@code EgressPolicy} decide every record on the {@code EXPORT} path (private sources, {@code LOCAL_ONLY}
 *       egress and license terms).
 *   <li>{@link ai.foresignal.stix.TaxiiCollection} names the two read-only TAXII collections.
 * </ul>
 */
package ai.foresignal.stix;
