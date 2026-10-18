from enum import StrEnum


class SocialPlatformId(StrEnum):
    BLUESKY_JETSTREAM = "bluesky-jetstream"
    BLUESKY_SEARCH = "bluesky-search"
    INSTAGRAM_HASHTAGS = "instagram-hashtags"
    MASTODON = "mastodon"
    REDDIT = "reddit"
    THREADS = "threads"
    X = "x"
    YOUTUBE = "youtube"

    def __str__(self) -> str:
        return str(self.value)
