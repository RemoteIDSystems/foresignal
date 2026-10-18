from enum import StrEnum


class NotificationEvent(StrEnum):
    ALERT = "alert"
    BRIEFING = "briefing"
    SYSTEM = "system"

    def __str__(self) -> str:
        return str(self.value)
