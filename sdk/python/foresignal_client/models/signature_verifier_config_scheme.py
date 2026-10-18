from enum import StrEnum


class SignatureVerifierConfigScheme(StrEnum):
    FORESIGNAL = "foresignal"
    HMAC_SHA256 = "hmac-sha256"
    NONE = "none"
    STANDARD_WEBHOOKS = "standard-webhooks"

    def __str__(self) -> str:
        return str(self.value)
