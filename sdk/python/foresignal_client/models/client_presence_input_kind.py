from enum import StrEnum


class ClientPresenceInputKind(StrEnum):
    DESKTOP = "desktop"
    MOBILE = "mobile"
    WEB = "web"

    def __str__(self) -> str:
        return str(self.value)
