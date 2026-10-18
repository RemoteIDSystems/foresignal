from enum import StrEnum


class EventStudyResultFrequency(StrEnum):
    DAY = "day"
    WEEK = "week"

    def __str__(self) -> str:
        return str(self.value)
