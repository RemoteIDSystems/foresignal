from enum import StrEnum


class PushPayloadEvent(StrEnum):
    ALERT = "alert"
    BRIEFING = "briefing"
    SYSTEM = "system"
    VERIFY = "verify"

    def __str__(self) -> str:
        return str(self.value)
