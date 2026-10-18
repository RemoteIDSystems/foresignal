from enum import StrEnum


class SampleStatus(StrEnum):
    NO_COVERAGE = "no-coverage"
    NO_DATA = "no-data"
    OK = "ok"
    STALE = "stale"
    STALE_POSITION = "stale-position"
    UNAVAILABLE = "unavailable"

    def __str__(self) -> str:
        return str(self.value)
