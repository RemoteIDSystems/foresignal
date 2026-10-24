from enum import StrEnum


class IntelCalibrationScope(StrEnum):
    INTEL_EVENT = "intel-event"
    PRECURSOR_ACTIVE = "precursor-active"

    def __str__(self) -> str:
        return str(self.value)
