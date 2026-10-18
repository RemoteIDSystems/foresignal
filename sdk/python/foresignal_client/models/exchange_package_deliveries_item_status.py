from enum import StrEnum


class ExchangePackageDeliveriesItemStatus(StrEnum):
    ACKNOWLEDGED = "acknowledged"
    DELIVERED = "delivered"
    FAILED = "failed"
    PENDING = "pending"

    def __str__(self) -> str:
        return str(self.value)
