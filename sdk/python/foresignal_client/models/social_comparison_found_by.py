from enum import StrEnum


class SocialComparisonFoundBy(StrEnum):
    CUE = "cue"
    ORGANIC = "organic"

    def __str__(self) -> str:
        return str(self.value)
