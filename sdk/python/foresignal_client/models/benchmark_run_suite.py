from enum import StrEnum


class BenchmarkRunSuite(StrEnum):
    ALL = "all"
    HISTORICAL = "historical"
    SYNTHETIC = "synthetic"

    def __str__(self) -> str:
        return str(self.value)
