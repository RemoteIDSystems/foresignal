from enum import StrEnum


class PairingCodeInputScope(StrEnum):
    AGENT = "agent"
    READ = "read"

    def __str__(self) -> str:
        return str(self.value)
