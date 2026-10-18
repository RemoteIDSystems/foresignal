from enum import StrEnum


class SatellitePassPayloadGeometrySource(StrEnum):
    FOOTPRINT = "footprint"
    SWATH = "swath"

    def __str__(self) -> str:
        return str(self.value)
