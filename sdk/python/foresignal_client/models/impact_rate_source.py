from enum import StrEnum


class ImpactRateSource(StrEnum):
    MIXED = "mixed"
    OWN_HISTORY = "own-history"
    REFERENCE_IMPORT = "reference-import"

    def __str__(self) -> str:
        return str(self.value)
