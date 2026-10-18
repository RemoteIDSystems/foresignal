from enum import StrEnum


class GetSocialLeadTimesFoundBy(StrEnum):
    CUE = "cue"
    ORGANIC = "organic"

    def __str__(self) -> str:
        return str(self.value)
