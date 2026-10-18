from enum import StrEnum


class PrecursorTestSpatialType0(StrEnum):
    NONE = "none"
    SAMEENTITY = "sameEntity"

    def __str__(self) -> str:
        return str(self.value)
