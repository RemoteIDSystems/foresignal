package ai.foresignal.export;

import ai.foresignal.provenance.ManifestSigner;
import ai.foresignal.provenance.SigningKeyService;
import ai.foresignal.security.InstallIdService;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Writes the sidecars of an export, snapshot or pack (prompt 111, 109): {@code manifest.json} (the
 * {@code ExportManifest}: every data file with its SHA-256, the spec, the watermark and the license
 * summary), its detached Ed25519 JWS {@code manifest.json.jws} ({@link ManifestSigner}, verifiable
 * against {@code /.well-known/jwks.json}), {@code licence.json} (the {@code ExportLicenceSummary})
 * and {@code ATTRIBUTION.txt} (one attribution per line, plus the personal-use notice when it
 * applies).
 */
@Component
public class ExportManifests {

    /** The signing algorithm named in the manifest. */
    public static final String ALG = "EdDSA";

    /** The manifest file. */
    public static final String MANIFEST = "manifest.json";

    /** The detached JWS of the manifest. */
    public static final String SIGNATURE = "manifest.json.jws";

    /** The license sidecar. */
    public static final String LICENSE = "licence.json";

    /** The attribution sidecar. */
    public static final String ATTRIBUTION = "ATTRIBUTION.txt";

    /** The sidecar names, which are never data files. */
    public static final List<String> SIDECARS = List.of(MANIFEST, SIGNATURE, LICENSE, ATTRIBUTION);

    /**
     * One data file of a job.
     *
     * @param name the file name
     * @param mediaType its media type
     * @param bytes its size
     * @param sha256 its SHA-256, lowercase hex
     * @param rows the rows (or tiles) it holds
     */
    public record ExportFile(String name, String mediaType, long bytes, String sha256, long rows) {

        /** The manifest entry. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("name", name);
            out.put("mediaType", mediaType);
            out.put("bytes", bytes);
            out.put("sha256", sha256);
            out.put("rows", rows);
            return out;
        }
    }

    private final JsonMapper jsonMapper;
    private final ManifestSigner signer;
    private final SigningKeyService keys;
    private final InstallIdService installId;

    /** Creates the writer. */
    public ExportManifests(JsonMapper jsonMapper, ManifestSigner signer, SigningKeyService keys,
            InstallIdService installId) {
        this.jsonMapper = jsonMapper;
        this.signer = signer;
        this.keys = keys;
        this.installId = installId;
    }

    /**
     * Writes the four sidecars into {@code dir}.
     *
     * @return the SHA-256 of {@code manifest.json}
     */
    public String write(Path dir, ExportJobEntity job, List<ExportFile> files, Long watermarkSeq, Long atSeq,
            Map<String, Object> licenceSummary) {
        try {
            Files.createDirectories(dir);
            Map<String, Object> license = licenceSummary == null ? Map.of() : licenceSummary;
            Map<String, Object> manifest = new LinkedHashMap<>();
            manifest.put("manifestVersion", 1);
            manifest.put("id", job.getId());
            manifest.put("kind", job.getKind());
            manifest.put("format", job.getFormat());
            manifest.put("instanceId", installId.installId());
            manifest.put("createdAt", job.getCreatedAt().toString());
            manifest.put("watermarkSeq", watermarkSeq);
            manifest.put("atSeq", atSeq);
            manifest.put("spec", job.getSpec());
            manifest.put("rowCount", job.getRowCount() == null ? 0L : job.getRowCount());
            List<Map<String, Object>> entries = new ArrayList<>();
            for (ExportFile file : files) {
                entries.add(file.toMap());
            }
            manifest.put("files", entries);
            manifest.put("licence", license);
            manifest.put("personalUse", job.isPersonalUse());
            Map<String, Object> signature = new LinkedHashMap<>();
            signature.put("alg", ALG);
            signature.put("kid", keys.activeKid());
            signature.put("file", SIGNATURE);
            manifest.put("signature", signature);
            byte[] bytes = jsonMapper.writeValueAsBytes(manifest);
            Files.write(dir.resolve(MANIFEST), bytes);
            Files.writeString(dir.resolve(SIGNATURE), signer.sign(bytes), StandardCharsets.UTF_8);
            Files.write(dir.resolve(LICENSE), jsonMapper.writeValueAsBytes(license));
            Files.writeString(dir.resolve(ATTRIBUTION), attributionText(license), StandardCharsets.UTF_8);
            return sha256(bytes);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write the export manifest", e);
        }
    }

    /** The {@code ATTRIBUTION.txt} text of a license summary. */
    static String attributionText(Map<String, Object> license) {
        StringBuilder text = new StringBuilder();
        for (String line : ExportLicenceService.attributionLines(license)) {
            text.append(line).append('\n');
        }
        if (Boolean.TRUE.equals(license.get("personalUseOnly"))) {
            text.append(ExportLicenceService.PERSONAL_USE_NOTICE);
            if (license.get("personalUseSources") instanceof List<?> sources && !sources.isEmpty()) {
                text.append(" Sources included for personal use: ");
                List<String> names = new ArrayList<>();
                sources.forEach(s -> names.add(String.valueOf(s)));
                text.append(String.join(", ", names)).append('.');
            }
            text.append('\n');
        }
        return text.toString();
    }

    /** The SHA-256 of {@code bytes}, lowercase hex. */
    public static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    /** The SHA-256 of {@code file}, lowercase hex, read in 64 KiB blocks. */
    public static String sha256(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (var in = Files.newInputStream(file)) {
                byte[] buffer = new byte[1 << 16];
                int read;
                while ((read = in.read(buffer)) >= 0) {
                    digest.update(buffer, 0, read);
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }
}
