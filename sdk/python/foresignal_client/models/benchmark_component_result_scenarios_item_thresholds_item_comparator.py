from enum import StrEnum


class BenchmarkComponentResultScenariosItemThresholdsItemComparator(StrEnum):
    EQ = "eq"
    GE = "ge"
    LE = "le"
    WITHIN = "within"

    def __str__(self) -> str:
        return str(self.value)
