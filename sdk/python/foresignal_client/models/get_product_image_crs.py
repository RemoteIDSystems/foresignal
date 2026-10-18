from enum import StrEnum


class GetProductImageCrs(StrEnum):
    EPSG3857 = "EPSG:3857"
    EPSG4326 = "EPSG:4326"

    def __str__(self) -> str:
        return str(self.value)
