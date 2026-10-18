from enum import StrEnum


class AnalysisSeriesInputFrequency(StrEnum):
    DAY = "day"
    HOUR = "hour"
    WEEK = "week"

    def __str__(self) -> str:
        return str(self.value)
