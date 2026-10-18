from enum import StrEnum


class GetHistoryFramesGeometry(StrEnum):
    FULL = "full"
    POINT = "point"

    def __str__(self) -> str:
        return str(self.value)
