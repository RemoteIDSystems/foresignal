from enum import StrEnum


class AllowedChatScope(StrEnum):
    AGENT = "agent"
    READ = "read"

    def __str__(self) -> str:
        return str(self.value)
