from enum import StrEnum


class SocialCollectionPathPath(StrEnum):
    ACCOUNT_POLL = "account-poll"
    CHANNEL_POLL = "channel-poll"
    COUNTS = "counts"
    CUE = "cue"
    HASHTAG_SEARCH = "hashtag-search"
    REPLAY = "replay"
    SEARCH = "search"
    STREAM = "stream"
    TRENDS = "trends"

    def __str__(self) -> str:
        return str(self.value)
