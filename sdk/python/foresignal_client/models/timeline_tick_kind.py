from enum import StrEnum


class TimelineTickKind(StrEnum):
    FORECAST = "forecast"
    OBSERVED = "observed"

    def __str__(self) -> str:
        return str(self.value)
