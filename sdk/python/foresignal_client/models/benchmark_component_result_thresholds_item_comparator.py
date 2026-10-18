from enum import StrEnum


class BenchmarkComponentResultThresholdsItemComparator(StrEnum):
    EQ = "eq"
    GE = "ge"
    LE = "le"
    WITHIN = "within"

    def __str__(self) -> str:
        return str(self.value)
