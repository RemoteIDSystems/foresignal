from enum import StrEnum


class ChronicleLinkTemporal(StrEnum):
    A_PRECEDES_B = "a-precedes-b"
    B_PRECEDES_A = "b-precedes-a"
    CONCURRENT = "concurrent"

    def __str__(self) -> str:
        return str(self.value)
