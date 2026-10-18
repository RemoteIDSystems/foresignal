from enum import StrEnum


class AnalysisSeriesInputDetectorsItem(StrEnum):
    CHANGEPOINT_BOCPD = "changepoint-bocpd"
    CHANGEPOINT_CUSUM = "changepoint-cusum"
    FORECAST = "forecast"

    def __str__(self) -> str:
        return str(self.value)
