from enum import StrEnum


class NtfyMessageEvent(StrEnum):
    MESSAGE = "message"

    def __str__(self) -> str:
        return str(self.value)
