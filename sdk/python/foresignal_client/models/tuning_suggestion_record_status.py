from enum import StrEnum


class TuningSuggestionRecordStatus(StrEnum):
    APPLIED = "APPLIED"
    DISMISSED = "DISMISSED"
    OPEN = "OPEN"
    STALE = "STALE"

    def __str__(self) -> str:
        return str(self.value)
