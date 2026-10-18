from enum import StrEnum


class SocialComparisonContradictionType0Reason(StrEnum):
    MEDIA_REUSE = "media-reuse"
    NEGATING_RECORD = "negating-record"

    def __str__(self) -> str:
        return str(self.value)
