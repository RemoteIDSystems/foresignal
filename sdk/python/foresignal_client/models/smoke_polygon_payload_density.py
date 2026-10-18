from enum import StrEnum


class SmokePolygonPayloadDensity(StrEnum):
    HEAVY = "heavy"
    LIGHT = "light"
    MEDIUM = "medium"

    def __str__(self) -> str:
        return str(self.value)
