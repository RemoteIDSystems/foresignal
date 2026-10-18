from enum import StrEnum


class PairingCodeScope(StrEnum):
    AGENT = "agent"
    READ = "read"

    def __str__(self) -> str:
        return str(self.value)
