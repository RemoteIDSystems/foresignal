from enum import StrEnum


class ListCanonicalEventsFlagsItem(StrEnum):
    ECHO = "ECHO"
    LATE_REVISION = "LATE_REVISION"
    SINGLE_ORIGINATOR = "SINGLE_ORIGINATOR"
    TIMESTAMP_CONFLICT = "TIMESTAMP_CONFLICT"

    def __str__(self) -> str:
        return str(self.value)
