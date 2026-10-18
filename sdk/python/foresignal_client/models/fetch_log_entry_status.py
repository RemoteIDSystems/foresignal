from enum import StrEnum


class FetchLogEntryStatus(StrEnum):
    DISCARDED = "DISCARDED"
    FAILED = "FAILED"
    NOT_MODIFIED = "NOT_MODIFIED"
    OK = "OK"
    REFUSED = "REFUSED"

    def __str__(self) -> str:
        return str(self.value)
