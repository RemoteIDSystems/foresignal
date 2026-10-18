from enum import StrEnum


class SocialQuotaUnit(StrEnum):
    HASHTAGS = "hashtags"
    POSTS = "posts"
    QUERIES = "queries"
    REQUESTS = "requests"
    UNITS = "units"

    def __str__(self) -> str:
        return str(self.value)
