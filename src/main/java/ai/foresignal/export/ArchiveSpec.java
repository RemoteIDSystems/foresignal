package ai.foresignal.export;

import java.nio.file.Path;

/**
 * One file to offload to an {@link ArchiveTier} (prompt R2-016).
 *
 * @param key the file's key in the tier, relative ({@code segments/{archiveId}/{name}})
 * @param file the local file, already written, fsynced and closed
 * @param sha256 the file's SHA-256, hex
 * @param bytes the file's size
 */
public record ArchiveSpec(String key, Path file, String sha256, long bytes) {}
