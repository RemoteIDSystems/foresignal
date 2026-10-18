from enum import StrEnum


class LedgerQuestionBriefStatus(StrEnum):
    ANNULLED = "annulled"
    CLOSED = "closed"
    OPEN = "open"
    RESOLVED = "resolved"

    def __str__(self) -> str:
        return str(self.value)
