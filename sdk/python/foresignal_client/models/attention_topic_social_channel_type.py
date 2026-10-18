from enum import StrEnum


class AttentionTopicSocialChannelType(StrEnum):
    MASTODON_HASHTAG = "mastodon-hashtag"
    MASTODON_INSTANCE = "mastodon-instance"
    SUBREDDIT = "subreddit"
    YOUTUBE_CHANNEL = "youtube-channel"

    def __str__(self) -> str:
        return str(self.value)
