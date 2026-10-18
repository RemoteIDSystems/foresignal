from enum import StrEnum


class SocialPostMediaType(StrEnum):
    AUDIO = "audio"
    GIF = "gif"
    IMAGE = "image"
    LINK_CARD = "link-card"
    OTHER = "other"
    VIDEO = "video"

    def __str__(self) -> str:
        return str(self.value)
