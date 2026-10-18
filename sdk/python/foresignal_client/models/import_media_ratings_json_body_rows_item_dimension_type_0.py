from enum import StrEnum


class ImportMediaRatingsJsonBodyRowsItemDimensionType0(StrEnum):
    FACTUALITY = "factuality"
    LEAN = "lean"
    OWNERSHIP_TYPE = "ownership-type"
    RELIABILITY = "reliability"
    STATE_AFFILIATION = "state-affiliation"

    def __str__(self) -> str:
        return str(self.value)
