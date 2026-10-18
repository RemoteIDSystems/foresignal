from enum import StrEnum


class NewsStoryBlindspotType2Type1(StrEnum):
    LEFT = "left"
    RIGHT = "right"

    def __str__(self) -> str:
        return str(self.value)
