from enum import StrEnum


class AnalystNoteStatus(StrEnum):
    DISMISSED = "dismissed"
    NEW = "new"
    READ = "read"

    def __str__(self) -> str:
        return str(self.value)
