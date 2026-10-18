from enum import StrEnum


class JammingCellPayloadGrid(StrEnum):
    DEG1 = "deg1"
    H3 = "h3"

    def __str__(self) -> str:
        return str(self.value)
