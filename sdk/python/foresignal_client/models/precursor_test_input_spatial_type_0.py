from enum import StrEnum


class PrecursorTestInputSpatialType0(StrEnum):
    NONE = "none"
    SAMEENTITY = "sameEntity"

    def __str__(self) -> str:
        return str(self.value)
