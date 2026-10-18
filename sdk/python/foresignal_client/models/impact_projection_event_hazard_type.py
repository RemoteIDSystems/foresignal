from enum import StrEnum


class ImpactProjectionEventHazardType(StrEnum):
    EXCESSIVE_RAIN = "excessive-rain"
    HAIL = "hail"
    RIVER_FLOOD = "river-flood"
    SEVERE_CATEGORICAL = "severe-categorical"
    STORM_CELL = "storm-cell"
    TORNADO = "tornado"
    TROPICAL_TRACK = "tropical-track"
    TROPICAL_WIND_34 = "tropical-wind-34"
    TROPICAL_WIND_50 = "tropical-wind-50"
    TROPICAL_WIND_64 = "tropical-wind-64"
    WIND = "wind"

    def __str__(self) -> str:
        return str(self.value)
