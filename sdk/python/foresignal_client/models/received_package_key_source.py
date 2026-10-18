from enum import StrEnum


class ReceivedPackageKeySource(StrEnum):
    PEER_JWKS = "peer-jwks"
    PINNED = "pinned"

    def __str__(self) -> str:
        return str(self.value)
