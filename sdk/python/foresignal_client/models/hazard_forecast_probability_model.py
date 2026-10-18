from enum import StrEnum


class HazardForecastProbabilityModel(StrEnum):
    GAUGE_CATEGORY = "gauge-category"
    NONE = "none"
    PUBLISHED_CATEGORY_RANGE = "published-category-range"
    PUBLISHED_POINT = "published-point"
    PUBLISHED_POLYGONS = "published-polygons"
    TRACK_ERROR_ELLIPSE = "track-error-ellipse"

    def __str__(self) -> str:
        return str(self.value)
