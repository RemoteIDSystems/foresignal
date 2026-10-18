from enum import StrEnum


class ListBenchmarkCasesSource(StrEnum):
    AGENT = "agent"
    AUTO = "auto"
    OPERATOR = "operator"

    def __str__(self) -> str:
        return str(self.value)
