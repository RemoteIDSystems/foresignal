from enum import StrEnum


class OverlandAckResult(StrEnum):
    OK = "ok"

    def __str__(self) -> str:
        return str(self.value)
