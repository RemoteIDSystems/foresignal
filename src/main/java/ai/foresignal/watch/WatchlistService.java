package ai.foresignal.watch;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.store.query.PgArrays;
import ai.foresignal.trigger.TriggerRulesChangedEvent;
import java.nio.charset.StandardCharsets;
import java.sql.Array;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Watchlists (prompt 036, task 4): lists of identifiers (ICAO 24-bit addresses, registrations,
 * callsigns, MMSIs, IMO numbers or custom values) that rules, views and filters match with the
 * {@code inWatchlist} operator.
 *
 * <ul>
 *   <li>Values are validated per identity ({@link Identity#invalid}) after the list's normalization,
 *       and stored normalized, unique per list.
 *   <li>A list holds at most {@value #MAX_ITEMS} items ({@code 400 WATCHLIST_TOO_LARGE}).
 *   <li>Imports take CSV with the header {@code value[,label,notes,tags]} (tags separated by
 *       {@code ;}) or a JSON array of items, at most 5 MB, in {@code merge} or {@code replace} mode; a
 *       dry run reports what would change and changes nothing. Exports write the same CSV.
 *   <li>Every change publishes {@link WatchlistChangedEvent} after commit; list creation, deletion and
 *       renormalization also publish {@link TriggerRulesChangedEvent}, because rules resolve identity
 *       paths from the list's identity. Edits converge on each entity's next observation; nothing
 *       fires retroactively.
 *   <li>Every REST write is audited by the token filter (prompt 023), every MCP call by the MCP
 *       registry.
 * </ul>
 */
@Service
public class WatchlistService {

    /** The most items of one list. */
    public static final int MAX_ITEMS = 100_000;

    /** The largest import body. */
    public static final int MAX_IMPORT_BYTES = 5 * 1024 * 1024;

    /** The CSV header of exports (and the full import header). */
    public static final String CSV_HEADER = "value,label,notes,tags";

    static final Set<String> MEMBERS = Set.of("name", "identity", "normalize", "description");

    static final Set<String> OUTPUT_ONLY = Set.of("id", "itemCount", "createdAt", "updatedAt");

    static final Set<String> ITEM_MEMBERS = Set.of("value", "label", "notes", "tags", "addedAt", "origin");

    private static final JsonMapper JSON = JsonMapper.builder().build();

    static final String UPSERT_ITEM = "INSERT INTO watch_list_item (list_id, value, label, notes, tags, added_at, origin)"
            + " VALUES (?, ?, ?, ?, CAST(? AS text[]), ?, ?) ON CONFLICT (list_id, value) DO UPDATE SET"
            + " label = EXCLUDED.label, notes = EXCLUDED.notes, tags = EXCLUDED.tags";

    private final WatchListRepository lists;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    /** Creates the service. */
    public WatchlistService(WatchListRepository lists, JdbcTemplate jdbc, TransactionTemplate transactions,
            ApplicationEventPublisher events, Clock clock) {
        this.lists = lists;
        this.jdbc = jdbc;
        this.transactions = transactions;
        this.events = events;
        this.clock = clock;
    }

    // ------------------------------------------------------------------------- lists

    /**
     * A page of lists.
     *
     * @param items the lists
     * @param nextCursor the next page's cursor, or null
     */
    public record Page(List<WatchListEntity> items, String nextCursor) {}

    /** Every list, by id. */
    public List<WatchListEntity> all() {
        List<WatchListEntity> out = new ArrayList<>(lists.findAll());
        out.sort((a, b) -> a.getId().toString().compareTo(b.getId().toString()));
        return out;
    }

    /** A page of lists, keyset by id. */
    public Page list(String cursor, int limit) {
        String after = decode(cursor, "w:");
        List<WatchListEntity> matching = all().stream()
                .filter(l -> after == null || l.getId().toString().compareTo(after) > 0)
                .toList();
        List<WatchListEntity> page = matching.size() > limit ? matching.subList(0, limit) : matching;
        return new Page(page, matching.size() > limit ? encode("w:" + page.getLast().getId()) : null);
    }

    /**
     * The list.
     *
     * @throws ForesignalException {@code 404 UNKNOWN_WATCHLIST}
     */
    public WatchListEntity get(UUID id) {
        return lists.findById(id).orElseThrow(() -> unknown(id));
    }

    /** {@link #get(UUID)} from text; an id that is not a UUID is unknown. */
    public WatchListEntity get(String id) {
        return get(parseId(id));
    }

    /**
     * Creates a list from a {@code WatchlistInput}.
     *
     * @throws ValidationException when the input is not valid
     */
    public WatchListEntity create(JsonNode body) {
        return create(UUID.randomUUID(), body);
    }

    /** Creates a list with {@code id} (settings import). */
    public WatchListEntity create(UUID id, JsonNode body) {
        ListInput input = parse(body, null);
        Instant now = clock.instant();
        WatchListEntity list = new WatchListEntity(id);
        list.setName(input.name());
        list.setIdentity(input.identity().wire());
        list.setNormalize(input.normalize().wire());
        list.setDescription(input.description());
        list.setCreatedAt(now);
        list.setUpdatedAt(now);
        WatchListEntity saved = lists.saveAndFlush(list);
        changed(saved.getId(), true);
        return saved;
    }

    /**
     * Replaces a list's name, normalization and description; the identity is fixed, and the
     * normalization may change only while the list is empty (stored values are normalized).
     *
     * @throws ForesignalException {@code 404 UNKNOWN_WATCHLIST}
     * @throws ValidationException when the input is not valid
     */
    public WatchListEntity update(UUID id, JsonNode body) {
        WatchListEntity list = get(id);
        ListInput input = parse(body, list);
        boolean renormalized = !input.normalize().wire().equals(list.getNormalize());
        if (renormalized && list.getItemCount() > 0) {
            throw ValidationException.forField("normalize", "can change only while the list is empty (its "
                    + list.getItemCount() + " values are stored normalized)");
        }
        list.setName(input.name());
        list.setNormalize(input.normalize().wire());
        list.setDescription(input.description());
        list.setUpdatedAt(clock.instant());
        WatchListEntity saved = lists.saveAndFlush(list);
        changed(id, renormalized);
        return saved;
    }

    /**
     * Deletes a list and its items; rules referencing it stop matching.
     *
     * @throws ForesignalException {@code 404 UNKNOWN_WATCHLIST}
     */
    public void delete(UUID id) {
        get(id);
        transactions.executeWithoutResult(status -> {
            jdbc.update("DELETE FROM watch_list_item WHERE list_id = ?", id);
            jdbc.update("DELETE FROM watch_list WHERE id = ?", id);
        });
        changed(id, true);
    }

    // ------------------------------------------------------------------------- items

    /**
     * One item.
     *
     * @param value the normalized value
     * @param label the label, or null
     * @param notes the notes, or null
     * @param tags the tags
     * @param addedAt when added
     * @param origin {@code manual}, {@code csv} or {@code json}
     */
    public record Item(String value, String label, String notes, List<String> tags, Instant addedAt, String origin) {

        /** Copies the tags. */
        public Item {
            tags = tags == null ? List.of() : List.copyOf(tags);
        }

        boolean sameContent(Item other) {
            return Objects.equals(label, other.label) && Objects.equals(notes, other.notes) && tags.equals(other.tags);
        }
    }

    /**
     * A page of items.
     *
     * @param items the items
     * @param nextCursor the next page's cursor, or null
     */
    public record ItemPage(List<Item> items, String nextCursor) {}

    /**
     * A page of a list's items, keyset by value.
     *
     * @param q a case-insensitive substring of value or label, or null
     */
    public ItemPage items(UUID listId, String q, String cursor, int limit) {
        get(listId);
        String after = decode(cursor, "i:");
        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT value, label, notes, tags, added_at, origin FROM watch_list_item"
                + " WHERE list_id = ?");
        args.add(listId);
        if (q != null && !q.isBlank()) {
            sql.append(" AND (value ILIKE ? OR label ILIKE ?)");
            String like = "%" + q.strip().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
            args.add(like);
            args.add(like);
        }
        if (after != null) {
            sql.append(" AND value > ?");
            args.add(after);
        }
        sql.append(" ORDER BY value LIMIT ").append(limit + 1);
        List<Item> rows = jdbc.query(sql.toString(), (rs, i) -> item(rs), args.toArray());
        String next = null;
        if (rows.size() > limit) {
            rows = new ArrayList<>(rows.subList(0, limit));
            next = encode("i:" + rows.getLast().value());
        }
        return new ItemPage(rows, next);
    }

    /** Every item of a list, by value (exports, settings). */
    public List<Item> allItems(UUID listId) {
        return jdbc.query("SELECT value, label, notes, tags, added_at, origin FROM watch_list_item WHERE list_id = ?"
                + " ORDER BY value", (rs, i) -> item(rs), listId);
    }

    private static Item item(java.sql.ResultSet rs) throws java.sql.SQLException {
        Array tags = rs.getArray("tags");
        Timestamp added = rs.getTimestamp("added_at");
        return new Item(rs.getString("value"), rs.getString("label"), rs.getString("notes"),
                tags == null ? List.of() : Arrays.asList((String[]) tags.getArray()),
                added == null ? null : added.toInstant(), rs.getString("origin"));
    }

    /**
     * The outcome of adding, removing or importing items.
     *
     * @param dryRun true when nothing was changed
     * @param mode {@code merge} or {@code replace}
     * @param added new values
     * @param updated existing values whose label, notes or tags changed
     * @param removed values removed
     * @param unchanged values given that were already present unchanged
     * @param invalid rejected rows
     * @param itemCount items after the operation (or as it would be)
     */
    public record Result(boolean dryRun, String mode, int added, int updated, int removed, int unchanged,
            List<Invalid> invalid, int itemCount) {}

    /**
     * One rejected row.
     *
     * @param row the 1-based data row (CSV: not counting the header), or null
     * @param value the value as given, or null
     * @param reason why
     */
    public record Invalid(Integer row, String value, String reason) {}

    /**
     * Adds or updates items ({@code WatchlistItemsInput}); every valid item is applied, invalid ones
     * are reported.
     *
     * @throws ValidationException {@code WATCHLIST_TOO_LARGE} above {@value #MAX_ITEMS} items, or when
     *     the body is not a {@code WatchlistItemsInput}
     */
    public Result add(UUID listId, JsonNode body) {
        WatchListEntity list = get(listId);
        if (body == null || !body.isObject() || !body.path("items").isArray() || body.path("items").isEmpty()) {
            throw ValidationException.forField("items", "must be a non-empty array of items");
        }
        Parsed parsed = parseJsonItems(list, body.get("items"));
        return apply(list, parsed, "merge", false, "manual");
    }

    /**
     * Removes values (normalized as the list normalizes); values not on the list count as unchanged.
     *
     * @throws ForesignalException {@code 404 UNKNOWN_WATCHLIST}
     */
    public Result remove(UUID listId, List<String> values) {
        WatchListEntity list = get(listId);
        Identity.Normalize normalize = Identity.Normalize.fromWire(list.getNormalize());
        Set<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            String n = normalize.apply(value);
            if (n != null && !n.isEmpty()) {
                normalized.add(n);
            }
        }
        int removed = transactions.execute(status -> {
            int count = 0;
            for (String value : normalized) {
                count += jdbc.update("DELETE FROM watch_list_item WHERE list_id = ? AND value = ?", listId, value);
            }
            updateCount(listId);
            return count;
        });
        changed(listId, false);
        return new Result(false, "merge", 0, 0, removed, normalized.size() - removed, List.of(), count(listId));
    }

    /**
     * Imports items from CSV ({@code text/csv}) or JSON ({@code application/json}).
     *
     * @param csv true for CSV, false for a JSON array
     * @param body the raw body
     * @param mode {@code merge} (add and update) or {@code replace} (also remove what is not imported)
     * @param dryRun report without changing anything
     * @throws ForesignalException {@code 413} above 5 MB
     * @throws ValidationException {@code WATCHLIST_TOO_LARGE}, or a body that cannot be read
     */
    public Result importItems(UUID listId, boolean csv, byte[] body, String mode, boolean dryRun) {
        WatchListEntity list = get(listId);
        if (body != null && body.length > MAX_IMPORT_BYTES) {
            throw new ForesignalException(HttpStatus.PAYLOAD_TOO_LARGE, ErrorCodes.PAYLOAD_TOO_LARGE,
                    "A watchlist import may be at most 5 MB.");
        }
        String text = body == null ? "" : new String(body, StandardCharsets.UTF_8);
        Parsed parsed;
        if (csv) {
            parsed = parseCsv(list, text);
        } else {
            JsonNode node;
            try {
                node = JSON.readTree(text);
            } catch (RuntimeException e) {
                throw new ValidationException("The import is not valid JSON.");
            }
            if (node == null || !node.isArray()) {
                throw new ValidationException("A JSON import must be an array of WatchlistItemInput objects.");
            }
            parsed = parseJsonItems(list, node);
        }
        return apply(list, parsed, mode, dryRun, csv ? "csv" : "json");
    }

    /** The CSV export: {@value #CSV_HEADER}, one row per item by value. */
    public String export(UUID listId) {
        get(listId);
        StringBuilder out = new StringBuilder(CSV_HEADER).append("\r\n");
        for (Item item : allItems(listId)) {
            out.append(Csv.field(item.value())).append(',')
                    .append(Csv.field(item.label())).append(',')
                    .append(Csv.field(item.notes())).append(',')
                    .append(Csv.field(item.tags().isEmpty() ? null : String.join(";", item.tags())))
                    .append("\r\n");
        }
        return out.toString();
    }

    // ------------------------------------------------------------------------- applying

    /**
     * Items read from a body, keyed by normalized value (the last row of a value wins).
     *
     * @param items the valid items
     * @param invalid the rejected rows
     */
    public record Parsed(Map<String, Item> items, List<Invalid> invalid) {}

    private Result apply(WatchListEntity list, Parsed parsed, String mode, boolean dryRun, String origin) {
        UUID listId = list.getId();
        Map<String, Item> existing = new LinkedHashMap<>();
        for (Item item : allItems(listId)) {
            existing.put(item.value(), item);
        }
        List<Item> toWrite = new ArrayList<>();
        int added = 0;
        int updated = 0;
        int unchanged = 0;
        for (Item item : parsed.items().values()) {
            Item current = existing.get(item.value());
            if (current == null) {
                added++;
                toWrite.add(item);
            } else if (!current.sameContent(item)) {
                updated++;
                toWrite.add(item);
            } else {
                unchanged++;
            }
        }
        List<String> toRemove = new ArrayList<>();
        if ("replace".equals(mode)) {
            for (String value : existing.keySet()) {
                if (!parsed.items().containsKey(value)) {
                    toRemove.add(value);
                }
            }
        }
        int after = existing.size() + added - toRemove.size();
        if (after > MAX_ITEMS) {
            throw new ValidationException(ErrorCodes.WATCHLIST_TOO_LARGE, "A watchlist holds at most " + MAX_ITEMS
                    + " items; this would make " + after + ".", List.of(new FieldError("items", "too many items")));
        }
        if (!dryRun && (!toWrite.isEmpty() || !toRemove.isEmpty())) {
            Instant now = clock.instant();
            transactions.executeWithoutResult(status -> {
                List<Object[]> batch = new ArrayList<>();
                for (Item item : toWrite) {
                    Item current = existing.get(item.value());
                    batch.add(new Object[] {listId, item.value(), item.label(), item.notes(),
                        item.tags().isEmpty() ? null : PgArrays.text(item.tags()),
                        java.sql.Timestamp.from(current == null ? now : current.addedAt()),
                        current == null ? origin : current.origin()});
                }
                if (!batch.isEmpty()) {
                    jdbc.batchUpdate(UPSERT_ITEM, batch);
                }
                for (String value : toRemove) {
                    jdbc.update("DELETE FROM watch_list_item WHERE list_id = ? AND value = ?", listId, value);
                }
                updateCount(listId);
            });
            changed(listId, false);
        }
        return new Result(dryRun, mode, added, updated, toRemove.size(), unchanged, parsed.invalid(), after);
    }

    private void updateCount(UUID listId) {
        jdbc.update("UPDATE watch_list SET item_count = (SELECT count(*) FROM watch_list_item WHERE list_id = ?),"
                + " updated_at = ? WHERE id = ?", listId, OffsetDateTime.ofInstant(clock.instant(), java.time.ZoneOffset.UTC),
                listId);
    }

    private int count(UUID listId) {
        Integer n = jdbc.queryForObject("SELECT count(*) FROM watch_list_item WHERE list_id = ?", Integer.class, listId);
        return n == null ? 0 : n;
    }

    // ------------------------------------------------------------------------- parsing

    /** Reads a JSON array of {@code WatchlistItemInput} for {@code list}, validating every value. */
    public static Parsed parseJsonItems(WatchListEntity list, JsonNode array) {
        Map<String, Item> items = new LinkedHashMap<>();
        List<Invalid> invalid = new ArrayList<>();
        int row = 0;
        for (JsonNode node : array) {
            row++;
            if (!node.isObject()) {
                invalid.add(new Invalid(row, null, "must be an object with value"));
                continue;
            }
            List<String> unknown = new ArrayList<>();
            node.propertyNames().forEach(name -> {
                if (!ITEM_MEMBERS.contains(name)) {
                    unknown.add(name);
                }
            });
            if (!unknown.isEmpty()) {
                invalid.add(new Invalid(row, node.path("value").asString(null), "unknown member(s): "
                        + String.join(", ", unknown)));
                continue;
            }
            JsonNode value = node.get("value");
            String raw = value == null || value.isNull() ? null : value.isNumber() ? value.asString() : value.asString(null);
            List<String> tags = new ArrayList<>();
            JsonNode tagsNode = node.get("tags");
            if (tagsNode != null && !tagsNode.isNull()) {
                if (!tagsNode.isArray()) {
                    invalid.add(new Invalid(row, raw, "tags must be an array of strings"));
                    continue;
                }
                for (JsonNode tag : tagsNode) {
                    if (tag.isString() && !tag.asString().isBlank()) {
                        tags.add(tag.asString().strip());
                    }
                }
            }
            accept(list, items, invalid, row, raw, optional(node, "label"), optional(node, "notes"), tags);
        }
        return new Parsed(items, invalid);
    }

    /** Reads a CSV import ({@code value[,label,notes,tags]}) for {@code list}, validating every value. */
    public static Parsed parseCsv(WatchListEntity list, String text) {
        List<List<String>> rows = Csv.parse(text);
        if (rows.isEmpty()) {
            throw new ValidationException("The CSV is empty; it needs the header value[,label,notes,tags].");
        }
        List<String> header = rows.getFirst().stream().map(h -> h.strip().toLowerCase(java.util.Locale.ROOT)).toList();
        if (header.isEmpty() || !header.getFirst().equals("value")) {
            throw new ValidationException("The CSV header must start with value (value[,label,notes,tags]).");
        }
        for (String column : header) {
            if (!List.of("value", "label", "notes", "tags").contains(column)) {
                throw new ValidationException("Unknown CSV column '" + column + "'; the header is value[,label,notes,tags].");
            }
        }
        Map<String, Item> items = new LinkedHashMap<>();
        List<Invalid> invalid = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) {
            List<String> cells = rows.get(i);
            if (cells.size() == 1 && cells.getFirst().isBlank()) {
                continue;
            }
            Map<String, String> byName = new LinkedHashMap<>();
            for (int c = 0; c < header.size() && c < cells.size(); c++) {
                byName.put(header.get(c), cells.get(c));
            }
            List<String> tags = new ArrayList<>();
            String tagText = byName.get("tags");
            if (tagText != null) {
                for (String tag : tagText.split(";")) {
                    if (!tag.isBlank()) {
                        tags.add(tag.strip());
                    }
                }
            }
            accept(list, items, invalid, i, byName.get("value"), blankToNull(byName.get("label")),
                    blankToNull(byName.get("notes")), tags);
        }
        return new Parsed(items, invalid);
    }

    private static void accept(WatchListEntity list, Map<String, Item> items, List<Invalid> invalid, int row, String raw,
            String label, String notes, List<String> tags) {
        if (raw == null || raw.isBlank()) {
            invalid.add(new Invalid(row, raw, "value is required"));
            return;
        }
        Identity identity = Identity.fromWire(list.getIdentity());
        String normalized = Identity.Normalize.fromWire(list.getNormalize()).apply(raw);
        String reason = identity.invalid(normalized);
        if (reason != null) {
            invalid.add(new Invalid(row, raw, "value " + reason));
            return;
        }
        if (label != null && label.length() > 200) {
            invalid.add(new Invalid(row, raw, "label must be at most 200 characters"));
            return;
        }
        items.put(normalized, new Item(normalized, label, notes, tags, null, null));
    }

    private static String optional(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() || !value.isString() ? null : blankToNull(value.asString());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    /**
     * A validated {@code WatchlistInput}.
     *
     * @param name the name
     * @param identity the identity
     * @param normalize the normalization
     * @param description the description, or null
     */
    public record ListInput(String name, Identity identity, Identity.Normalize normalize, String description) {}

    /**
     * Validates a {@code WatchlistInput}; with {@code existing} the identity must stay the same.
     *
     * @throws ValidationException listing every problem
     */
    public static ListInput parse(JsonNode body, WatchListEntity existing) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a WatchlistInput object.");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String member : body.propertyNames()) {
            if (!MEMBERS.contains(member) && !OUTPUT_ONLY.contains(member)) {
                errors.add(new FieldError(member, "is not a WatchlistInput member"));
            }
        }
        String name = body.path("name").isString() ? body.get("name").asString().strip() : null;
        if (name == null || name.isEmpty()) {
            errors.add(new FieldError("name", "is required"));
        } else if (name.length() > 200) {
            errors.add(new FieldError("name", "must be at most 200 characters"));
        }
        Identity identity = null;
        JsonNode identityNode = body.get("identity");
        if (identityNode == null || !identityNode.isString()) {
            errors.add(new FieldError("identity", "is required"));
        } else {
            try {
                identity = Identity.fromWire(identityNode.asString());
            } catch (IllegalArgumentException e) {
                errors.add(new FieldError("identity", e.getMessage()));
            }
        }
        if (existing != null && identity != null && !identity.wire().equals(existing.getIdentity())) {
            errors.add(new FieldError("identity", "is fixed for a list (it is " + existing.getIdentity() + ")"));
        }
        Identity.Normalize normalize = identity == null ? Identity.Normalize.NONE : identity.defaultNormalize();
        JsonNode normalizeNode = body.get("normalize");
        if (normalizeNode != null && !normalizeNode.isNull()) {
            try {
                normalize = Identity.Normalize.fromWire(normalizeNode.asString(""));
            } catch (IllegalArgumentException e) {
                errors.add(new FieldError("normalize", e.getMessage()));
            }
        }
        String description = null;
        JsonNode descriptionNode = body.get("description");
        if (descriptionNode != null && !descriptionNode.isNull()) {
            if (!descriptionNode.isString()) {
                errors.add(new FieldError("description", "must be a string"));
            } else {
                description = descriptionNode.asString();
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The watchlist is not valid.", errors);
        }
        return new ListInput(name, identity, normalize, description);
    }

    // ------------------------------------------------------------------------- helpers

    private void changed(UUID listId, boolean structural) {
        events.publishEvent(new WatchlistChangedEvent(listId, structural));
        if (structural) {
            events.publishEvent(new TriggerRulesChangedEvent(null));
        }
    }

    /** The UUID of {@code id}, or {@code 404 UNKNOWN_WATCHLIST}. */
    public static UUID parseId(String id) {
        try {
            return UUID.fromString(id);
        } catch (RuntimeException e) {
            throw unknown(id);
        }
    }

    static ForesignalException unknown(Object id) {
        return new ForesignalException(HttpStatus.NOT_FOUND, ErrorCodes.UNKNOWN_WATCHLIST,
                "No watchlist has the id '" + id + "'.");
    }

    private static String encode(String text) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String cursor, String prefix) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String text = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            if (!text.startsWith(prefix)) {
                throw new IllegalArgumentException();
            }
            return text.substring(prefix.length());
        } catch (IllegalArgumentException e) {
            throw new ValidationException(ErrorCodes.INVALID_CURSOR, "The cursor is not valid.",
                    List.of(new FieldError("cursor", "is not a cursor of this listing")));
        }
    }

    /** Lists and their items for a settings export. */
    public List<Map<String, Object>> exportAll(WatchlistMapper mapper) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (WatchListEntity list : all()) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("watchlist", mapper.watchlist(list));
            entry.put("items", allItems(list.getId()).stream().map(mapper::item).toList());
            out.add(entry);
        }
        return out;
    }

    /** Creates or replaces a list with its items from a settings bundle entry. */
    public void importEntry(JsonNode entry) {
        JsonNode watchlist = entry.path("watchlist");
        UUID id = parseId(watchlist.path("id").asString(""));
        tools.jackson.databind.node.ObjectNode input = JSON.createObjectNode();
        for (String member : MEMBERS) {
            if (watchlist.has(member)) {
                input.set(member, watchlist.get(member));
            }
        }
        WatchListEntity list = lists.findById(id).orElse(null);
        if (list == null) {
            list = create(id, input);
        } else if (!list.getIdentity().equals(watchlist.path("identity").asString(""))) {
            throw ValidationException.forField("watchlists", "list " + id + " exists with another identity");
        }
        JsonNode items = entry.path("items");
        if (items.isArray()) {
            Parsed parsed = parseJsonItems(list, sanitizedItems(items));
            apply(list, parsed, "replace", false, "json");
        }
    }

    private static JsonNode sanitizedItems(JsonNode items) {
        tools.jackson.databind.node.ArrayNode out = JSON.createArrayNode();
        for (JsonNode item : items) {
            tools.jackson.databind.node.ObjectNode copy = JSON.createObjectNode();
            for (String member : List.of("value", "label", "notes", "tags")) {
                if (item.has(member)) {
                    copy.set(member, item.get(member));
                }
            }
            out.add(copy);
        }
        return out;
    }
}
