from enum import StrEnum


class NarrativePostLinkReason(StrEnum):
    LINK = "link"
    MEDIA = "media"
    QUOTE = "quote"
    REPOST = "repost"
    SEED = "seed"
    TEXT = "text"

    def __str__(self) -> str:
        return str(self.value)
