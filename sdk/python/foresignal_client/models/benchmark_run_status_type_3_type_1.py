from enum import StrEnum


class BenchmarkRunStatusType3Type1(StrEnum):
    FAIL = "fail"
    PASS = "pass"
    WARN = "warn"

    def __str__(self) -> str:
        return str(self.value)
