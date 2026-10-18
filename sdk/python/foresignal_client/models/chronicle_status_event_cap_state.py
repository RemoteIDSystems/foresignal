from enum import StrEnum


class ChronicleStatusEventCapState(StrEnum):
    EXCEEDED = "exceeded"
    OK = "ok"
    WARNING = "warning"

    def __str__(self) -> str:
        return str(self.value)
