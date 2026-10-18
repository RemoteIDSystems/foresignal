from enum import StrEnum


class ReceivedPackageSignatureStatus(StrEnum):
    INVALID = "invalid"
    UNKNOWN_KEY = "unknown-key"
    VALID = "valid"

    def __str__(self) -> str:
        return str(self.value)
