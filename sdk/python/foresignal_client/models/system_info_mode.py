from enum import StrEnum


class SystemInfoMode(StrEnum):
    EDGE = "edge"
    FULL = "full"

    def __str__(self) -> str:
        return str(self.value)
