package ai.foresignal.export;

import ai.foresignal.provenance.ManifestSigner;
import ai.foresignal.provenance.SigningKeyService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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
 * Writes an export's {@code manifest.json}, its detached JWS, {@code licence.json} and
 * {@code ATTRIBUTION.txt} (prompt 111, 109). The signed manifest is the canonical record of the
 * export's contents and rights.
 */
@Component
public class ExportManifests {

    /** The signing algorithm name carried in the manifest. */
    public static final String ALG = "EdDSA";

    /** One file of an export. */
    public record ExportFile(String name, String mediaType, long bytes, String sha256, long rows) {

        /** The JSON shape of a manifest file entry. */
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
    private final AppSettingService settings;

    /** Creates the writer. */
    public ExportManifests(JsonMapper jsonMapper, ManifestSigner signer, SigningKeyService keys,
            AppSettingService settings) {
        this.jsonMapper = jsonMapper;
        this.signer = signer;
        this.keys = keys;
        this.settings = settings;
    }

    /** Writes the sidecars and returns the manifest's SHA-256. */
    public String write(Path dir, ExportJobEntity job, List<ExportFile> files, long watermarkSeq, Long atSeq,
            Map<String, Object> licenceSummary, List<String> attributions) {
        try {
            Files.createDirectories(dir);
            Map<String, Object> manifest = new LinkedHashMap<>();
            manifest.put("manifestVersion", 1);
            manifest.put("id", job.getId());
            manifest.put("kind", job.getKind());
            manifest.put("format", job.getFormat());
            manifest.put("instanceId", settings.getOrDefault(SettingKeys.INSTALL_ID, String.class, () -> "unknown"));
            manifest.put("createdAt", job.getCreatedAt().toString());
            manifest.put("watermarkSeq", watermarkSeq);
            manifest.put("atSeq", atSeq);
            manifest.put("spec", job.getSpec());
            manifest.put("rowCount", job.getRowCount());
            List<Map<String, Object>> fileMaps = new ArrayList<>();
            for (ExportFile file : files) {
                fileMaps.add(file.toMap());
            }
            manifest.put("files", fileMaps);
            manifest.put("licence", licenceSummary);
            manifest.put("personalUse", job.isPersonalUse());
            Map<String, Object> signature = new LinkedHashMap<>();
            signature.put("alg", ALG);
            signature.put("kid", keys.activeKid());
            signature.put("file", "manifest.json.jws");
            manifest.put("signature", signature);
            byte[] bytes = jsonMapper.writeValueAsBytes(manifest);
            Files.write(dir.resolve("manifest.json"), bytes, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            Files.writeString(dir.resolve("manifest.json.jws"), signer.sign(bytes), StandardCharsets.UTF_8);
            Files.write(dir.resolve("licence.json"), jsonMapper.writeValueAsBytes(licenceSummary),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            StringBuilder attribution = new StringBuilder();
            for (String line : attributions) {
                attribution.append(line).append('\n');
            }
            if (job.isPersonalUse()) {
                attribution.append("Personal use only — do not redistribute.\n");
            }
            Files.writeString(dir.resolve("ATTRIBUTION.txt"), attribution.toString(), StandardCharsets.UTF_8);
            return sha256(bytes);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write the export manifest", e);
        }
    }

    /** The SHA-256 of a byte array, lowercase hex. */
    public static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    /** The SHA-256 of a file, lowercase hex. */
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
