from enum import StrEnum


class NotificationAlertStateType2Type1(StrEnum):
    ACKNOWLEDGED = "acknowledged"
    FIRING = "firing"
    RESOLVED = "resolved"

    def __str__(self) -> str:
        return str(self.value)
