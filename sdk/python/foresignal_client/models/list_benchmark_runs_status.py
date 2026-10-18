from enum import StrEnum


class ListBenchmarkRunsStatus(StrEnum):
    FAIL = "fail"
    PASS = "pass"
    WARN = "warn"

    def __str__(self) -> str:
        return str(self.value)
