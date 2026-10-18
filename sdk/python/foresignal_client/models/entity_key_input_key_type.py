from enum import StrEnum


class EntityKeyInputKeyType(StrEnum):
    AIRFRAME = "airframe"
    CALLSIGN = "callsign"
    COSPAR = "cospar"
    ICAO24 = "icao24"
    IMO = "imo"
    MMSI = "mmsi"
    NORAD = "norad"
    REGISTRATION = "registration"

    def __str__(self) -> str:
        return str(self.value)
