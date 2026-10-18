from enum import StrEnum


class TimelineTickFramesItemAvailability(StrEnum):
    AVAILABLE = "available"
    UNVERIFIED = "unverified"

    def __str__(self) -> str:
        return str(self.value)
