from enum import StrEnum


class CustomSourceInputTtlAnchor(StrEnum):
    OBSERVED = "OBSERVED"
    RECEIVED = "RECEIVED"

    def __str__(self) -> str:
        return str(self.value)
