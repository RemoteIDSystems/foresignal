from enum import StrEnum


class GetProductTimelineMode(StrEnum):
    SEAMLESS = "seamless"

    def __str__(self) -> str:
        return str(self.value)
