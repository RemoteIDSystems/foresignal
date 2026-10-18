from enum import StrEnum


class NotificationMessageEvent(StrEnum):
    ALERT = "alert"
    BRIEFING = "briefing"
    SYSTEM = "system"
    TEST = "test"

    def __str__(self) -> str:
        return str(self.value)
