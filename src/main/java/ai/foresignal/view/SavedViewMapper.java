package ai.foresignal.view;

import ai.foresignal.source.formats.geojson.GeoJsonWriter;
import ai.foresignal.store.TriggerCondition;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Maps saved views to the {@code SavedView} and {@code SavedViewPage} schemas (prompt 036). */
@Component
public class SavedViewMapper {

    private final GeoJsonWriter geoJson = new GeoJsonWriter();

    /** The {@code SavedView} object. */
    public Map<String, Object> view(SavedViewEntity v) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", v.getId().toString());
        out.put("slug", v.getSlug());
        out.put("name", v.getName());
        out.put("sources", v.getSources());
        out.put("kinds", v.getKinds());
        out.put("placeIds", v.getPlaceIds());
        out.put("bbox", v.getBbox());
        out.put("area", v.getArea() == null ? null : geoJson.write(v.getArea()));
        List<Map<String, Object>> conditions = new ArrayList<>();
        for (TriggerCondition condition : v.getConditions()) {
            Map<String, Object> c = new LinkedHashMap<>();
            if (condition.path() != null) {
                c.put("path", condition.path());
            }
            c.put("op", condition.op());
            c.put("value", condition.value());
            conditions.add(c);
        }
        out.put("conditions", conditions);
        out.put("timeField", v.getTimeField());
        out.put("includeExpired", v.isIncludeExpired());
        out.put("fields", v.getFields());
        out.put("version", v.getVersion());
        out.put("shared", v.getOwnerPrincipalId() == null);
        out.put("ownerPrincipalId", v.getOwnerPrincipalId());
        out.put("createdAt", v.getCreatedAt());
        out.put("updatedAt", v.getUpdatedAt());
        return out;
    }

    /** The {@code SavedViewPage} object. */
    public Map<String, Object> page(SavedViewService.Page page) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", page.items().stream().map(this::view).toList());
        out.put("nextCursor", page.nextCursor());
        return out;
    }
}
