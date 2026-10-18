from enum import StrEnum


class PluginState(StrEnum):
    DISABLED = "disabled"
    ENABLED = "enabled"
    ERROR = "error"
    HASH_MISMATCH = "hash-mismatch"

    def __str__(self) -> str:
        return str(self.value)
