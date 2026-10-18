from enum import StrEnum


class ScorecardEntryLabel(StrEnum):
    INSUFFICIENT = "insufficient"
    OK = "ok"

    def __str__(self) -> str:
        return str(self.value)
