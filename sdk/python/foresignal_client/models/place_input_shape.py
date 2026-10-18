from enum import StrEnum


class PlaceInputShape(StrEnum):
    POINT = "point"
    POLYGON = "polygon"
    ROUTE = "route"

    def __str__(self) -> str:
        return str(self.value)
