from enum import StrEnum


class BenchmarkComponentResultScenariosItemScenario(StrEnum):
    BASE_RATE_DRIFT = "base-rate-drift"
    COMMON_DRIVER = "common-driver"
    COVERAGE_OUTAGE = "coverage-outage"
    DUPLICATE_ECHO = "duplicate-echo"
    HISTORICAL = "historical"
    LATE_REVISION = "late-revision"
    NULL_SEASONAL = "null-seasonal"
    PRE_EVENT_MOVE = "pre-event-move"
    SPIKE = "spike"
    STEP_CHANGE = "step-change"
    TRUE_PRECURSOR = "true-precursor"

    def __str__(self) -> str:
        return str(self.value)
