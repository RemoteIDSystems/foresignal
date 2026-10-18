from enum import StrEnum


class ListEntitiesType(StrEnum):
    AIRCRAFT = "aircraft"
    SATELLITE = "satellite"
    STATION = "station"
    VESSEL = "vessel"

    def __str__(self) -> str:
        return str(self.value)
