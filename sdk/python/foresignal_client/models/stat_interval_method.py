from enum import StrEnum


class StatIntervalMethod(StrEnum):
    EMPIRICAL = "empirical"
    JEFFREYS = "jeffreys"
    PAIRED_BOOTSTRAP = "paired-bootstrap"
    WILSON = "wilson"

    def __str__(self) -> str:
        return str(self.value)
