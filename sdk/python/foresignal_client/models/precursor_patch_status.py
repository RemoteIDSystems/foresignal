from enum import StrEnum


class PrecursorPatchStatus(StrEnum):
    RETIRED = "retired"
    VALIDATED = "validated"

    def __str__(self) -> str:
        return str(self.value)
