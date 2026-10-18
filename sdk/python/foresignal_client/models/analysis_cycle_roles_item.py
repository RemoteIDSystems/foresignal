from enum import StrEnum


class AnalysisCycleRolesItem(StrEnum):
    ANALYST = "analyst"
    FORECASTER = "forecaster"
    PROPOSER = "proposer"
    RED_TEAM = "red-team"

    def __str__(self) -> str:
        return str(self.value)
