from enum import StrEnum


class ArchivePartitionTier(StrEnum):
    STD = "std"
    TRK = "trk"

    def __str__(self) -> str:
        return str(self.value)
