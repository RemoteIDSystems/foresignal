from enum import StrEnum


class FindingPatchStatus(StrEnum):
    DISMISSED = "dismissed"

    def __str__(self) -> str:
        return str(self.value)
