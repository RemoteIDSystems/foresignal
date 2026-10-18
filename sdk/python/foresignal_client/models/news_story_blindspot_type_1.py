from enum import StrEnum


class NewsStoryBlindspotType1(StrEnum):
    LEFT = "left"
    RIGHT = "right"

    def __str__(self) -> str:
        return str(self.value)
