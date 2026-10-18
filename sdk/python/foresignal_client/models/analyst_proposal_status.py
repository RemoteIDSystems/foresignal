from enum import StrEnum


class AnalystProposalStatus(StrEnum):
    EXECUTED = "executed"
    EXPIRED = "expired"
    FAILED = "failed"
    PENDING = "pending"
    REJECTED = "rejected"

    def __str__(self) -> str:
        return str(self.value)
