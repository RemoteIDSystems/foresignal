from enum import StrEnum


class ExchangeReceiptType(StrEnum):
    ACKNOWLEDGED = "acknowledged"
    DELIVERED = "delivered"

    def __str__(self) -> str:
        return str(self.value)
