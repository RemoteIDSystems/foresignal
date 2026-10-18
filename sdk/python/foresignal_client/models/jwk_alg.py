from enum import StrEnum


class JwkAlg(StrEnum):
    ED25519 = "Ed25519"
    EDDSA = "EdDSA"

    def __str__(self) -> str:
        return str(self.value)
