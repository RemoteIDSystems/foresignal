from enum import StrEnum


class AlertState(StrEnum):
    ACKNOWLEDGED = "acknowledged"
    FIRING = "firing"
    RESOLVED = "resolved"

    def __str__(self) -> str:
        return str(self.value)
