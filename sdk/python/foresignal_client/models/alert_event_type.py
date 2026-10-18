from enum import StrEnum


class AlertEventType(StrEnum):
    ACKNOWLEDGED = "acknowledged"
    FIRED = "fired"
    RESOLVED = "resolved"
    SNOOZED = "snoozed"
    UPDATED = "updated"

    def __str__(self) -> str:
        return str(self.value)
