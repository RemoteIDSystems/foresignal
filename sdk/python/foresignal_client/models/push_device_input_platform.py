from enum import StrEnum


class PushDeviceInputPlatform(StrEnum):
    ANDROID = "android"
    IOS = "ios"
    IPADOS = "ipados"
    MACOS = "macos"

    def __str__(self) -> str:
        return str(self.value)
