from enum import StrEnum


class CommentEventReason(StrEnum):
    MENTION = "mention"
    REPLY = "reply"

    def __str__(self) -> str:
        return str(self.value)
