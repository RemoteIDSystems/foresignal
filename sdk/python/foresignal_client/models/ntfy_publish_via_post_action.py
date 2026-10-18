from enum import StrEnum


class NtfyPublishViaPostAction(StrEnum):
    PUBLISH = "publish"
    SEND = "send"
    TRIGGER = "trigger"

    def __str__(self) -> str:
        return str(self.value)
