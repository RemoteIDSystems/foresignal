from enum import StrEnum


class PropagationEdgeType(StrEnum):
    CROSS_PLATFORM = "cross-platform"
    LINK = "link"
    MEDIA_MATCH = "media-match"
    QUOTE = "quote"
    REPLY = "reply"
    REPOST = "repost"
    TEXT_MATCH = "text-match"

    def __str__(self) -> str:
        return str(self.value)
