from enum import StrEnum


class TrackType(StrEnum):
    FEATURE = "Feature"

    def __str__(self) -> str:
        return str(self.value)
