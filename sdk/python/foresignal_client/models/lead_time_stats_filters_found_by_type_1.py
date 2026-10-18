from enum import StrEnum


class LeadTimeStatsFiltersFoundByType1(StrEnum):
    CUE = "cue"
    ORGANIC = "organic"

    def __str__(self) -> str:
        return str(self.value)
