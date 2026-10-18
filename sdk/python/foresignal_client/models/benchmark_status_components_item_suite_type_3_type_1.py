from enum import StrEnum


class BenchmarkStatusComponentsItemSuiteType3Type1(StrEnum):
    ALL = "all"
    HISTORICAL = "historical"
    SYNTHETIC = "synthetic"

    def __str__(self) -> str:
        return str(self.value)
