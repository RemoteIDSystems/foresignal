from enum import StrEnum


class SystemRastersProfile(StrEnum):
    LARGE = "large"
    SMALL = "small"

    def __str__(self) -> str:
        return str(self.value)
