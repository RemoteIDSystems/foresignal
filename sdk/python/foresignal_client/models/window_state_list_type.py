from enum import StrEnum


class WindowStateListType(StrEnum):
    ABSENCE = "absence"
    ANOMALY = "anomaly"
    COUNT = "count"

    def __str__(self) -> str:
        return str(self.value)
