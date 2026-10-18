from enum import StrEnum


class ChronicleArchiveType0State(StrEnum):
    ARCHIVED = "archived"
    FAILED = "failed"
    PENDING = "pending"
    RESTORING = "restoring"

    def __str__(self) -> str:
        return str(self.value)
