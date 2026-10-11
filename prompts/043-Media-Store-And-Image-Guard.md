# 043 — Media Store And Image Guard

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/043-CR-Media-Store-And-Image-Guard.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–138).
- **Previous Completion Report:** read `completion-reports/042-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Weather-Imagery-Design.md` §2.3 (`media` table), §2.4 (storage layout; "Media: follows the owning record; map attachments 7 d"), §3 (Image guard row, licence boundaries), §4.6 (thumbnails and media), §6.1 (cache policy), §6.6 (media endpoints), §6.11 (AccessPolicy, problem codes), §8 (attachments use media). Design prompt 030 = this prompt; its "042"/"045" static maps are v1.2 **055**, its "043"/"041" polar overlays are **054** (Roadmap §5).
- `docs/Core-Data-Model.md` §13 (retention windows, head purge)
- Completion Reports 018, 019 and 042

## Objective

Add a content-addressed media store for images Foresignal derives or receives: SatDump composites and thumbnails, static maps and notification attachments. Every image passes `ImageGuard` before any pixel is decoded, so a hostile or corrupt file in an inbox or an ingest body cannot exhaust memory. Media rows follow the lifetime of the record that owns them.

## Tasks

### 1. Hooks (check, add if missing)

- **019:** the RetentionJob `RetentionStep` extension point and per-kind retention resolution (042 added them if 019 had not). This prompt registers `MediaRetentionStep` on it.
- **018:** `NotificationMessage.attachments: List<Attachment(filename, contentType, bytes, mediaId)>` (empty by default). If 018 did not add the field, add it now with no channel using it; 055 fills it. Teams must be implemented only through Power Automate Workflows URLs, rejecting `webhook.office.com` with a validation error; if 018 still accepts Office 365 connector URLs, fix the validator here and note it in the Completion Report.

### 2. Entity (`ai.foresignal.media`)

`MediaEntity` / table `media`, exactly as §2.3 plus one column:
- `id` (ULID, varchar 26), `sha256` (char 64, indexed), `content_type`, `width`, `height`, `bytes`, `path`
- `role` (image, thumbnail, projected, map)
- `source_id`, `record_source_id`, `record_external_id` (index on the pair), `licence`
- `created_at`, `expires_at` (nullable; null means "follows the owning record")
- **`variant_of`** (nullable media id): links a thumbnail to its original, with `variant_width`. Record this addition in the Completion Report.

### 3. ImageGuard

- `ImageGuard.inspect(Path | InputStream, declaredContentType)` returns `ImageInfo(format, width, height, bitDepth, hasAlpha)` or throws `ImageRejectedException(reason)`.
- Sniff magic bytes (PNG `89 50 4E 47`, JPEG `FF D8 FF`, TIFF `II*\0` / `MM\0*`; BigTIFF `II+\0` is rejected with reason "BigTIFF not supported"). The declared content type must agree with the sniffed format.
- Read dimensions from the header only (`ImageIO.getImageReaders(ImageInputStream)` → `reader.getWidth(0)` / `getHeight(0)` with `ignoreMetadata=true`; never `read()`).
- Reject anything over **12,000 px on a side or 150 megapixels**, and files over `foresignal.media.max-file-bytes` (default 25 MB), **before** decoding. Also reject multi-image TIFFs beyond the first IFD unless the caller opts in.
- Applies to every PNG, TIFF and JPEG from the inbox or any ingest path. Expose a `GuardedImages.decode(Path)` helper that runs `inspect` and then decodes with `ImageIO` (16-bit grayscale PNG supported), so callers cannot skip the guard.
- Metrics `foresignal.media.guard.rejected{reason}`.

### 4. MediaStore and ThumbnailService

- `MediaStore.store(MediaInput)`: `MediaInput(Path or bytes, contentType, role, sourceId, recordRef?, licence, expiresAt?)`.
  - Stream to `${FORESIGNAL_DATA_DIR}/media/tmp`, hash sha256 while writing, run `ImageGuard`, then atomic-rename to `media/{ab}/{sha256}.{ext}` where `ab` is the first two hex characters. If the file exists, reuse it (content addressing); always insert a new row.
  - Returns `MediaRef(id, sha256, contentType, width, height, bytes)`.
- `ThumbnailService.thumbnails(MediaRef)`: a **256 px and a 1024 px** long-edge rendition, JPEG quality 0.85, or PNG when the source has alpha. Downscale by progressive halving with bilinear interpolation, then one final bicubic step. Stored as `role=thumbnail` rows with `variant_of`. Images already smaller than the target are not upscaled; the original is linked instead.
- `ctx.media()`: add `MediaSink media()` to `FetchContext` and `StreamContext`, delegating to `MediaStore` with the source id filled in. Adapters never write files themselves.
- Disk accounting: media bytes appear in the `/v1/system` `rasters` block as `mediaBytes` and count against `foresignal.raster.disk-quota-gb`.

### 5. Retention linkage (`MediaRetentionStep`)

- Delete rows whose `expires_at < now` (map attachments are stored with `expires_at = now + 7d` by 055).
- Rows with null `expires_at` follow the owning record: delete when `created_at` is older than the effective retention window of the owning record's (source, kind), resolved by the 019 per-kind order, **or** when the owning record's head no longer exists and the row is older than 24 h.
- Thumbnails go with their original (`variant_of`).
- A file is unlinked only when no remaining row references its sha256 (row delete first, then unlink).
- Hourly orphan sweep: files with no row are removed; rows with no file are deleted and counted (`foresignal.media.orphans{type}`).

### 6. API

- `GET /v1/media/{id}` (read): streams the file with its content type, `Content-Length`, a strong `ETag` = `"sha256"`, and `Cache-Control: {public|private}, max-age=31536000, immutable` (content-addressed). `public` only when `security.openReads` covers `/v1/media/**`. Honour `If-None-Match` → 304. Unknown id → 404 `UNKNOWN_MEDIA`; expired or deleted → 410 `MEDIA_EXPIRED`.
- `GET /v1/media/{id}/thumbnail?w=256|1024` (read): serves the stored variant; if none exists yet, generates it synchronously from the original (once, single-flight) and stores it. Other widths → 400 `VALIDATION_FAILED`.
- `GET /v1/media/{id}/meta` (read): the row as JSON (`Media` schema) including record ref and licence.
- No upload endpoint here: `POST /v1/ingest/{sourceId}/media` belongs to 054 (25 MB per file, 413 `MEDIA_TOO_LARGE`). Add `MEDIA_TOO_LARGE` 413 to `ErrorCodes` now, because `MediaStore` throws it when a stream exceeds the cap.

### 7. Spec, Postman, docs

- `docs/OpenAPI.json`: tag `Media`; operations `getMedia` (`image/png`, `image/jpeg`, `image/tiff` responses), `getMediaThumbnail`, `getMediaMeta`, each `"x-foresignal-scope": "read"`; schemas `Media`, `Attachment`; problem codes `UNKNOWN_MEDIA` 404, `MEDIA_EXPIRED` 410, `MEDIA_TOO_LARGE` 413. Regenerate Postman with `python3 scripts/generate-postman.py docs/OpenAPI.json postman`.
- `AccessPolicy`: `GET /v1/media/**` → READ.
- `docs/Architecture.md` §7 (`media` table), §11 (media retention linkage).

## Tests to write (do not run)

- `ImageGuardTest`: valid PNG/JPEG/TIFF; 12,001 px side and 151 MP headers rejected **without decoding** (use crafted headers whose pixel data is absent, and assert no `OutOfMemoryError` and fast return); BigTIFF rejected with the exact reason; magic/content-type mismatch; truncated header; oversized file.
- `MediaStoreIT`: dedupe on identical content (one file, two rows); atomic rename; `ctx.media()` fills the source id; guard failure leaves no file behind.
- `ThumbnailServiceTest`: sizes, JPEG vs PNG-with-alpha choice, no upscaling, 16-bit grayscale input.
- `MediaRetentionStepIT`: `expires_at` deletion; record-following deletion by kind retention; head-missing deletion after 24 h; shared file survives while another row references it; orphan sweep.
- `MediaApiIT`: immutable caching headers, `public` vs `private`, 304 on `If-None-Match`, lazy thumbnail single-flight, 404/410 codes, READ scope enforced when `openReads=false`.

## Acceptance criteria

- No code path decodes image pixels without passing `ImageGuard` first (`GuardedImages` is the only decode entry point; a test scans `src/main/java` for direct `ImageIO.read` calls outside it).
- Media follows the owning record's retention; no orphan rows or files after a sweep.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 138, 141, 142, 143).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 143).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `043: Media Store And Image Guard`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/043-CR-Media-Store-And-Image-Guard.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `043: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
