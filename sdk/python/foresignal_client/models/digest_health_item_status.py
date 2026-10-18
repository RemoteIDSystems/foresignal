from enum import StrEnum


class DigestHealthItemStatus(StrEnum):
    DEGRADED = "degraded"
    FAILING = "failing"

    def __str__(self) -> str:
        return str(self.value)
