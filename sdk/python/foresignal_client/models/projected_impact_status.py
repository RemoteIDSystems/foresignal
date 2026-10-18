from enum import StrEnum


class ProjectedImpactStatus(StrEnum):
    ACTIVE = "active"
    SUPERSEDED = "superseded"

    def __str__(self) -> str:
        return str(self.value)
