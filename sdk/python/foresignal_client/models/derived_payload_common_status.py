from enum import StrEnum


class DerivedPayloadCommonStatus(StrEnum):
    DEGRADED_INPUT = "degraded-input"
    INSUFFICIENT_DATA = "insufficient-data"
    NO_COVERAGE = "no-coverage"
    OK = "ok"

    def __str__(self) -> str:
        return str(self.value)
