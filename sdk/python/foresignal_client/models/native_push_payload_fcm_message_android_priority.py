from enum import StrEnum


class NativePushPayloadFcmMessageAndroidPriority(StrEnum):
    HIGH = "HIGH"
    NORMAL = "NORMAL"

    def __str__(self) -> str:
        return str(self.value)
