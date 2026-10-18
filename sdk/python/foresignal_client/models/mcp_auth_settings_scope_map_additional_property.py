from enum import StrEnum


class McpAuthSettingsScopeMapAdditionalProperty(StrEnum):
    AGENT = "agent"
    READ = "read"

    def __str__(self) -> str:
        return str(self.value)
