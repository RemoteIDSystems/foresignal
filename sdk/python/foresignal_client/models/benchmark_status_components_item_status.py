from enum import StrEnum


class BenchmarkStatusComponentsItemStatus(StrEnum):
    FAIL = "fail"
    PASS = "pass"
    UNKNOWN = "unknown"
    WARN = "warn"

    def __str__(self) -> str:
        return str(self.value)
