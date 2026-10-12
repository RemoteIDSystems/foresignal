package ai.foresignal.watch;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Maps watchlists to the {@code Watchlist}, {@code WatchlistItem} and result schemas (prompt 036). */
@Component
public class WatchlistMapper {

    /** The {@code Watchlist} object. */
    public Map<String, Object> watchlist(WatchListEntity list) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", list.getId().toString());
        out.put("name", list.getName());
        out.put("identity", list.getIdentity());
        out.put("normalize", list.getNormalize());
        out.put("description", list.getDescription());
        out.put("itemCount", list.getItemCount());
        out.put("createdAt", list.getCreatedAt());
        out.put("updatedAt", list.getUpdatedAt());
        return out;
    }

    /** The {@code WatchlistPage} object. */
    public Map<String, Object> page(WatchlistService.Page page) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", page.items().stream().map(this::watchlist).toList());
        out.put("nextCursor", page.nextCursor());
        return out;
    }

    /** The {@code WatchlistItem} object. */
    public Map<String, Object> item(WatchlistService.Item item) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("value", item.value());
        out.put("label", item.label());
        out.put("notes", item.notes());
        out.put("tags", item.tags());
        out.put("addedAt", item.addedAt());
        out.put("origin", item.origin());
        return out;
    }

    /** The {@code WatchlistItemPage} object. */
    public Map<String, Object> itemPage(WatchlistService.ItemPage page) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", page.items().stream().map(this::item).toList());
        out.put("nextCursor", page.nextCursor());
        return out;
    }

    /** The {@code WatchlistImportResult} object. */
    public Map<String, Object> result(WatchlistService.Result result) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("dryRun", result.dryRun());
        out.put("mode", result.mode());
        out.put("added", result.added());
        out.put("updated", result.updated());
        out.put("removed", result.removed());
        out.put("unchanged", result.unchanged());
        List<Map<String, Object>> invalid = new ArrayList<>();
        for (WatchlistService.Invalid row : result.invalid()) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("row", row.row());
            r.put("value", row.value());
            r.put("reason", row.reason());
            invalid.add(r);
        }
        out.put("invalid", invalid);
        out.put("itemCount", result.itemCount());
        return out;
    }
}
