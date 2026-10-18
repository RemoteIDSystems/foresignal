from enum import StrEnum


class OwnTracksMessageType(StrEnum):
    CARD = "card"
    ENCRYPTED = "encrypted"
    LOCATION = "location"
    LWT = "lwt"
    STATUS = "status"
    TRANSITION = "transition"
    WAYPOINT = "waypoint"

    def __str__(self) -> str:
        return str(self.value)
