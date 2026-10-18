from enum import StrEnum


class NtfyPublishViaGetAction(StrEnum):
    PUBLISH = "publish"
    SEND = "send"
    TRIGGER = "trigger"

    def __str__(self) -> str:
        return str(self.value)
