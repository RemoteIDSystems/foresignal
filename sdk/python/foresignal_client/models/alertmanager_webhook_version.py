from enum import StrEnum


class AlertmanagerWebhookVersion(StrEnum):
    VALUE_0 = "4"

    def __str__(self) -> str:
        return str(self.value)
