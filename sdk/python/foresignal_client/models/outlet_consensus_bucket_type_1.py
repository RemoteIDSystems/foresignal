from enum import StrEnum


class OutletConsensusBucketType1(StrEnum):
    CENTER = "center"
    LEFT = "left"
    RIGHT = "right"

    def __str__(self) -> str:
        return str(self.value)
