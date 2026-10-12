package ai.foresignal.inbound.edp;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.InvalidInputException;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.JsonNode;

/** Batch-level checks of an EDP batch header (docs/External-Data-Protocol.md §3, §3.1). */
public final class EdpBatchChecks {

    private EdpBatchChecks() {}

    /**
     * A batch's {@code snapshot} member.
     *
     * @param id the snapshot id, shared by its pages
     * @param stream the snapshot stream
     * @param scope the scope value, or null for the whole stream
     * @param page the page number, from 1
     * @param finalPage true on the last page
     */
    public record EdpSnapshotRef(String id, String stream, String scope, int page, boolean finalPage) {}

    /**
     * Checks the header against the registered manifest.
     *
     * @param registeredSourceId the source the batch was sent to ({@code ext-…})
     * @throws ForesignalException 400 {@code VALIDATION_FAILED} without a valid protocol, 400
     *     {@code PROTOCOL_UNSUPPORTED} for a minor above the manifest's, 400 {@code SOURCE_MISMATCH}
     *     when {@code sourceId} names another source
     */
    public static void checkHeader(JsonNode header, EdpManifestModel manifest, String registeredSourceId) {
        JsonNode protocol = header == null ? null : header.get("protocol");
        int minor = EdpProtocol.minor(protocol != null && protocol.isString() ? protocol.asString() : null);
        if (minor < 0) {
            throw new InvalidInputException("The batch needs a protocol of the form foresignal-edp/1.N.");
        }
        if (minor > manifest.minor()) {
            throw new ForesignalException(HttpStatus.BAD_REQUEST, ErrorCodes.PROTOCOL_UNSUPPORTED,
                    "The batch uses foresignal-edp/1." + minor + ", newer than the registered manifest's 1."
                            + manifest.minor() + ".");
        }
        JsonNode sourceId = header.get("sourceId");
        if (sourceId != null && !sourceId.isNull()) {
            String value = sourceId.isString() ? sourceId.asString() : sourceId.toString();
            boolean matches = value.equals(manifest.providerId()) || value.equals(registeredSourceId)
                    || value.equals(EdpProtocol.sourceId(manifest.providerId()));
            if (!matches) {
                throw new ForesignalException(HttpStatus.BAD_REQUEST, ErrorCodes.SOURCE_MISMATCH,
                        "The batch names source '" + value + "' but was sent to " + registeredSourceId + ".");
            }
        }
    }

    /**
     * The batch's snapshot reference, or null when it is not a snapshot page.
     *
     * @throws InvalidInputException when {@code snapshot} is present but malformed
     */
    public static EdpSnapshotRef snapshot(JsonNode header) {
        JsonNode snapshot = header == null ? null : header.get("snapshot");
        if (snapshot == null || snapshot.isNull()) {
            return null;
        }
        if (!snapshot.isObject()) {
            throw new InvalidInputException("snapshot must be an object.");
        }
        String id = EdpManifestModel.text(snapshot, "id");
        String stream = EdpManifestModel.text(snapshot, "stream");
        JsonNode page = snapshot.get("page");
        JsonNode finalPage = snapshot.get("final");
        if (id == null || id.length() > 128 || stream == null || page == null || !page.isIntegralNumber()
                || page.asInt() < 1 || finalPage == null || !finalPage.isBoolean()) {
            throw new InvalidInputException("snapshot needs id (≤ 128 characters), stream, page ≥ 1 and final.");
        }
        return new EdpSnapshotRef(id, stream, EdpManifestModel.text(snapshot, "scope"), page.asInt(),
                finalPage.booleanValue());
    }
}
