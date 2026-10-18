from enum import StrEnum


class OpaqueChronicleLinkTimeOverlap(StrEnum):
    FULL = "full"
    NONE = "none"
    PARTIAL = "partial"

    def __str__(self) -> str:
        return str(self.value)
