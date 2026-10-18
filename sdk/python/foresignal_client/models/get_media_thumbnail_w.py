from enum import IntEnum


class GetMediaThumbnailW(IntEnum):
    VALUE_256 = 256
    VALUE_1024 = 1024

    def __str__(self) -> str:
        return str(self.value)
