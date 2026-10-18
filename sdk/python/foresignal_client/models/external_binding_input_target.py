from enum import StrEnum


class ExternalBindingInputTarget(StrEnum):
    CANONICAL_FAMILY = "canonical-family"
    CHRONICLE = "chronicle"
    EVIDENCE = "evidence"
    FORECAST = "forecast"
    INDICATOR = "indicator"
    NEWS = "news"
    PLACE = "place"
    SERIES = "series"
    SITUATION = "situation"
    SOCIAL = "social"

    def __str__(self) -> str:
        return str(self.value)
