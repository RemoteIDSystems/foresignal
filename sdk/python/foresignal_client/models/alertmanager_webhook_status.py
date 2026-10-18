from enum import StrEnum


class AlertmanagerWebhookStatus(StrEnum):
    FIRING = "firing"
    RESOLVED = "resolved"

    def __str__(self) -> str:
        return str(self.value)
