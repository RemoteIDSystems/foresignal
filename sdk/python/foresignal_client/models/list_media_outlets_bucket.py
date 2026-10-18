from enum import StrEnum


class ListMediaOutletsBucket(StrEnum):
    CENTER = "center"
    LEFT = "left"
    RIGHT = "right"
    UNRATED = "unrated"

    def __str__(self) -> str:
        return str(self.value)
