from enum import StrEnum


class AircraftFusionPayloadPositionSource(StrEnum):
    ADSB = "adsb"
    MLAT = "mlat"
    OTHER = "other"
    TISB = "tisb"

    def __str__(self) -> str:
        return str(self.value)
