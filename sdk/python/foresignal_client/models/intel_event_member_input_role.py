from enum import StrEnum


class IntelEventMemberInputRole(StrEnum):
    CONTEXT = "context"
    CONTRADICTING = "contradicting"
    SUPPORTING = "supporting"

    def __str__(self) -> str:
        return str(self.value)
