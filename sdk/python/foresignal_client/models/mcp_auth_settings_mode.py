from enum import StrEnum


class McpAuthSettingsMode(StrEnum):
    BOTH = "both"
    OAUTH = "oauth"
    TOKEN = "token"

    def __str__(self) -> str:
        return str(self.value)
