# 043-CR-Media-Store-And-Image-Guard

| | |
|---|---|
| Prompt | 043-Media-Store-And-Image-Guard.md |
| Status | Complete |
| Implementation commit | b015aaf8e9aa97aa2e6bfd57e2352592f7deaa1b |
| Date | 2026-10-12 |

## Summary

Foresignal now has a content-addressed media store for the images it derives or receives. Every image
passes `ImageGuard` before any pixel is decoded, and `GuardedImages` is the only place in
`src/main/java` that calls `ImageIO.read`. Media rows follow the lifetime of the record that owns them.
All seven tasks are done. The three 043 operations are `implemented` and off `pending-operations.txt`.

**1. Hooks.** Both already existed, so none were added:
- 019/042: the `RetentionStep` extension point and per-kind resolution (`RetentionResolver.window`).
- 018: `NotificationMessage.attachments` (`Attachment(filename, contentType, bytes, mediaId)`, empty
  by default).
- Teams already rejects `webhook.office.com` and `*.webhook.office.com`
  (`notify/types/TeamsChannel.java:78`), so the validator needed no fix.

**2. Entity** (`ai.foresignal.media`).
- `MediaEntity` / `media` has the §2.3 columns: `id` varchar(26), `sha256` char(64), `content_type`,
  `width`, `height`, `bytes`, `path`, `role`, `source_id`, `record_source_id`, `record_external_id`,
  `licence`, `created_at` and `expires_at`.
- **Added:** `variant_of` (nullable media id) and `variant_width` link a thumbnail to its original.
- Indexes: `sha256`, (`record_source_id`, `record_external_id`), `variant_of`, `expires_at`.
- Also added: `media_tombstone` (Deviation 2).
- The entities exist so Hibernate owns the DDL. All reads and writes are native SQL
  (`MediaQueries`), as 042 does for frames.

**3. ImageGuard.**
- `inspect(Path | InputStream | byte[], declaredContentType[, allowMultiImage])` returns
  `ImageInfo(format, width, height, bitDepth, hasAlpha)`, or throws `ImageRejectedException`, which
  carries a human `reason()` and a machine `tag()`.
- The checks run in this order:
  1. file size against `foresignal.media.max-file-bytes` (default 25 MB);
  2. the magic bytes (PNG, JPEG, TIFF II/MM). BigTIFF in either byte order is rejected with the exact
     reason `BigTIFF not supported`;
  3. the declared type must match the sniffed format (parameters and case are ignored);
  4. the header only, through `ImageIO.getImageReaders(ImageInputStream)` → `setInput(iis, false, true)`
     → `getWidth(0)` / `getHeight(0)`. `read()` is never called;
  5. more than 150 MP, then a side over 12,000 px;
  6. a TIFF with more than one IFD, unless the caller opts in. The IFDs are counted by walking the
     next-IFD offsets with loop detection, so no pixel data is read.
- Bit depth and alpha come from `getRawImageType(0)` after the size checks.
- `GuardedImages.decode(Path | byte[])` runs `inspect` and then `ImageIO.read`. 16-bit grayscale PNGs
  decode to `TYPE_USHORT_GRAY`.
- The metric is `foresignal.media.guard.rejected{reason}`.
- The API answers an `ImageRejectedException` with `400 VALIDATION_FAILED` and a `reason` property
  (in `GlobalExceptionHandler`).

**4. MediaStore, ThumbnailService, `ctx.media()`.**
- `MediaStore.store(MediaInput)`:
  - streams to `media/tmp/{ulid}.part`, hashing with SHA-256 as it writes. Over the cap it answers
    `413 MEDIA_TOO_LARGE`, checking the size up front when it is known and while streaming otherwise;
  - fsyncs, then runs the guard;
  - atomically renames to `media/{ab}/{sha256}.{ext}`, or reuses the file if it already exists;
  - always inserts a new row and returns `MediaRef(id, sha256, contentType, width, height, bytes)`;
  - any failure deletes the staged file.
  - The reuse/rename and the insert hold a per-SHA stripe lock (`MediaLocks`). Retention takes the same
    lock before unlinking.
- `ThumbnailService`:
  - Produces 256 px and 1024 px long-edge renditions with `ThumbnailRenderer`: progressive bilinear
    halving, then one bicubic step; JPEG at quality 0.85, or PNG when the source has alpha.
  - Stores them as `role=thumbnail` rows with `variant_of` and `variant_width`.
  - An original that is already within the target and in PNG or JPEG is not upscaled. A variant row
    links the original's file instead (same SHA).
  - `thumbnail(id, w)` is single-flight.
- `ctx.media()`:
  - `FetchContext.media()` is a default method that throws without a store. `StreamContext` inherits
    it.
  - `collect.MediaSinkFactory` is implemented by `media.MediaSinks`. That sink fills in the source id;
    a probe sink inspects and hashes but writes nothing (it returns a null id).
- Disk accounting: `/v1/system` `rasters.mediaBytes` (each distinct file counted once).
  - `diskQuotaPct`, the `RASTER_DISK_QUOTA` warning and `RasterRetentionStep.enforceQuota` all count
    frames plus media.
  - The quota still evicts only frames.

**5. `MediaRetentionStep`** (order 780: after the head purges and the region sweep, before rasters at
800; not `@Transactional`). It handles, in order:
1. rows with `expires_at < now`;
2. record-following originals:
   - deleted when `created_at` is older than `RetentionResolver.window(record source, head kind)`;
   - or, when the head is gone, deleted once older than `head-missing-grace` (24 h);
3. originals with neither a record nor an expiry: they follow the producing source's window
   (Deviation 3);
4. variants whose original is gone. Every delete also takes the row's variants
   (`DELETE … WHERE id IN (:ids) OR variant_of IN (:ids) RETURNING`);
5. files: a file is unlinked under the stripe lock only when no remaining row references its SHA. The
   row is deleted first;
6. the orphan sweep, on every hourly run:
   - removes rows with no file, files with no row and stale staging files, after the 10-minute
     `orphan-grace`;
   - counts them in `foresignal.media.orphans{type=row|file}`;
7. tombstones older than 30 days are pruned.

**6. API** (`api/MediaController`).
- `GET /v1/media/{id}`:
  - streams the file with its content type and `Content-Length`;
  - sends `ETag: "sha256"` and `Cache-Control: {public|private}, max-age=31536000, immutable`;
  - answers `If-None-Match` with 304.
- `GET /v1/media/{id}/thumbnail?w=256|1024`:
  - serves the stored variant, or renders it synchronously (single-flight);
  - any other, malformed or missing `w` is `400 VALIDATION_FAILED`.
- `GET /v1/media/{id}/meta` returns the `Media` document, including `recordRef`
  (`record:{source}/{percent-encoded id}`), `licence`, `variantOf` and `links`.
- `public` requires all three of the following; anything else is `private`:
  - `ReadCaller.publicCache()` (`openReads` on and an unrestricted caller);
  - the image's producing source is not private;
  - the image's owning source is not private.
- Unknown or malformed ids, and media of sources the caller may not read, are `404 UNKNOWN_MEDIA`.
  Expired rows, tombstoned ids and rows whose file is gone are `410 MEDIA_EXPIRED`.
- `ErrorCodes` gains `UNKNOWN_MEDIA`, `MEDIA_EXPIRED` and `MEDIA_TOO_LARGE`.

**7. Spec, policy, docs.**
- OpenAPI:
  - the three operations are `implemented`;
  - the problem-code registry gains the three codes;
  - `Problem.reason` is added;
  - there is a changelog entry.
- `AccessPolicy`:
  - `GET /v1/media/**` is READ;
  - `GET /v1/media/providers` and `/v1/media/providers/**` (133) are ADMIN, listed before it so the
    spec's planned admin scope holds.
- Postman was regenerated and is unchanged.
- `docs/Architecture.md`: §7 (`media`, `media_tombstone`) and §11 (media retention linkage).
- `application.yml` gains `foresignal.media.*`.

## Files created / modified

Production, created:
- `source/spi/`: `MediaRole`, `MediaInput`, `MediaRef`, `MediaSink`, `ImageRejectedException`.
- `collect/MediaSinkFactory`.
- `media/`:
  - `package-info`, `MediaProperties`, `ImageFormat`, `ImageInfo`, `ImageGuard`, `GuardedImages`;
  - `MediaPaths`, `MediaLocks`, `MediaEntity`, `MediaTombstoneEntity`, `MediaRow`, `MediaQueries`,
    `MediaErrors`;
  - `MediaStore`, `ThumbnailRenderer`, `ThumbnailService`, `MediaSinks`, `MediaRetentionStep`,
    `MediaService`.
- `api/MediaController`.

Production, modified:
- `source/spi/FetchContext` (`media()`).
- `collect/`: `DefaultFetchContext`, `FetchContextDefaults`, `FetchContextFactory`.
- `raster/`: `RasterSystemBlock` (`mediaBytes`, quota), `RasterRetentionStep` (quota counts media).
- `security/AccessPolicy`.
- `api/error/`: `ErrorCodes`, `GlobalExceptionHandler`.
- `application.yml`.

Docs and spec: `docs/OpenAPI.json`, `docs/Architecture.md`,
`src/test/resources/openapi/pending-operations.txt` (3 lines removed).

Tests: listed below, plus `collect/NoClientTriggeredFetchIT` (media paths added).

## Deviations from the prompt

1. **The adapter-facing types live in `ai.foresignal.source.spi`**: `MediaSink`, `MediaInput`,
   `MediaRef`, `MediaRole` and `ImageRejectedException`. Architecture §3 says adapters depend only on
   SPI types, and 042 made the same choice. The implementations are in `media`. `MediaInput` carries
   the record ref as `recordSourceId`/`recordExternalId` plus an `allowMultiImage` opt-in.
2. **A `media_tombstone` table is added.** "Expired or deleted → 410" cannot be told apart from
   "unknown → 404" once a row is gone. Retention therefore records each deleted id, keeping it for
   `foresignal.media.tombstone-retention` (30 d). After that a deleted id answers 404.
3. **Originals with neither a record ref nor an `expires_at`.** The prompt does not cover them. They
   follow the producing source's source-level window, or `retention.default` when there is no source,
   so nothing is kept forever.
4. **The 150 MP check runs before the side check.** At 12,000 px per side at most, an image has
   144 MP, so a 150 MP limit can only bite on an image whose side is already too long. Checking pixels
   first is what lets a 151 MP header be refused for its pixel count. The test uses 15,100 × 10,000
   (see Issue 1).
5. **Linking an original that is too small.** "The original is linked instead" is implemented as a
   `thumbnail` variant row that shares the original's file (same SHA, no new bytes), so the lookup is
   uniform. A small **TIFF** is still re-encoded to JPEG or PNG at its own size, because the spec's
   thumbnail responses are `image/jpeg` / `image/png` only.
6. **Visibility.** The prompt does not mention private sources. To keep EGR-1:
   - media whose producing or owning source the caller may not read answers `404 UNKNOWN_MEDIA`;
   - a private source's media is never `public`.
7. **`ImageRejectedException` maps to `400 VALIDATION_FAILED`** with a `reason` property. This follows
   the spec's 054 description ("image rejected by ImageGuard"). The handler was added now, so a guard
   refusal never surfaces as a 500. `Problem.reason` was added to the spec (additive, changelog).
8. **The orphan sweep runs inside the hourly `MediaRetentionStep`**, not on a separate scheduler, as
   042 did. It has a 10-minute `orphan-grace` for stores in flight.
9. **`StreamContext`** gets `media()` by inheriting from `FetchContext`, as `rasters()` does in 042.

## Issues found / open questions

**Needs the owner:**

1. **The 150 MP limit is unreachable as a separate rule.** With a 12,000 px side cap, the largest
   accepted image is 144 MP, so every image over 150 MP is also over 12,000 px on a side.
   - Both rules are kept: Deviation 4 orders the checks so the pixel reason is reported.
   - **Owner:** lower the pixel cap (for example to 100 MP), or drop it?
2. **Package name `media`** is still shared with news (133), as noted in the 042 report's issue 3.
   **Owner:** which one should be renamed?
3. **039's entity purge does not delete media.** A purged entity's media goes through the head-missing
   rule after 24 h. **Owner:** should `PurgeFanout` delete it at once?

**Other findings:**
- Not verified at run time; the new ITs first run at 057:
  - that `ImageIO`'s TIFF reader reports dimensions for a crafted IFD whose strip offset points past
    the end of the file;
  - that the JPEG reader reads a patched SOF0 without touching the scan data.
- No instructions directed at an agent appeared in any data, file or tool output.

## Tests written (not run)

The 043 integration tests are `@WebIntegrationTest` and import `RasterTestSources`, so they reuse the
042 context and add no new cached context.

| Test | What it covers |
|---|---|
| `media/ImageGuardTest` | Valid PNG (alpha, 16-bit gray), JPEG and TIFF facts; 12,001 px side (crafted PNG, TIFF and patched JPEG headers with no pixel data) and 151 MP (PNG, TIFF) refused under a 2 s preemptive timeout with no `Error`, and `GuardedImages` never reaching `ImageIO.read`; 12,000 × 12,000 header accepted; BigTIFF (LE/BE) with the exact reason and the counter; type mismatches and an unknown format; truncated PNG, TIFF and JPEG; oversized path, stream and bytes; multi-image TIFF with and without the opt-in; sniffing |
| `media/ThumbnailServiceTest` | 256/1024 sizes and aspect ratio; JPEG versus PNG-with-alpha; no upscaling and the link-or-render decision (small TIFF re-encoded); 16-bit grayscale input (brighter in, brighter out); the halving path; widths |
| `media/GuardedImagesOnlyDecodeEntryTest` | Scans `src/main/java`: `ImageIO.read` appears only in `GuardedImages` |
| `media/MediaStoreIT` | Dedupe (one file, two rows, bytes counted once); atomic rename (staging empty, path input copied); failed rename (read-only directory) leaves no file and no row; `ctx.media()` fills the source id, and a probe writes nothing; guard refusal leaves nothing; `413 MEDIA_TOO_LARGE`; rendered and linked thumbnails, reused on repeat; TIFF thumbnails |
| `media/MediaRetentionStepIT` | `expires_at` deletion with tombstone; record-following by the (source, kind) window with thumbnails going along; head missing after 24 h; shared file survives until its last row goes; unowned rows follow the source window; orphan sweep (file, row, staging, grace, metrics, nothing orphaned afterwards); tombstone pruning |
| `media/MediaApiIT` | Body, `Content-Type`, `Content-Length`, strong ETag and `public, max-age=31536000, immutable`; 304; `openReads=false` gives 401 without a token and `private` with a READ token; private source answers 404; lazy single-flight thumbnail (8 concurrent requests, one render, one row, 304, the 1024 link); 400 widths; 404 and 410 (expired and tombstoned); `Media` schema validation for an original and a thumbnail |
| `collect/NoClientTriggeredFetchIT` (paths added) | SRC-6 over the three media paths |

## Compile result

- `./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`.
- `./gradlew lintOpenApi` → valid, 71 warnings (unchanged).
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman` → 829 requests, covers all
  operations. The collection is unchanged.
- No new dependencies.

## Notes for the next prompt

1. **049/054 (SatDump):**
   - store composites through `ctx.media()` (`MediaInput.of(path, "image/png", MediaRole.IMAGE)
     .withRecord(sourceId, passExternalId).withLicence(…)`);
   - put `ctx.media().thumbnails(ref).get(0).id()` in `products[].thumbMediaId`;
   - decode any inbox image only through `GuardedImages`.
2. **054 (remote ingest):**
   - `POST /v1/ingest/{sourceId}/media` can call `MediaStore.store`; it already answers
     `413 MEDIA_TOO_LARGE`, and `ImageRejectedException` → `400 VALIDATION_FAILED`;
   - pass `allowingMultiImage()` only where a multi-page TIFF is expected.
3. **055 (static maps, attachments):**
   - store map PNGs with `MediaRole.MAP` and `withExpiresAt(now + 7d)`;
   - fill `NotificationMessage.Attachment.mediaId`; channels read the bytes through `MediaStore.file`.
4. **057 checkpoint:**
   - run the 043 tests;
   - they share the `RasterTestSources` context and clear `media/` in `@BeforeEach`.
5. **144 (Flyway):** baseline the following.
   - Tables: `media`, `media_tombstone`.
   - Indexes: `media_sha256_idx`, `media_record_idx`, `media_variant_of_idx`, `media_expires_at_idx`.
6. **Owner decisions pending:** Issues 1–3 above, plus those carried from 012–042.
