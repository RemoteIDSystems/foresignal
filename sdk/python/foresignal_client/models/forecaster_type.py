from enum import StrEnum


class ForecasterType(StrEnum):
    ANALOG = "analog"
    BASE_RATE = "base-rate"
    CLIMATOLOGY = "climatology"
    ENSEMBLE = "ensemble"
    EXTERNAL = "external"
    IMPACT = "impact"
    INDICATOR_BAYES = "indicator-bayes"
    LLM = "llm"
    OPERATOR = "operator"
    PERSISTENCE = "persistence"
    PRECURSOR = "precursor"
    STATISTICAL = "statistical"
    THRESHOLD = "threshold"

    def __str__(self) -> str:
        return str(self.value)
