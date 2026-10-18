from enum import StrEnum


class DoctorReportProbeType0SkipReasonType2Type1(StrEnum):
    BACKOFF = "backoff"
    BUDGET = "budget"
    NOT_REQUESTED = "not-requested"
    NO_UPSTREAM = "no-upstream"

    def __str__(self) -> str:
        return str(self.value)
