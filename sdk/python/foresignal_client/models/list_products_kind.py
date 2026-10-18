from enum import StrEnum


class ListProductsKind(StrEnum):
    EXTERNAL = "external"
    FIELD = "field"
    RASTER = "raster"

    def __str__(self) -> str:
        return str(self.value)
