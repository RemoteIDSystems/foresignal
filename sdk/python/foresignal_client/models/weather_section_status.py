from enum import StrEnum


class WeatherSectionStatus(StrEnum):
    NO_COVERAGE = "no-coverage"
    OK = "ok"
    STALE = "stale"
    UNAVAILABLE = "unavailable"

    def __str__(self) -> str:
        return str(self.value)
