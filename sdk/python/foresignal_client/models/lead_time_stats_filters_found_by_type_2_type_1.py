from enum import StrEnum


class LeadTimeStatsFiltersFoundByType2Type1(StrEnum):
    CUE = "cue"
    ORGANIC = "organic"

    def __str__(self) -> str:
        return str(self.value)
