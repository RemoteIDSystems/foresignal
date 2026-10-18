from enum import StrEnum


class ListChronicleAccessRequestsDirection(StrEnum):
    INCOMING = "incoming"
    OUTGOING = "outgoing"

    def __str__(self) -> str:
        return str(self.value)
