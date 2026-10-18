from enum import StrEnum


class MediaRole(StrEnum):
    AUDIO = "audio"
    DOCUMENT = "document"
    IMAGE = "image"
    MAP = "map"
    PROJECTED = "projected"
    THUMBNAIL = "thumbnail"

    def __str__(self) -> str:
        return str(self.value)
