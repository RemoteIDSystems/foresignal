from enum import StrEnum


class PushDeviceInputContentLevel(StrEnum):
    MINIMAL = "minimal"
    SHORT = "short"

    def __str__(self) -> str:
        return str(self.value)
