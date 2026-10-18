from enum import StrEnum


class ExposureAreaSource(StrEnum):
    EVENT = "event"
    GEOMETRY = "geometry"
    PLACE = "place"

    def __str__(self) -> str:
        return str(self.value)
