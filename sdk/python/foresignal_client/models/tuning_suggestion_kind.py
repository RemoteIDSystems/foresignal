from enum import StrEnum


class TuningSuggestionKind(StrEnum):
    ACTIVE_WINDOW = "ACTIVE_WINDOW"
    ADD_COOLDOWN = "add-cooldown"
    ADD_EDGE_MIN_INTERVAL = "add-edge-min-interval"
    LOWER_ANOMALY_SENSITIVITY = "lower-anomaly-sensitivity"
    LOWER_THRESHOLD = "lower-threshold"
    RAISE_THRESHOLD = "raise-threshold"
    SEVERITY_DOWN = "SEVERITY_DOWN"
    SEVERITY_UP = "SEVERITY_UP"
    SHRINK_GEOFENCE = "shrink-geofence"
    SWITCH_TO_ENTER = "switch-to-enter"

    def __str__(self) -> str:
        return str(self.value)
