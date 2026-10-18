from enum import StrEnum


class BenchmarkCaseExpectationsPrecursorsItemExpected(StrEnum):
    NOT_VALIDATED = "not-validated"
    VALIDATED = "validated"

    def __str__(self) -> str:
        return str(self.value)
