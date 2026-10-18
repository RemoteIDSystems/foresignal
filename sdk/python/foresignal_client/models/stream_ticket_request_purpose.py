from enum import StrEnum


class StreamTicketRequestPurpose(StrEnum):
    STREAM = "stream"
    TILES = "tiles"

    def __str__(self) -> str:
        return str(self.value)
