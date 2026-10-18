from enum import StrEnum


class NotificationDeliveryKind(StrEnum):
    DIGEST = "digest"
    FOLLOWUP = "followup"
    SEND = "send"
    UPDATE = "update"

    def __str__(self) -> str:
        return str(self.value)
