from enum import StrEnum


class GetAssessmentBundleFormat(StrEnum):
    GEOPARQUET = "geoparquet"
    JSON = "json"

    def __str__(self) -> str:
        return str(self.value)
