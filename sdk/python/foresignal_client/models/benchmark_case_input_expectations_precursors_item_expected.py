from enum import StrEnum


class BenchmarkCaseInputExpectationsPrecursorsItemExpected(StrEnum):
    NOT_VALIDATED = "not-validated"
    VALIDATED = "validated"

    def __str__(self) -> str:
        return str(self.value)
