from enum import StrEnum


class RegionalIndexDefInputComponentsItemTransform(StrEnum):
    LEVEL = "level"
    LOG1P = "log1p"
    PCTRANK = "pctrank"
    ZSCORE = "zscore"

    def __str__(self) -> str:
        return str(self.value)
