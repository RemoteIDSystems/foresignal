from enum import StrEnum


class PeerCreateMode(StrEnum):
    PULL = "PULL"
    RECEIVE = "RECEIVE"

    def __str__(self) -> str:
        return str(self.value)
