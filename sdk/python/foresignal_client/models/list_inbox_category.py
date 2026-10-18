from enum import StrEnum


class ListInboxCategory(StrEnum):
    ACCESS_REQUEST = "access-request"
    ASSESSMENT = "assessment"
    CHRONICLE = "chronicle"
    COMMENT_REPLY = "comment-reply"
    MENTION = "mention"
    REPORT = "report"
    SAVED_SEARCH = "saved-search"
    SHARE = "share"
    SYSTEM = "system"

    def __str__(self) -> str:
        return str(self.value)
