from enum import StrEnum


class BenchmarkComponentResultStatus(StrEnum):
    FAIL = "fail"
    PASS = "pass"
    WARN = "warn"

    def __str__(self) -> str:
        return str(self.value)
