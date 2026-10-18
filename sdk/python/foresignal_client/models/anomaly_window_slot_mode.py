from enum import StrEnum


class AnomalyWindowSlotMode(StrEnum):
    HOUR_OF_DAY = "hour-of-day"
    HOUR_OF_WEEK = "hour-of-week"

    def __str__(self) -> str:
        return str(self.value)
