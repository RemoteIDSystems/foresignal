from enum import StrEnum


class PushDeviceUpdateContentLevel(StrEnum):
    MINIMAL = "minimal"
    SHORT = "short"

    def __str__(self) -> str:
        return str(self.value)
