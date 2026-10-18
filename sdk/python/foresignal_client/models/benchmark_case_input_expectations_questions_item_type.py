from enum import StrEnum


class BenchmarkCaseInputExpectationsQuestionsItemType(StrEnum):
    BINARY = "binary"
    NUMERIC = "numeric"

    def __str__(self) -> str:
        return str(self.value)
