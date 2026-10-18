from enum import StrEnum


class NotificationPreferenceInputEventTypesItem(StrEnum):
    ASSESSMENT = "assessment"
    CHRONICLE = "chronicle"
    COMMENT_REPLY = "comment-reply"
    MENTION = "mention"
    REPORT = "report"
    SHARE = "share"

    def __str__(self) -> str:
        return str(self.value)
