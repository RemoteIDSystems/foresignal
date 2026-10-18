from enum import StrEnum


class ListPlacesKind(StrEnum):
    AIRPORT = "airport"
    ASSET = "asset"
    USER = "user"

    def __str__(self) -> str:
        return str(self.value)
