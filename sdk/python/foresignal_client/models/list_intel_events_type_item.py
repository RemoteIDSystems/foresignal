from enum import StrEnum


class ListIntelEventsTypeItem(StrEnum):
    AVIATION_ANOMALY = "aviation-anomaly"
    CUSTOM = "custom"
    INFRASTRUCTURE_DISRUPTION = "infrastructure-disruption"
    MARITIME_DISRUPTION = "maritime-disruption"
    SEISMIC = "seismic"
    WEATHER_IMPACT = "weather-impact"

    def __str__(self) -> str:
        return str(self.value)
