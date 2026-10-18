from enum import StrEnum


class AlertmanagerWebhookAlertsItemStatus(StrEnum):
    FIRING = "firing"
    RESOLVED = "resolved"

    def __str__(self) -> str:
        return str(self.value)
