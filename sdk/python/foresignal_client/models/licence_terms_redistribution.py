from enum import StrEnum


class LicenceTermsRedistribution(StrEnum):
    ALLOWED = "ALLOWED"
    FORBIDDEN = "FORBIDDEN"
    LOCAL_ONLY = "LOCAL_ONLY"
    UNKNOWN = "UNKNOWN"

    def __str__(self) -> str:
        return str(self.value)
