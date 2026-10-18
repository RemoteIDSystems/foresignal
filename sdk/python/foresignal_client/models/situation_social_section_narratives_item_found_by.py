from enum import StrEnum


class SituationSocialSectionNarrativesItemFoundBy(StrEnum):
    CUE = "cue"
    ORGANIC = "organic"

    def __str__(self) -> str:
        return str(self.value)
