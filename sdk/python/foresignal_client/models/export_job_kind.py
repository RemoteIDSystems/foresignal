from enum import StrEnum


class ExportJobKind(StrEnum):
    EXPORT = "EXPORT"
    PACK = "PACK"
    PLACE_PACK = "PLACE_PACK"
    RASTER = "RASTER"
    SNAPSHOT = "SNAPSHOT"

    def __str__(self) -> str:
        return str(self.value)
