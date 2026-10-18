from enum import StrEnum


class MapAttachmentConfigAttachMap(StrEnum):
    NONE = "none"
    STATIC = "static"

    def __str__(self) -> str:
        return str(self.value)
