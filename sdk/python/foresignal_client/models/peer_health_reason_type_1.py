from enum import StrEnum


class PeerHealthReasonType1(StrEnum):
    PEER_KEY_MISMATCH = "PEER_KEY_MISMATCH"
    PEER_RESYNC = "PEER_RESYNC"
    PEER_UNREACHABLE = "PEER_UNREACHABLE"

    def __str__(self) -> str:
        return str(self.value)
