from enum import StrEnum


class ListNewsStoriesBlindspot(StrEnum):
    ANY = "any"
    LEFT = "left"
    RIGHT = "right"

    def __str__(self) -> str:
        return str(self.value)
