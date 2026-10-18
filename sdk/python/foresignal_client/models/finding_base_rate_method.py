from enum import StrEnum


class FindingBaseRateMethod(StrEnum):
    DETECTOR_FALSE_ALARM_RATE = "detector-false-alarm-rate"
    INDEPENDENCE_BASELINE = "independence-baseline"
    OTHER = "other"
    PLACEBO_DISTRIBUTION = "placebo-distribution"
    PRECURSOR_BASE_RATE = "precursor-base-rate"

    def __str__(self) -> str:
        return str(self.value)
