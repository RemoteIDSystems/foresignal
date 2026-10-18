from enum import StrEnum


class ReportSendRecipientsItemStatus(StrEnum):
    DELIVERED = "delivered"
    FAILED = "failed"
    GRANTED = "granted"
    NOTIFIED = "notified"

    def __str__(self) -> str:
        return str(self.value)
