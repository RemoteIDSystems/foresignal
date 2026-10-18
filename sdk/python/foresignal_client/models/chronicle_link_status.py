from enum import StrEnum


class ChronicleLinkStatus(StrEnum):
    ACTIVE = "active"
    DISMISSED = "dismissed"

    def __str__(self) -> str:
        return str(self.value)
