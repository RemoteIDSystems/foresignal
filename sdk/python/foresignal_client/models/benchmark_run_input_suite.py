from enum import StrEnum


class BenchmarkRunInputSuite(StrEnum):
    ALL = "all"
    HISTORICAL = "historical"
    SYNTHETIC = "synthetic"

    def __str__(self) -> str:
        return str(self.value)
