from enum import StrEnum


class LicenceTermsCommercialUse(StrEnum):
    ALLOWED = "ALLOWED"
    FORBIDDEN = "FORBIDDEN"
    UNKNOWN = "UNKNOWN"

    def __str__(self) -> str:
        return str(self.value)
