from enum import StrEnum


class EdpStreamMode(StrEnum):
    ATOMIC = "atomic"
    LAYER = "layer"

    def __str__(self) -> str:
        return str(self.value)
