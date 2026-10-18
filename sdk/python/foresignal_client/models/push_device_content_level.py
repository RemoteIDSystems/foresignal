from enum import StrEnum


class PushDeviceContentLevel(StrEnum):
    MINIMAL = "minimal"
    SHORT = "short"

    def __str__(self) -> str:
        return str(self.value)
