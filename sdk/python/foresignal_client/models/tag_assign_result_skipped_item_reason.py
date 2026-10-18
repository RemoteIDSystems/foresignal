from enum import StrEnum


class TagAssignResultSkippedItemReason(StrEnum):
    INVALID_REF = "invalid-ref"
    NOT_VISIBLE = "not-visible"

    def __str__(self) -> str:
        return str(self.value)
