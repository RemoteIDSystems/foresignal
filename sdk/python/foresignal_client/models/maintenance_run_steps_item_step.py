from enum import StrEnum


class MaintenanceRunStepsItemStep(StrEnum):
    BASE_RATE_REFRESH = "base-rate-refresh"
    BENCHMARK_HISTORICAL = "benchmark-historical"
    ENSEMBLE_WEIGHTS = "ensemble-weights"
    EXTREMIZING_CHECK = "extremizing-check"
    HYPOTHESIS_ARCHIVAL_PROPOSALS = "hypothesis-archival-proposals"
    INDEX_WEIGHT_FIT = "index-weight-fit"
    LLM_RETROSPECTIVE_EVALUATION = "llm-retrospective-evaluation"
    OBSERVABILITY_REESTIMATION = "observability-reestimation"
    PLATT_REFIT = "platt-refit"
    PRECURSOR_RETEST = "precursor-retest"
    SERIES_METHOD_RESELECTION = "series-method-reselection"

    def __str__(self) -> str:
        return str(self.value)
