from enum import StrEnum


class ReportBlockBaseType(StrEnum):
    ATTACHMENTS = "attachments"
    BLUF = "bluf"
    COVERAGE = "coverage"
    EVIDENCE = "evidence"
    FORECASTS = "forecasts"
    HYPOTHESES = "hypotheses"
    MAP = "map"
    MARKDOWN = "markdown"
    OBJECTS = "objects"
    SOURCES = "sources"
    TIMELINE = "timeline"

    def __str__(self) -> str:
        return str(self.value)
