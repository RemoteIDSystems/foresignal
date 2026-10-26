package ai.foresignal.api;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.backup.BackupRun;
import ai.foresignal.backup.BackupRunStore;
import ai.foresignal.backup.BackupService;
import ai.foresignal.backup.BackupSettings;
import ai.foresignal.backup.BackupSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * Backup & restore (OpenAPI tag {@code Backups}, prompt R2-017; admin, {@code storage.manage}):
 * {@code getBackupSettings}, {@code updateBackupSettings}, {@code listBackups}, {@code runBackup} and
 * {@code restoreBackup}.
 */
@RestController
@Tag(name = "Backups")
public class BackupsController {

    /** The labels' longest length. */
    static final int MAX_LABEL = 200;

    private final BackupSettingsService settings;
    private final BackupRunStore runs;
    private final BackupService backups;

    /** Creates the controller. */
    public BackupsController(BackupSettingsService settings, BackupRunStore runs, BackupService backups) {
        this.settings = settings;
        this.runs = runs;
        this.backups = backups;
    }

    /** {@code getBackupSettings}: the settings without their secrets. */
    @GetMapping("/v1/settings/backup")
    @Operation(operationId = "getBackupSettings", tags = "Backups")
    public Map<String, Object> getBackupSettings() {
        return settings.view(settings.get(), runs.lastCreatedAt());
    }

    /** {@code updateBackupSettings}: replaces the settings; an omitted secret keeps the stored one. */
    @PutMapping("/v1/settings/backup")
    @Operation(operationId = "updateBackupSettings", tags = "Backups")
    public Map<String, Object> updateBackupSettings(@RequestBody(required = false) JsonNode body) {
        BackupSettings updated = settings.update(body);
        return settings.view(updated, runs.lastCreatedAt());
    }

    /** {@code listBackups}: the history, newest first, optionally of one type. */
    @GetMapping("/v1/backups")
    @Operation(operationId = "listBackups", tags = "Backups")
    public Map<String, Object> listBackups(@RequestParam(required = false) String type,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) String limit) {
        if (type != null && !BackupRun.TYPES.contains(type)) {
            throw ValidationException.forField("type", "must be one of " + String.join(", ", BackupRun.TYPES));
        }
        BackupRunStore.Page page;
        try {
            page = runs.page(type, cursor == null || cursor.isBlank() ? null : cursor,
                    BulkExportsController.parseLimit(limit));
        } catch (IllegalArgumentException e) {
            throw ValidationException.forField("cursor", e.getMessage());
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", page.items().stream().map(BackupsController::view).toList());
        out.put("nextCursor", page.nextCursor());
        return out;
    }

    /** {@code runBackup}: starts a backup now (202 with the {@code pending} backup). */
    @PostMapping("/v1/backups/run")
    @Operation(operationId = "runBackup", tags = "Backups")
    public ResponseEntity<Map<String, Object>> runBackup(@RequestBody(required = false) JsonNode body) {
        RunRequest request = request(body);
        BackupRun run = backups.start(request.type(), request.label(), BackupRun.MANUAL);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(view(run));
    }

    /**
     * {@code restoreBackup}: {@code confirm: true} is required. A config backup is imported at once (the backup
     * carries {@code importReport}); a database or full backup is restored on the worker ({@code restoring}).
     */
    @PostMapping("/v1/backups/{id}/restore")
    @Operation(operationId = "restoreBackup", tags = "Backups")
    public ResponseEntity<Map<String, Object>> restoreBackup(@PathVariable String id,
            @RequestBody(required = false) JsonNode body) {
        if (body == null || !body.isObject() || !body.has("confirm") || !body.get("confirm").isBoolean()
                || !body.get("confirm").asBoolean()) {
            throw ValidationException.forField("confirm", "must be true: a restore replaces the current data");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String name : body.propertyNames()) {
            if (!name.equals("confirm")) {
                errors.add(new FieldError(name, "is not a member of BackupRestoreRequest"));
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The restore request is not valid.", errors);
        }
        BackupService.Restore restore = backups.restore(id);
        Map<String, Object> out = view(restore.run());
        if (restore.importReport() != null) {
            out.put("importReport", restore.importReport());
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(out);
    }

    /**
     * A {@code BackupRunRequest}.
     *
     * @param type the type, or null for the settings' default
     * @param label the label, or null
     */
    record RunRequest(String type, String label) {}

    /** Parses a {@code BackupRunRequest}; every problem in one 400. */
    static RunRequest request(JsonNode body) {
        if (body == null || body.isNull()) {
            return new RunRequest(null, null);
        }
        if (!body.isObject()) {
            throw new ValidationException("The request body must be a BackupRunRequest object.");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String name : body.propertyNames()) {
            if (!name.equals("type") && !name.equals("label")) {
                errors.add(new FieldError(name, "is not a member of BackupRunRequest"));
            }
        }
        String type = null;
        JsonNode typeNode = body.get("type");
        if (typeNode != null && !typeNode.isNull()) {
            if (!typeNode.isString() || !BackupRun.TYPES.contains(typeNode.asString())) {
                errors.add(new FieldError("type", "must be one of " + String.join(", ", BackupRun.TYPES)));
            } else {
                type = typeNode.asString();
            }
        }
        String label = null;
        JsonNode labelNode = body.get("label");
        if (labelNode != null && !labelNode.isNull()) {
            if (!labelNode.isString()) {
                errors.add(new FieldError("label", "must be a string"));
            } else if (labelNode.asString().length() > MAX_LABEL) {
                errors.add(new FieldError("label", "must be at most " + MAX_LABEL + " characters"));
            } else {
                label = labelNode.asString().isBlank() ? null : labelNode.asString().strip();
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The backup run request is not valid.", errors);
        }
        return new RunRequest(type, label);
    }

    /** The {@code Backup} view of a row. */
    static Map<String, Object> view(BackupRun run) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", run.id());
        out.put("type", run.type());
        out.put("status", run.status());
        out.put("createdAt", run.createdAt());
        out.put("sizeBytes", run.sizeBytes());
        out.put("target", run.target());
        out.put("location", run.location());
        out.put("checksum", run.checksum());
        out.put("label", run.label());
        out.put("trigger", run.trigger());
        out.put("files", run.files().stream().map(BackupsController::file).toList());
        out.put("error", run.error());
        out.put("startedAt", run.startedAt());
        out.put("finishedAt", run.finishedAt());
        out.put("restoredAt", run.restoredAt());
        return out;
    }

    private static Map<String, Object> file(BackupRun.BackupFile file) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("name", file.name());
        out.put("location", file.location());
        out.put("bytes", file.bytes());
        out.put("sha256", file.sha256());
        return out;
    }
}
