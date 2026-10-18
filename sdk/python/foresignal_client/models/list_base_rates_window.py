from enum import StrEnum


class ListBaseRatesWindow(StrEnum):
    P28D = "P28D"
    P7D = "P7D"
    P90D = "P90D"

    def __str__(self) -> str:
        return str(self.value)
