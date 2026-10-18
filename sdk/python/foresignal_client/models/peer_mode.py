from enum import StrEnum


class PeerMode(StrEnum):
    PULL = "PULL"
    RECEIVE = "RECEIVE"

    def __str__(self) -> str:
        return str(self.value)
