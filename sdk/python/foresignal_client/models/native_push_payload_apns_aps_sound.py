from enum import StrEnum


class NativePushPayloadApnsApsSound(StrEnum):
    DEFAULT = "default"

    def __str__(self) -> str:
        return str(self.value)
