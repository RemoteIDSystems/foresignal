from enum import StrEnum


class GetObjectDossierIncludeItem(StrEnum):
    ANALYSIS = "analysis"
    COVERAGE = "coverage"
    FORECAST = "forecast"
    IMAGERY = "imagery"
    IMPACTS = "impacts"
    OVERVIEW = "overview"
    RELATED = "related"
    TIMELINE = "timeline"

    def __str__(self) -> str:
        return str(self.value)
