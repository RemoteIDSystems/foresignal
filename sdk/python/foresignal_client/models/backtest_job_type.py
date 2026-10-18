from enum import StrEnum


class BacktestJobType(StrEnum):
    BASE_RATE_REFRESH = "base-rate-refresh"
    BENCHMARK = "benchmark"
    EVENT_STUDY = "event-study"
    FORECAST_EVALUATION = "forecast-evaluation"
    MANIFEST_REPLAY = "manifest-replay"
    PRECURSOR = "precursor"
    PRECURSOR_MINING = "precursor-mining"
    QUALITY = "quality"
    RECIPE = "recipe"
    REFERENCE_IMPORT = "reference-import"
    RULE = "rule"

    def __str__(self) -> str:
        return str(self.value)
