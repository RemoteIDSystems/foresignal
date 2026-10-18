from enum import StrEnum


class StreamTicketPurpose(StrEnum):
    STREAM = "stream"
    TILES = "tiles"

    def __str__(self) -> str:
        return str(self.value)
