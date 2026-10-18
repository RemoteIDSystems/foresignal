from enum import StrEnum


class BenchmarkCaseExpectationsQuestionsItemType(StrEnum):
    BINARY = "binary"
    NUMERIC = "numeric"

    def __str__(self) -> str:
        return str(self.value)
