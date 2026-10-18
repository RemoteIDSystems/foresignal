from enum import StrEnum


class SeriesDataFrequency(StrEnum):
    DAY = "day"
    HOUR = "hour"
    IRREGULAR = "irregular"
    MONTH = "month"
    WEEK = "week"

    def __str__(self) -> str:
        return str(self.value)
