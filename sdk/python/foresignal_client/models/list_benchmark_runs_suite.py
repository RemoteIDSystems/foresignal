from enum import StrEnum


class ListBenchmarkRunsSuite(StrEnum):
    ALL = "all"
    HISTORICAL = "historical"
    SYNTHETIC = "synthetic"

    def __str__(self) -> str:
        return str(self.value)
