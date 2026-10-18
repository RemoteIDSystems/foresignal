from enum import StrEnum


class NarrativeTimelineInterval(StrEnum):
    P1D = "P1D"
    PT15M = "PT15M"
    PT1H = "PT1H"
    PT5M = "PT5M"

    def __str__(self) -> str:
        return str(self.value)
