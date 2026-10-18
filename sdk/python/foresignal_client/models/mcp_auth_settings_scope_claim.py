from enum import StrEnum


class McpAuthSettingsScopeClaim(StrEnum):
    SCOPE = "scope"
    SCP = "scp"

    def __str__(self) -> str:
        return str(self.value)
