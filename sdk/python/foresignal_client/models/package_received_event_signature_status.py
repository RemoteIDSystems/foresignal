from enum import StrEnum


class PackageReceivedEventSignatureStatus(StrEnum):
    INVALID = "invalid"
    UNKNOWN_KEY = "unknown-key"
    VALID = "valid"

    def __str__(self) -> str:
        return str(self.value)
