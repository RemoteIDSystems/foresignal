from enum import StrEnum


class GetCalendarFeed(StrEnum):
    BRIEFINGS = "briefings"
    CUSTOM = "custom"
    LAUNCHES = "launches"
    NWS = "nws"
    TFR = "tfr"

    def __str__(self) -> str:
        return str(self.value)
