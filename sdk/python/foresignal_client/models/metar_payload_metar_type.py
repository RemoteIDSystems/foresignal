from enum import StrEnum


class MetarPayloadMetarType(StrEnum):
    METAR = "METAR"
    SPECI = "SPECI"

    def __str__(self) -> str:
        return str(self.value)
