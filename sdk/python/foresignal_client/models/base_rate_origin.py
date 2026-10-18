from enum import StrEnum


class BaseRateOrigin(StrEnum):
    AGENT = "agent"
    AUTO = "auto"
    OPERATOR = "operator"

    def __str__(self) -> str:
        return str(self.value)
