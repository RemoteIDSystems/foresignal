from enum import StrEnum


class EvidenceEventStatus(StrEnum):
    CLOSED = "closed"
    MERGED = "merged"
    OPEN = "open"

    def __str__(self) -> str:
        return str(self.value)
