from enum import StrEnum


class PrecisionComponentsItemComponent(StrEnum):
    FINDING = "finding"
    INTEL_EVENT = "intel-event"
    PRECURSOR_ACTIVE = "precursor-active"
    SITUATION = "situation"

    def __str__(self) -> str:
        return str(self.value)
