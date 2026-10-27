from enum import StrEnum


class ArchiveFilesItemTier(StrEnum):
    STANDARD = "standard"
    TRACK = "track"

    def __str__(self) -> str:
        return str(self.value)
