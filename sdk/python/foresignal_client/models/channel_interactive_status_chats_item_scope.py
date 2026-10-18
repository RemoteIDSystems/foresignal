from enum import StrEnum


class ChannelInteractiveStatusChatsItemScope(StrEnum):
    AGENT = "agent"
    READ = "read"

    def __str__(self) -> str:
        return str(self.value)
