from enum import StrEnum


class AskActionStatus(StrEnum):
    EXECUTED = "executed"
    FAILED = "failed"
    PENDING = "pending"
    REJECTED = "rejected"

    def __str__(self) -> str:
        return str(self.value)
