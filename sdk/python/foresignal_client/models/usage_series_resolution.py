from enum import StrEnum


class UsageSeriesResolution(StrEnum):
    DAY = "day"
    HOUR = "hour"
    MINUTE = "minute"

    def __str__(self) -> str:
        return str(self.value)
