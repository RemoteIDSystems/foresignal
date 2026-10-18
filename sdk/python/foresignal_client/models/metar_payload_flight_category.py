from enum import StrEnum


class MetarPayloadFlightCategory(StrEnum):
    IFR = "IFR"
    LIFR = "LIFR"
    MVFR = "MVFR"
    VFR = "VFR"

    def __str__(self) -> str:
        return str(self.value)
