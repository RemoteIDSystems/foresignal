from enum import StrEnum


class ProductKind(StrEnum):
    EXTERNAL = "external"
    FIELD = "field"
    RASTER = "raster"

    def __str__(self) -> str:
        return str(self.value)
