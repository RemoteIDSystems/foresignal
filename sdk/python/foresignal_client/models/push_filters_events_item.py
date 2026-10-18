from enum import StrEnum


class PushFiltersEventsItem(StrEnum):
    ALERT = "alert"
    BRIEFING = "briefing"
    SYSTEM = "system"

    def __str__(self) -> str:
        return str(self.value)
