from enum import StrEnum


class ChronicleThreadItemType(StrEnum):
    ALERT = "alert"
    BACKFILL = "backfill"
    CAPTURE_GAP = "capture-gap"
    CAP_FALLBACK = "cap-fallback"
    CAP_WARNING = "cap-warning"
    COMPARISON_CHANGE = "comparison-change"
    FINDING = "finding"
    FIRST_APPEARANCE = "first-appearance"
    FORECAST_RESOLUTION = "forecast-resolution"
    HYPOTHESIS_BAND_CHANGE = "hypothesis-band-change"
    LAYER_EDIT = "layer-edit"
    NARRATIVE_CHANGE = "narrative-change"
    SITUATION_CHANGE = "situation-change"
    STATUS_CHANGE = "status-change"

    def __str__(self) -> str:
        return str(self.value)
