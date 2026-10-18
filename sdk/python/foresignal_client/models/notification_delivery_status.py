from enum import StrEnum


class NotificationDeliveryStatus(StrEnum):
    DELIVERED = "delivered"
    FAILED = "failed"
    PENDING = "pending"
    SUPPRESSED = "suppressed"
    UNACKED = "unacked"

    def __str__(self) -> str:
        return str(self.value)
