from enum import StrEnum


class GroundStationPayloadState(StrEnum):
    IDLE = "idle"
    LOCKED = "locked"
    RECEIVING = "receiving"
    TRACKING = "tracking"
    UNKNOWN = "unknown"

    def __str__(self) -> str:
        return str(self.value)
