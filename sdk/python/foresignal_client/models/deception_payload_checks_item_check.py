from enum import StrEnum


class DeceptionPayloadChecksItemCheck(StrEnum):
    AIRCRAFT_IMPLIED_SPEED = "aircraft-implied-speed"
    DUPLICATE_MMSI = "duplicate-mmsi"
    VESSEL_IMPLIED_SPEED = "vessel-implied-speed"

    def __str__(self) -> str:
        return str(self.value)
