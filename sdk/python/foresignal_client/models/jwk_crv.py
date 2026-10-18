from enum import StrEnum


class JwkCrv(StrEnum):
    ED25519 = "Ed25519"

    def __str__(self) -> str:
        return str(self.value)
