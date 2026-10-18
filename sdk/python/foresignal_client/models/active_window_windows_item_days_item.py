from enum import StrEnum


class ActiveWindowWindowsItemDaysItem(StrEnum):
    FRI = "FRI"
    MON = "MON"
    SAT = "SAT"
    SUN = "SUN"
    THU = "THU"
    TUE = "TUE"
    WED = "WED"

    def __str__(self) -> str:
        return str(self.value)
