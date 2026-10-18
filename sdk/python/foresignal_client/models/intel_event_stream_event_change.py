from enum import StrEnum


class IntelEventStreamEventChange(StrEnum):
    CREATED = "created"
    MERGED = "merged"
    RESOLVED = "resolved"
    UPDATED = "updated"

    def __str__(self) -> str:
        return str(self.value)
