from enum import StrEnum


class LeadTimeStatsGroupsItemFoundBy(StrEnum):
    CUE = "cue"
    ORGANIC = "organic"

    def __str__(self) -> str:
        return str(self.value)
