from enum import StrEnum


class ApproachConditionType(StrEnum):
    APPROACH = "approach"

    def __str__(self) -> str:
        return str(self.value)
