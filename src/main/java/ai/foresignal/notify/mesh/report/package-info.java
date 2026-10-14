/**
 * Field reports from the operator's own mesh (prompt 084): the {@code #E} command grammar and the service that reads
 * {@code MeshMessageBus}, accepts commands only from allowlisted senders on a private channel that has
 * {@code reportsEnabled}, records reports in the private {@code mesh-reports} source, acknowledges alerts and replies
 * through the airtime governor. Report text is untrusted data: never embedded, quoted only as data.
 */
package ai.foresignal.notify.mesh.report;
