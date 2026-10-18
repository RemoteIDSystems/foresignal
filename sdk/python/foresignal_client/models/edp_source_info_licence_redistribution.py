from enum import StrEnum


class EdpSourceInfoLicenceRedistribution(StrEnum):
    ALLOWED = "allowed"
    LOCAL_ONLY = "local-only"
    UNKNOWN = "unknown"

    def __str__(self) -> str:
        return str(self.value)
