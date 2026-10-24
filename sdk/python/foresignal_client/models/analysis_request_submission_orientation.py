from enum import StrEnum


class AnalysisRequestSubmissionOrientation(StrEnum):
    INVERSE = "inverse"
    SAME = "same"

    def __str__(self) -> str:
        return str(self.value)
