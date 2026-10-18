from enum import StrEnum


class TripRunStatus(StrEnum):
    DELIVERED = "delivered"
    DRY_RUN = "dry-run"
    FAILED = "failed"
    SKIPPED = "skipped"

    def __str__(self) -> str:
        return str(self.value)
