from enum import StrEnum


class LeadTimeStatsFiltersFoundByType3Type1(StrEnum):
    CUE = "cue"
    ORGANIC = "organic"

    def __str__(self) -> str:
        return str(self.value)
