from enum import StrEnum


class ListNotificationsEventsItem(StrEnum):
    ALERT = "alert"
    BRIEFING = "briefing"
    SYSTEM = "system"

    def __str__(self) -> str:
        return str(self.value)
