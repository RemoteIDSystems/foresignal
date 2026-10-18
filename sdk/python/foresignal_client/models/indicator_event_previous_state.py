from enum import StrEnum


class IndicatorEventPreviousState(StrEnum):
    ABSENT = "absent"
    EMERGING = "emerging"
    OBSERVED = "observed"
    PENDING = "pending"
    REPORTED = "reported"
    RETIRED = "retired"
    UNKNOWN_COVERAGE = "unknown-coverage"

    def __str__(self) -> str:
        return str(self.value)
