package ai.foresignal.export;

/**
 * One file held by an {@link ArchiveTier} (prompt R2-016): what {@link ArchiveTier#offload} returns and what the
 * archive manifest records per file.
 *
 * @param target the tier ({@code local}, {@code s3} or {@code glacier})
 * @param key the file's key in the tier, relative ({@code segments/{archiveId}/{name}})
 * @param location the absolute path ({@code local}) or {@code s3://bucket/key} of the stored copy
 * @param sha256 the SHA-256 the copy must have, hex
 * @param bytes the size the copy must have
 * @param etag the object's ETag (object storage), or null
 */
public record ArchiveRef(String target, String key, String location, String sha256, long bytes, String etag) {}
