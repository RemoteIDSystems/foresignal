from enum import StrEnum


class GetWmtsKvpREQUEST(StrEnum):
    GETCAPABILITIES = "GetCapabilities"
    GETTILE = "GetTile"

    def __str__(self) -> str:
        return str(self.value)
