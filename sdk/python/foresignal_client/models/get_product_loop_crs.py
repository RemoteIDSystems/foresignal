from enum import StrEnum


class GetProductLoopCrs(StrEnum):
    EPSG3857 = "EPSG:3857"

    def __str__(self) -> str:
        return str(self.value)
