from enum import StrEnum


class DeviceInfoPlatform(StrEnum):
    ANDROID = "android"
    IOS = "ios"
    LINUX = "linux"
    MACOS = "macos"
    OTHER = "other"
    WEB = "web"
    WINDOWS = "windows"

    def __str__(self) -> str:
        return str(self.value)
