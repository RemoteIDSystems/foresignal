from enum import StrEnum


class IntelEventMemberRole(StrEnum):
    CONTEXT = "context"
    CONTRADICTING = "contradicting"
    SUPPORTING = "supporting"

    def __str__(self) -> str:
        return str(self.value)
