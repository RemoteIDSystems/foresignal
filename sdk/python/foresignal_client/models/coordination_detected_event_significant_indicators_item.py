from enum import StrEnum


class CoordinationDetectedEventSignificantIndicatorsItem(StrEnum):
    ACCOUNTAGESKEW = "accountAgeSkew"
    COREPOSTBURSTS = "coRepostBursts"
    SYNCHRONY = "synchrony"
    TEXTDUPLICATION = "textDuplication"

    def __str__(self) -> str:
        return str(self.value)
