/**
 * The machine-readable mesh message codec (prompt 084): a CBOR map with small integer keys, at most 160 bytes, so it
 * fits one MeshCore channel datagram or, base64url-encoded behind {@code ~E1 }, one Meshtastic text. Prompt 113
 * reuses and extends it for MeshDTN messages; the decoder ignores unknown integer keys for forward compatibility.
 */
package ai.foresignal.notify.mesh.cbor;
