from enum import StrEnum


class CoverageJsonType(StrEnum):
    COVERAGE = "Coverage"
    COVERAGECOLLECTION = "CoverageCollection"

    def __str__(self) -> str:
        return str(self.value)
