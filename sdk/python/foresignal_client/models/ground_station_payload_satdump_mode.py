from enum import StrEnum


class GroundStationPayloadSatdumpMode(StrEnum):
    AUTOTRACK = "autotrack"
    LIVE = "live"

    def __str__(self) -> str:
        return str(self.value)
