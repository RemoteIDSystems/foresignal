from enum import StrEnum


class SituationOrigin(StrEnum):
    AGENT = "agent"
    ANALYST = "analyst"
    AUTO = "auto"
    OPERATOR = "operator"

    def __str__(self) -> str:
        return str(self.value)
