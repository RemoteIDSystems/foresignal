from enum import StrEnum


class GetSocialVolumeMetric(StrEnum):
    DENOMINATOR = "denominator"
    ENGAGEMENT = "engagement"
    POSTS = "posts"
    REPOSTS = "reposts"
    UNIQUEAUTHORS = "uniqueAuthors"

    def __str__(self) -> str:
        return str(self.value)
