from enum import StrEnum


class ListAnalystNotesStatus(StrEnum):
    DISMISSED = "dismissed"
    NEW = "new"
    READ = "read"

    def __str__(self) -> str:
        return str(self.value)
