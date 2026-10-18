from enum import StrEnum


class PrecursorSpatialRule(StrEnum):
    NONE = "none"
    SAMEENTITY = "sameEntity"
    SAMEREGION = "sameRegion"
    WITHINKM = "withinKm"

    def __str__(self) -> str:
        return str(self.value)
