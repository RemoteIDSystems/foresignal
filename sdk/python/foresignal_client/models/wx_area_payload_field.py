from enum import StrEnum


class WxAreaPayloadField(StrEnum):
    FFG = "ffg"
    MESH = "mesh"
    PRECIP = "precip"
    QPE1H = "qpe1h"
    REFL = "refl"
    ROTATION = "rotation"

    def __str__(self) -> str:
        return str(self.value)
