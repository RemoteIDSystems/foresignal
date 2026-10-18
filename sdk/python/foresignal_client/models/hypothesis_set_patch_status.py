from enum import StrEnum


class HypothesisSetPatchStatus(StrEnum):
    ACTIVE = "active"
    ARCHIVED = "archived"

    def __str__(self) -> str:
        return str(self.value)
