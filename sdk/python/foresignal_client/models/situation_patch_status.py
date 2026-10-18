from enum import StrEnum


class SituationPatchStatus(StrEnum):
    ACTIVE = "active"
    CLOSED = "closed"

    def __str__(self) -> str:
        return str(self.value)
