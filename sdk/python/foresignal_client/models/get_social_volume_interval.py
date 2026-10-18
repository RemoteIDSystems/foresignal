from enum import StrEnum


class GetSocialVolumeInterval(StrEnum):
    P1D = "P1D"
    PT1H = "PT1H"

    def __str__(self) -> str:
        return str(self.value)
