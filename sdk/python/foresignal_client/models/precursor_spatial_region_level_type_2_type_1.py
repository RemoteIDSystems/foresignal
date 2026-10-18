from enum import StrEnum


class PrecursorSpatialRegionLevelType2Type1(StrEnum):
    ADMIN1 = "admin1"
    COUNTRY = "country"

    def __str__(self) -> str:
        return str(self.value)
