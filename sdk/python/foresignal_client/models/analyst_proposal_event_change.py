from enum import StrEnum


class AnalystProposalEventChange(StrEnum):
    CREATED = "created"
    EXECUTED = "executed"
    EXPIRED = "expired"
    FAILED = "failed"
    REJECTED = "rejected"

    def __str__(self) -> str:
        return str(self.value)
