from enum import StrEnum


class SocialPlatformType(StrEnum):
    CUE = "CUE"
    POLLING = "POLLING"
    STREAMING = "STREAMING"
    STREAMINGPOLLING = "STREAMING+POLLING"

    def __str__(self) -> str:
        return str(self.value)
