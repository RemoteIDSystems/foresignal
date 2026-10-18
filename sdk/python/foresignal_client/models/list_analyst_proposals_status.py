from enum import StrEnum


class ListAnalystProposalsStatus(StrEnum):
    EXECUTED = "executed"
    EXPIRED = "expired"
    FAILED = "failed"
    PENDING = "pending"
    REJECTED = "rejected"

    def __str__(self) -> str:
        return str(self.value)
