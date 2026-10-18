from enum import StrEnum


class ChronicleSocialVolumeSelectorMetricsItem(StrEnum):
    ENGAGEMENT = "engagement"
    POSTS = "posts"
    REPOSTS = "reposts"
    UNIQUEAUTHORS = "uniqueAuthors"

    def __str__(self) -> str:
        return str(self.value)
