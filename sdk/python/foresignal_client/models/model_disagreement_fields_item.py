from enum import StrEnum


class ModelDisagreementFieldsItem(StrEnum):
    GUST = "gust"
    PRECIP24H = "precip24h"
    T2MMAX = "t2mMax"

    def __str__(self) -> str:
        return str(self.value)
