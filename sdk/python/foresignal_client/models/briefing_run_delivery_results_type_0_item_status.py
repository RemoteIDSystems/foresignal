from enum import StrEnum


class BriefingRunDeliveryResultsType0ItemStatus(StrEnum):
    AVAILABLE = "available"
    FAILED = "failed"
    QUEUED = "queued"
    SKIPPED = "skipped"

    def __str__(self) -> str:
        return str(self.value)
