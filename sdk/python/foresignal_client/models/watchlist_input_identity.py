from enum import StrEnum


class WatchlistInputIdentity(StrEnum):
    CALLSIGN = "callsign"
    CUSTOM = "custom"
    ENTITY = "entity"
    ICAO24 = "icao24"
    IMO = "imo"
    MMSI = "mmsi"
    REGISTRATION = "registration"

    def __str__(self) -> str:
        return str(self.value)
