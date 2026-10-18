from enum import StrEnum


class ReportDraftRequestTemplate(StrEnum):
    CHRONICLE_RECAP = "chronicle-recap"
    CUSTOM = "custom"
    INVESTIGATION_SUMMARY = "investigation-summary"
    SITUATION_BRIEF = "situation-brief"
    WEEKLY_ASSESSMENT = "weekly-assessment"

    def __str__(self) -> str:
        return str(self.value)
