from enum import StrEnum


class SeriesForecastModelBacktestResultsItemMethod(StrEnum):
    DAMPED_TREND = "damped-trend"
    ETS_AAA = "ets-aaa"
    ETS_ANN = "ets-ann"
    NB_GLM = "nb-glm"
    SEASONAL_NAIVE = "seasonal-naive"

    def __str__(self) -> str:
        return str(self.value)
