from enum import StrEnum


class BenchmarkCaseSource(StrEnum):
    AGENT = "agent"
    AUTO = "auto"
    OPERATOR = "operator"

    def __str__(self) -> str:
        return str(self.value)
